# Spowlo Backend API

FastAPI backend that replicates the functionality from `spoti-down-m4a-opus-v4.2.py`.

## Features

- **Spotify Integration**: Fetch track metadata using Spotify API
- **JioSaavn Support**: Download and extract metadata from JioSaavn
- **YouTube Fallback**: Search and download from YouTube when needed
- **Quality Selection**: M4A 320kbps / Opus 160kbps / Best Available
- **Metadata Embedding**: Automatically embed cover art and metadata using ffmpeg
- **Job Management**: Track download progress with real-time updates
- **REST API**: Complete REST endpoints for Android app integration

## Installation

```bash
# Install Python dependencies
pip install -r requirements.txt

# Install ffmpeg (required for audio processing)
# Ubuntu/Debian:
sudo apt-get install ffmpeg

# macOS:
brew install ffmpeg
```

## Running the Server

```bash
# Development mode
python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000

# Production mode
python -m uvicorn app.main:app --host 0.0.0.0 --port 8000 --workers 4
```

## API Endpoints

### Health Check
```
GET /
```

### Get Spotify Metadata
```
GET /api/metadata/spotify/{track_id}
```

**Example Response:**
```json
{
  "id": "3n3Ppam7vgaVa1iaRUc9Lp",
  "title": "Mr. Brightside",
  "artists": ["The Killers"],
  "album": "Hot Fuss",
  "thumbnailUrl": "https://...",
  "duration": 222,
  "platform": "spotify"
}
```

### Get JioSaavn Metadata
```
GET /api/metadata/jiosaavn/{song_id}
```

### Start Download
```
POST /api/download
```

**Request Body:**
```json
{
  "url": "https://open.spotify.com/track/...",
  "quality": "m4a_320",
  "metadata": {
    "id": "...",
    "title": "Song Title",
    "artists": ["Artist 1", "Artist 2"],
    "album": "Album Name",
    "thumbnailUrl": "https://...",
    "duration": 180,
    "platform": "spotify"
  }
}
```

**Response:**
```json
{
  "job_id": "uuid-here",
  "status": "started",
  "message": "Download job started successfully"
}
```

### Get Job Status
```
GET /api/job/{job_id}
```

**Response:**
```json
{
  "job_id": "uuid-here",
  "status": "processing",
  "progress": 0.65,
  "current_line": "Downloading: 65% - 1.2MB/s",
  "error": null,
  "result_file": null
}
```

### Real-Time Job Updates (SSE)
```
GET /api/job/{job_id}/events
```

Returns Server-Sent Events stream with real-time progress updates.

## Quality Options

- `m4a_320`: M4A format at 320kbps (best quality)
- `opus_160`: Opus format at 160kbps (high efficiency)
- `best`: Best available format without conversion

## Architecture

```
backend/
├── app/
│   ├── main.py              # FastAPI application and routes
│   ├── spotify_client.py    # Spotify API client
│   ├── jiosaavn_client.py   # JioSaavn API client
│   ├── downloader.py        # Audio downloader with yt-dlp
│   └── job_manager.py       # Job tracking and management
├── requirements.txt         # Python dependencies
└── README.md               # This file
```

## Environment Variables

You can override default Spotify credentials:

```bash
export SPOTIFY_CLIENT_ID="your_client_id"
export SPOTIFY_CLIENT_SECRET="your_client_secret"
```

## Notes

- Downloads are stored in `backend/downloads/` directory
- FFmpeg is required for metadata embedding
- Server supports CORS for Android app integration
- Job history is kept in memory (use Redis for production)
