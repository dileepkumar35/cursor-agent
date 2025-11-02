"""Audio Downloader - Replicates logic from spoti-down-m4a-opus-v4.2.py"""
import os
import asyncio
import yt_dlp
import subprocess
from typing import Optional, Dict, Callable
from dataclasses import dataclass


@dataclass
class QualityPreset:
    name: str
    codec: str
    bitrate: Optional[str]


class AudioDownloader:
    """Download audio with quality selection options"""
    
    QUALITY_PRESETS = {
        '1': QualityPreset(name='M4A 320kbps (Best Quality)', codec='m4a', bitrate='320'),
        '2': QualityPreset(name='Opus 160kbps (High Efficiency)', codec='opus', bitrate='160'),
        '3': QualityPreset(name='Best Available (No Conversion)', codec='best', bitrate=None),
    }
    
    def __init__(self, output_dir: str = "downloads"):
        self.output_dir = output_dir
        os.makedirs(output_dir, exist_ok=True)
    
    def get_download_options(self, quality_choice: str) -> Dict:
        """Get yt-dlp options based on quality selection"""
        preset = self.QUALITY_PRESETS.get(quality_choice, self.QUALITY_PRESETS['1'])
        
        ydl_opts = {
            'format': 'bestaudio/best',
            'outtmpl': f'{self.output_dir}/%(title)s.%(ext)s',
            'quiet': False,
            'no_warnings': False,
            'keepvideo': True,
        }
        
        if preset.codec == 'best':
            # No conversion, keep original format
            ydl_opts['postprocessors'] = []
        else:
            # Convert to selected format
            postprocessor = {
                'key': 'FFmpegExtractAudio',
                'preferredcodec': preset.codec,
            }
            
            if preset.bitrate:
                postprocessor['preferredquality'] = preset.bitrate
            
            ydl_opts['postprocessors'] = [postprocessor]
        
        return ydl_opts
    
    async def download_from_url(
        self,
        url: str,
        quality_choice: str = '1',
        job_id: Optional[str] = None,
        progress_callback: Optional[Callable] = None
    ) -> Dict:
        """Download audio from direct URL (JioSaavn, YouTube, etc.)"""
        try:
            ydl_opts = self.get_download_options(quality_choice)
            
            # Add progress hook
            def progress_hook(d):
                if progress_callback and d['status'] == 'downloading':
                    try:
                        percent = d.get('_percent_str', '0%').strip('%')
                        progress = float(percent) / 100.0
                        line = f"Downloading: {d.get('_percent_str', '0%')} - {d.get('_speed_str', 'N/A')}"
                        progress_callback(progress, line)
                    except:
                        pass
                elif progress_callback and d['status'] == 'finished':
                    progress_callback(0.9, "Processing...")
            
            ydl_opts['progress_hooks'] = [progress_hook]
            ydl_opts['extractor_args'] = {'youtube': {'player_client': ['android', 'web']}}
            
            # Run in executor to avoid blocking
            loop = asyncio.get_event_loop()
            result = await loop.run_in_executor(
                None,
                self._download_sync,
                url,
                ydl_opts,
                progress_callback
            )
            
            return result
            
        except Exception as e:
            return {'success': False, 'error': str(e)}
    
    def _download_sync(
        self,
        url: str,
        ydl_opts: Dict,
        progress_callback: Optional[Callable]
    ) -> Dict:
        """Synchronous download helper"""
        try:
            with yt_dlp.YoutubeDL(ydl_opts) as ydl:
                info = ydl.extract_info(url, download=True)
                
                # Get downloaded filename
                if 'requested_downloads' in info and info['requested_downloads']:
                    filename = info['requested_downloads'][0]['filepath']
                else:
                    # Construct filename from template
                    title = info.get('title', 'Unknown')
                    ext = info.get('ext', 'opus')
                    filename = f"{self.output_dir}/{title}.{ext}"
                
                return {
                    'success': True,
                    'filename': filename,
                    'title': info.get('title', 'Unknown')
                }
        except Exception as e:
            return {'success': False, 'error': str(e)}
    
    async def download_youtube(
        self,
        url: str,
        quality_choice: str = '1',
        job_id: Optional[str] = None,
        metadata: Optional[Dict] = None,
        progress_callback: Optional[Callable] = None
    ) -> Dict:
        """Download from YouTube with optional metadata embedding"""
        try:
            result = await self.download_from_url(
                url=url,
                quality_choice=quality_choice,
                job_id=job_id,
                progress_callback=progress_callback
            )
            
            if result['success'] and metadata:
                # Embed metadata using ffmpeg
                if progress_callback:
                    progress_callback(0.95, "Embedding metadata...")
                
                result = await self._embed_metadata(
                    result['filename'],
                    metadata,
                    progress_callback
                )
            
            return result
            
        except Exception as e:
            return {'success': False, 'error': str(e)}
    
    async def _embed_metadata(
        self,
        audio_file: str,
        metadata: Dict,
        progress_callback: Optional[Callable] = None
    ) -> Dict:
        """Embed metadata and cover art into audio file using ffmpeg"""
        try:
            if not os.path.exists(audio_file):
                return {'success': False, 'error': 'Audio file not found'}
            
            # Download cover art if available
            cover_file = None
            thumbnail_url = metadata.get('thumbnailUrl')
            if thumbnail_url:
                try:
                    import requests
                    cover_file = f"{self.output_dir}/cover_temp.jpg"
                    response = requests.get(thumbnail_url, timeout=10)
                    response.raise_for_status()
                    with open(cover_file, 'wb') as f:
                        f.write(response.content)
                except Exception as e:
                    print(f"Failed to download cover art: {e}")
                    cover_file = None
            
            # Prepare output file
            base, ext = os.path.splitext(audio_file)
            output_file = f"{base}_tagged{ext}"
            
            # Build ffmpeg command
            cmd = ['ffmpeg', '-i', audio_file, '-y']
            
            # Add cover art if available
            if cover_file and os.path.exists(cover_file):
                cmd.extend(['-i', cover_file, '-map', '0', '-map', '1', '-c', 'copy'])
                cmd.extend(['-disposition:v', 'attached_pic'])
            else:
                cmd.extend(['-c', 'copy'])
            
            # Add metadata
            if metadata.get('title'):
                cmd.extend(['-metadata', f"title={metadata['title']}"])
            if metadata.get('artists'):
                artists_str = ', '.join(metadata['artists'])
                cmd.extend(['-metadata', f"artist={artists_str}"])
            if metadata.get('album'):
                cmd.extend(['-metadata', f"album={metadata['album']}"])
            
            cmd.append(output_file)
            
            # Run ffmpeg
            loop = asyncio.get_event_loop()
            await loop.run_in_executor(
                None,
                subprocess.run,
                cmd,
                {'capture_output': True, 'check': True}
            )
            
            # Clean up
            if cover_file and os.path.exists(cover_file):
                os.remove(cover_file)
            
            # Replace original with tagged version
            if os.path.exists(output_file):
                os.remove(audio_file)
                os.rename(output_file, audio_file)
            
            if progress_callback:
                progress_callback(1.0, "Metadata embedded successfully")
            
            return {'success': True, 'filename': audio_file}
            
        except Exception as e:
            print(f"Failed to embed metadata: {e}")
            # Return success with original file if metadata embedding fails
            return {'success': True, 'filename': audio_file}
