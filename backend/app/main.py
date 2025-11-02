"""
FastAPI Backend for Spowlo Music Downloader
Replicates functionality from spoti-down-m4a-opus-v4.2.py
"""
import asyncio
import os
import uuid
from typing import Optional, Dict, List
from datetime import datetime
from enum import Enum

from fastapi import FastAPI, HTTPException, BackgroundTasks
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import StreamingResponse
from pydantic import BaseModel

from .spotify_client import SpotifyAPI
from .jiosaavn_client import JioSaavnAPI
from .downloader import AudioDownloader, QualityPreset
from .job_manager import JobManager, JobStatus

app = FastAPI(title="Spowlo Music API", version="1.0.0")

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Initialize components
spotify_api = SpotifyAPI(
    client_id="2079ef31b1bb4feaaaa811d9f280faef",
    client_secret="2e76121a91864d41a768ba1eaf80610e"
)
jiosaavn_api = JioSaavnAPI()
downloader = AudioDownloader(output_dir="downloads")
job_manager = JobManager()


class Platform(str, Enum):
    SPOTIFY = "spotify"
    JIOSAAVN = "jiosaavn"
    YOUTUBE = "youtube"


class Quality(str, Enum):
    M4A_320 = "m4a_320"
    OPUS_160 = "opus_160"
    BEST = "best"


class TrackMetadata(BaseModel):
    id: str
    title: str
    artists: List[str]
    album: Optional[str] = None
    thumbnailUrl: Optional[str] = None
    duration: Optional[int] = None
    platform: Platform


class DownloadRequest(BaseModel):
    url: str
    quality: Quality
    metadata: Optional[TrackMetadata] = None


class DownloadResponse(BaseModel):
    job_id: str
    status: str
    message: str


class JobProgressResponse(BaseModel):
    job_id: str
    status: JobStatus
    progress: float
    current_line: str
    error: Optional[str] = None
    result_file: Optional[str] = None


@app.get("/")
async def root():
    """Health check endpoint"""
    return {
        "status": "online",
        "service": "Spowlo Music API",
        "version": "1.0.0"
    }


@app.get("/api/metadata/spotify/{track_id}")
async def get_spotify_metadata(track_id: str):
    """Get metadata for a Spotify track"""
    try:
        # Construct full URL if only ID is provided
        if not track_id.startswith("http"):
            track_url = f"https://open.spotify.com/track/{track_id}"
        else:
            track_url = track_id
        
        track_info = spotify_api.get_track_info(track_url)
        if not track_info:
            raise HTTPException(status_code=404, detail="Track not found")
        
        # Extract metadata
        track_name = track_info.get('name', 'Unknown')
        artists = [artist['name'] for artist in track_info.get('artists', [])]
        album_name = track_info.get('album', {}).get('name', 'Unknown')
        duration_ms = track_info.get('duration_ms', 0)
        thumbnail_url = None
        
        # Get largest album cover image
        images = track_info.get('album', {}).get('images', [])
        if images:
            thumbnail_url = images[0]['url']
        
        return TrackMetadata(
            id=track_id,
            title=track_name,
            artists=artists,
            album=album_name,
            thumbnailUrl=thumbnail_url,
            duration=duration_ms // 1000,  # Convert to seconds
            platform=Platform.SPOTIFY
        )
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/api/metadata/jiosaavn/{song_id}")
async def get_jiosaavn_metadata(song_id: str):
    """Get metadata for a JioSaavn song"""
    try:
        # Construct full URL if only ID is provided
        if not song_id.startswith("http"):
            song_url = f"https://www.jiosaavn.com/song/{song_id}"
        else:
            song_url = song_id
        
        song_details = jiosaavn_api.get_song_details(song_url)
        if not song_details:
            raise HTTPException(status_code=404, detail="Song not found")
        
        # Extract metadata
        title = song_details.get('song', song_details.get('title', 'Unknown'))
        
        # Handle different artist field formats
        artists_str = song_details.get('primary_artists', 
                                      song_details.get('singer', 
                                      song_details.get('artist', 'Unknown')))
        artists = [a.strip() for a in artists_str.split(',')]
        
        album = song_details.get('album', song_details.get('album_name', 'Unknown'))
        duration = int(song_details.get('duration', 0))
        
        # Get thumbnail - JioSaavn usually has image field
        thumbnail = song_details.get('image', 
                                    song_details.get('media_preview_url', None))
        
        return TrackMetadata(
            id=song_id,
            title=title,
            artists=artists,
            album=album,
            thumbnailUrl=thumbnail,
            duration=duration,
            platform=Platform.JIOSAAVN
        )
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/api/download", response_model=DownloadResponse)
async def start_download(request: DownloadRequest, background_tasks: BackgroundTasks):
    """Start a download job"""
    try:
        job_id = str(uuid.uuid4())
        
        # Create job
        job_manager.create_job(
            job_id=job_id,
            url=request.url,
            quality=request.quality,
            metadata=request.metadata
        )
        
        # Start download in background
        background_tasks.add_task(
            process_download,
            job_id=job_id,
            url=request.url,
            quality=request.quality,
            metadata=request.metadata
        )
        
        return DownloadResponse(
            job_id=job_id,
            status="started",
            message="Download job started successfully"
        )
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


async def process_download(
    job_id: str,
    url: str,
    quality: Quality,
    metadata: Optional[TrackMetadata]
):
    """Process download job"""
    try:
        # Determine quality preset
        quality_choice = '1'  # Default M4A 320kbps
        if quality == Quality.OPUS_160:
            quality_choice = '2'
        elif quality == Quality.BEST:
            quality_choice = '3'
        
        # Detect URL type
        platform = detect_url_type(url)
        
        # Update job status
        job_manager.update_job(
            job_id,
            status=JobStatus.PROCESSING,
            progress=0.1,
            current_line=f"Detected {platform} URL"
        )
        
        # Process based on platform
        if platform == Platform.JIOSAAVN:
            await process_jiosaavn_download(job_id, url, quality_choice, metadata)
        elif platform == Platform.SPOTIFY:
            await process_spotify_download(job_id, url, quality_choice, metadata)
        else:
            # Direct YouTube or other URL
            await process_youtube_download(job_id, url, quality_choice, metadata)
        
    except Exception as e:
        job_manager.update_job(
            job_id,
            status=JobStatus.FAILED,
            error=str(e),
            current_line=f"Error: {str(e)}"
        )


async def process_jiosaavn_download(
    job_id: str,
    url: str,
    quality_choice: str,
    metadata: Optional[TrackMetadata]
):
    """Process JioSaavn download"""
    try:
        job_manager.update_job(
            job_id,
            progress=0.2,
            current_line="Downloading from JioSaavn..."
        )
        
        # Use yt-dlp for JioSaavn (it has built-in support)
        result = await downloader.download_from_url(
            url=url,
            quality_choice=quality_choice,
            job_id=job_id,
            progress_callback=lambda prog, line: job_manager.update_job(
                job_id, progress=prog, current_line=line
            )
        )
        
        if result['success']:
            job_manager.update_job(
                job_id,
                status=JobStatus.COMPLETED,
                progress=1.0,
                current_line="Download completed",
                result_file=result.get('filename')
            )
        else:
            raise Exception(result.get('error', 'Unknown error'))
    
    except Exception as e:
        # Fallback to YouTube search
        song_name = url.split('/song/')[-1].split('/')[0].replace('-', ' ')
        job_manager.update_job(
            job_id,
            progress=0.3,
            current_line=f"Falling back to YouTube search: {song_name}"
        )
        await process_youtube_download(job_id, song_name, quality_choice, metadata)


async def process_spotify_download(
    job_id: str,
    url: str,
    quality_choice: str,
    metadata: Optional[TrackMetadata]
):
    """Process Spotify download via YouTube search"""
    try:
        # Fetch metadata if not provided
        if not metadata:
            track_id = url.split("/track/")[-1].split("?")[0]
            track_info = spotify_api.get_track_info(url)
            if not track_info:
                raise Exception("Failed to fetch track info")
            
            track_name = track_info.get('name', 'Unknown')
            artists = [artist['name'] for artist in track_info.get('artists', [])]
            search_query = f"{track_name} {' '.join(artists)}"
        else:
            search_query = f"{metadata.title} {' '.join(metadata.artists)}"
        
        job_manager.update_job(
            job_id,
            progress=0.2,
            current_line=f"Searching YouTube for: {search_query}"
        )
        
        # Search and download from YouTube
        await process_youtube_download(job_id, search_query, quality_choice, metadata)
    
    except Exception as e:
        raise Exception(f"Spotify download failed: {str(e)}")


async def process_youtube_download(
    job_id: str,
    search_query: str,
    quality_choice: str,
    metadata: Optional[TrackMetadata]
):
    """Process YouTube download"""
    try:
        job_manager.update_job(
            job_id,
            progress=0.3,
            current_line=f"Downloading from YouTube: {search_query}"
        )
        
        # If it's a search query, prepend ytsearch
        url = search_query
        if not search_query.startswith('http'):
            url = f"ytsearch1:{search_query}"
        
        result = await downloader.download_youtube(
            url=url,
            quality_choice=quality_choice,
            job_id=job_id,
            metadata=metadata,
            progress_callback=lambda prog, line: job_manager.update_job(
                job_id, progress=prog, current_line=line
            )
        )
        
        if result['success']:
            job_manager.update_job(
                job_id,
                status=JobStatus.COMPLETED,
                progress=1.0,
                current_line="Download completed",
                result_file=result.get('filename')
            )
        else:
            raise Exception(result.get('error', 'Unknown error'))
    
    except Exception as e:
        raise Exception(f"YouTube download failed: {str(e)}")


def detect_url_type(url: str) -> Platform:
    """Detect platform from URL"""
    if 'spotify.com' in url:
        return Platform.SPOTIFY
    elif 'jiosaavn.com' in url or 'saavn.com' in url:
        return Platform.JIOSAAVN
    else:
        return Platform.YOUTUBE


@app.get("/api/job/{job_id}", response_model=JobProgressResponse)
async def get_job_status(job_id: str):
    """Get status of a download job"""
    job = job_manager.get_job(job_id)
    if not job:
        raise HTTPException(status_code=404, detail="Job not found")
    
    return JobProgressResponse(
        job_id=job_id,
        status=job['status'],
        progress=job['progress'],
        current_line=job['current_line'],
        error=job.get('error'),
        result_file=job.get('result_file')
    )


@app.get("/api/job/{job_id}/events")
async def job_events(job_id: str):
    """Server-Sent Events endpoint for real-time job updates"""
    async def event_generator():
        job = job_manager.get_job(job_id)
        if not job:
            yield f"data: {{'error': 'Job not found'}}\n\n"
            return
        
        last_update = None
        while True:
            job = job_manager.get_job(job_id)
            if not job:
                break
            
            # Send update if changed
            current_update = (job['status'], job['progress'], job['current_line'])
            if current_update != last_update:
                import json
                data = {
                    'status': job['status'].value,
                    'progress': job['progress'],
                    'current_line': job['current_line'],
                    'error': job.get('error'),
                    'result_file': job.get('result_file')
                }
                yield f"data: {json.dumps(data)}\n\n"
                last_update = current_update
            
            # Stop if job is terminal
            if job['status'] in [JobStatus.COMPLETED, JobStatus.FAILED, JobStatus.CANCELLED]:
                break
            
            await asyncio.sleep(0.5)
    
    return StreamingResponse(event_generator(), media_type="text/event-stream")


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
