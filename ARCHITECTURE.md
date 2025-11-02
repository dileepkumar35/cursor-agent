# Spowlo Music Downloader - System Architecture

Complete architecture documentation for the Spowlo Music Downloader system.

## System Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    ANDROID APPLICATION                       │
│  ┌──────────────────────────────────────────────────────┐  │
│  │                 Presentation Layer                    │  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  │  │
│  │  │ Home Screen │  │  Downloads  │  │   History   │  │  │
│  │  │             │  │   Screen    │  │   Screen    │  │  │
│  │  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  │  │
│  │         │                │                │          │  │
│  │         └────────────────┴────────────────┘          │  │
│  │                         │                            │  │
│  │  ┌──────────────────────▼──────────────────────────┐ │  │
│  │  │           ViewModels (MVVM)                     │ │  │
│  │  │  • HomeViewModel                                │ │  │
│  │  │  • DownloadViewModel                            │ │  │
│  │  │  • HistoryViewModel                             │ │  │
│  │  └──────────────────────┬──────────────────────────┘ │  │
│  └────────────────────────┬┬────────────────────────────┘  │
│                           ││                                │
│  ┌────────────────────────▼▼────────────────────────────┐  │
│  │                  Domain Layer                         │  │
│  │  • Business Logic                                     │  │
│  │  • Domain Models (DownloadItem, DownloadStatus)      │  │
│  │  • Use Cases                                          │  │
│  └────────────────────────┬──────────────────────────────┘  │
│                           │                                 │
│  ┌────────────────────────▼──────────────────────────────┐ │
│  │                   Data Layer                          │ │
│  │  ┌──────────────────────────────────────────────────┐ │ │
│  │  │           Repository Pattern                     │ │ │
│  │  │      (MusicRepository)                           │ │ │
│  │  └──────────────┬───────────────┬───────────────────┘ │ │
│  │                 │               │                     │ │
│  │     ┌───────────▼──────┐    ┌──▼──────────────┐     │ │
│  │     │  Local Source    │    │  Remote Source  │     │ │
│  │     │  (Room DB)       │    │  (Retrofit)     │     │ │
│  │     │  • DownloadDao   │    │  • API Service  │     │ │
│  │     │  • AppDatabase   │    │  • RetrofitClient│    │ │
│  │     └──────────────────┘    └─────────┬────────┘     │ │
│  └──────────────────────────────────────┬┬──────────────┘ │
└─────────────────────────────────────────┼┼────────────────┘
                                          ││
                    Network (HTTP/REST)   ││
                                          ││
┌─────────────────────────────────────────▼▼────────────────────┐
│                    BACKEND API SERVER                          │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │                 FastAPI Application                       │ │
│  │  ┌─────────────────────────────────────────────────────┐ │ │
│  │  │                API Routes                            │ │ │
│  │  │  • GET  /                     (Health Check)        │ │ │
│  │  │  • GET  /api/metadata/spotify/{id}                  │ │ │
│  │  │  • GET  /api/metadata/jiosaavn/{id}                 │ │ │
│  │  │  • POST /api/download                               │ │ │
│  │  │  • GET  /api/job/{job_id}                           │ │ │
│  │  │  • GET  /api/job/{job_id}/events (SSE)             │ │ │
│  │  └─────────────────┬────────────────────────────────────┘ │ │
│  └────────────────────┼─────────────────────────────────────┘ │
│                       │                                        │
│  ┌────────────────────▼─────────────────────────────────────┐ │
│  │              Business Logic Layer                        │ │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │ │
│  │  │ Spotify API  │  │ JioSaavn API │  │ Job Manager  │  │ │
│  │  │   Client     │  │    Client    │  │              │  │ │
│  │  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  │ │
│  │         │                  │                 │          │ │
│  │         └──────────┬───────┴─────────────────┘          │ │
│  │                    │                                     │ │
│  │         ┌──────────▼──────────┐                         │ │
│  │         │  Audio Downloader   │                         │ │
│  │         │  (yt-dlp wrapper)   │                         │ │
│  │         └──────────┬──────────┘                         │ │
│  └────────────────────┼─────────────────────────────────────┘ │
│                       │                                        │
│  ┌────────────────────▼─────────────────────────────────────┐ │
│  │              External Services                           │ │
│  │  • yt-dlp (YouTube downloader)                           │ │
│  │  • ffmpeg (Audio processing & metadata embedding)        │ │
│  │  • Spotify Web API                                       │ │
│  │  • JioSaavn API                                          │ │
│  └──────────────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────────────┘
```

---

## Android App Architecture

### MVVM Pattern Implementation

```
┌───────────────────────────────────────────────────┐
│                    VIEW LAYER                      │
│   (Jetpack Compose UI)                            │
│                                                    │
│   HomeScreen.kt      DownloadScreen.kt            │
│   HistoryScreen.kt   Components/                  │
│                                                    │
└─────────────────┬──────────────────────────────────┘
                  │ observes State
                  │
┌─────────────────▼──────────────────────────────────┐
│               VIEWMODEL LAYER                      │
│   (State Management & Business Logic)             │
│                                                    │
│   HomeViewModel      DownloadViewModel            │
│   HistoryViewModel                                │
│                                                    │
│   • Holds UI State (StateFlow)                    │
│   • Handles User Actions                          │
│   • Coordinates Repository calls                  │
│                                                    │
└─────────────────┬──────────────────────────────────┘
                  │ calls
                  │
┌─────────────────▼──────────────────────────────────┐
│              REPOSITORY LAYER                      │
│   (Single Source of Truth)                        │
│                                                    │
│   MusicRepository                                 │
│                                                    │
│   • Coordinates Local & Remote data               │
│   • Handles data caching                          │
│   • Provides clean API to ViewModels              │
│                                                    │
└──────────────┬───────────────┬─────────────────────┘
               │               │
       ┌───────▼──────┐    ┌──▼──────────────┐
       │ Local Data   │    │  Remote Data    │
       │   Source     │    │    Source       │
       │              │    │                 │
       │ Room DB      │    │ Retrofit API    │
       │ • AppDatabase│    │ • SpowloApiService│
       │ • DownloadDao│    │ • RetrofitClient│
       │ • Entities   │    │ • API Models    │
       └──────────────┘    └─────────────────┘
```

### Data Flow

#### Download Flow:
```
1. User enters URL in HomeScreen
2. HomeScreen calls HomeViewModel.fetchMetadata()
3. HomeViewModel calls Repository.getSpotifyMetadata()
4. Repository calls Remote API via Retrofit
5. API returns TrackMetadata
6. Repository returns Result to ViewModel
7. ViewModel updates UI State
8. HomeScreen observes state and displays metadata

9. User selects quality and clicks Download
10. HomeViewModel calls Repository.startDownload()
11. Repository calls Remote API to start job
12. API returns job_id
13. Repository inserts DownloadEntity in Room DB
14. ViewModel updates state and navigates to Downloads

15. DownloadViewModel starts polling job status
16. Repository.pollJobStatus() calls API repeatedly
17. Updates are written to Room DB
18. DownloadViewModel observes Room DB changes
19. DownloadScreen displays real-time progress
20. When complete, status updates in both DB and UI
```

---

## Backend Architecture

### FastAPI Application Structure

```
backend/
├── app/
│   ├── __init__.py
│   ├── main.py                 # FastAPI app & routes
│   │   ├── CORS middleware
│   │   ├── Route definitions
│   │   └── Request/Response models
│   │
│   ├── spotify_client.py       # Spotify API integration
│   │   ├── Authentication
│   │   ├── Track info fetching
│   │   └── Search functionality
│   │
│   ├── jiosaavn_client.py      # JioSaavn API integration
│   │   ├── Song details fetching
│   │   ├── URL parsing
│   │   └── Metadata extraction
│   │
│   ├── downloader.py           # Audio download logic
│   │   ├── yt-dlp wrapper
│   │   ├── Quality selection
│   │   ├── Progress tracking
│   │   └── Metadata embedding (ffmpeg)
│   │
│   └── job_manager.py          # Job tracking system
│       ├── Job creation
│       ├── Status updates
│       ├── Progress tracking
│       └── Error handling
│
├── downloads/                  # Downloaded files (gitignored)
├── requirements.txt
└── README.md
```

### Request Processing Flow

```
Client Request
     │
     ▼
┌─────────────────┐
│  FastAPI Route  │
│   (Endpoint)    │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Validation     │  (Pydantic models)
│  & Parsing      │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Business Logic │
│  (Service Layer)│
│  • Spotify API  │
│  • JioSaavn API │
│  • Downloader   │
│  • Job Manager  │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ External Services│
│  • yt-dlp       │
│  • ffmpeg       │
│  • Spotify Web  │
│  • JioSaavn     │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Response       │
│  Formation      │
└────────┬────────┘
         │
         ▼
   Client Response
```

---

## Data Models

### Android App Models

#### Domain Models
```kotlin
// DownloadItem.kt
data class DownloadItem(
    val jobId: String,
    val title: String,
    val artist: String,
    val album: String?,
    val coverImageUrl: String?,
    val quality: String,
    val platform: String,
    val status: DownloadStatus,
    val progress: Float,
    val currentLine: String,
    val error: String?,
    val resultFile: String?,
    val timestamp: Long
)

enum class DownloadStatus {
    PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED
}
```

#### Database Models (Room)
```kotlin
// DownloadEntity.kt
@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val jobId: String,
    val title: String,
    val artist: String,
    val album: String?,
    val coverImageUrl: String?,
    val quality: String,
    val platform: String,
    val status: String,
    val progress: Float,
    val currentLine: String,
    val error: String?,
    val resultFile: String?,
    val downloadUrl: String,
    val timestamp: Long
)
```

#### API Models
```kotlin
// TrackMetadata
data class TrackMetadata(
    val id: String,
    val title: String,
    val artists: List<String>,
    val album: String?,
    val thumbnailUrl: String?,
    val duration: Int?,
    val platform: String
)

// DownloadRequest
data class DownloadRequest(
    val url: String,
    val quality: String,
    val metadata: TrackMetadata?
)

// DownloadResponse
data class DownloadResponse(
    val jobId: String,
    val status: String,
    val message: String
)

// JobProgressResponse
data class JobProgressResponse(
    val jobId: String,
    val status: String,
    val progress: Float,
    val currentLine: String,
    val error: String?,
    val resultFile: String?
)
```

### Backend Models

```python
# Pydantic Models in main.py
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
```

---

## Technology Stack

### Android App

| Layer | Technology | Purpose |
|-------|------------|---------|
| UI | Jetpack Compose | Declarative UI framework |
| Architecture | MVVM | Separation of concerns |
| Navigation | Navigation Compose | Screen navigation |
| Networking | Retrofit 2 | REST API client |
| HTTP Client | OkHttp | Network operations |
| JSON | Gson | JSON parsing |
| Database | Room | Local persistence |
| Async | Kotlin Coroutines | Background operations |
| Reactive | Kotlin Flow | Data streams |
| Images | Coil | Image loading & caching |
| Dependency Injection | Manual | Simplicity |

### Backend

| Component | Technology | Purpose |
|-----------|------------|---------|
| Framework | FastAPI | High-performance async API |
| Server | Uvicorn | ASGI server |
| Downloader | yt-dlp | Audio extraction |
| Audio Processing | ffmpeg | Format conversion & metadata |
| API Clients | requests | HTTP requests |
| Data Validation | Pydantic | Request/response models |
| Async I/O | asyncio | Concurrent operations |
| File Handling | aiofiles | Async file operations |

---

## Communication Protocol

### REST API Endpoints

```
┌────────────────────────────────────────────────────────────┐
│  Endpoint                    Method   Purpose              │
├────────────────────────────────────────────────────────────┤
│  /                           GET      Health check         │
│  /api/metadata/spotify/{id}  GET      Get track metadata  │
│  /api/metadata/jiosaavn/{id} GET      Get song metadata   │
│  /api/download               POST     Start download      │
│  /api/job/{job_id}           GET      Get job status     │
│  /api/job/{job_id}/events    GET      SSE progress stream│
└────────────────────────────────────────────────────────────┘
```

### Request/Response Flow

```
Android App                                Backend API
     │                                          │
     │  1. POST /api/download                   │
     ├──────────────────────────────────────────>│
     │  {url, quality, metadata}                │
     │                                          │
     │  2. Response: {job_id, status}           │
     │<────────────────────────────────────────┤
     │                                          │
     │  3. GET /api/job/{job_id} (polling)      │
     ├──────────────────────────────────────────>│
     │                                          │
     │  4. Response: {progress, status, ...}    │
     │<────────────────────────────────────────┤
     │                                          │
     │  (Repeat polling every 1 second)         │
     │                                          │
     │  5. Final Response: {status: completed}  │
     │<────────────────────────────────────────┤
     │                                          │
```

---

## Scalability Considerations

### Current Implementation (Development)
- In-memory job storage
- Single-threaded job processing
- Local file storage
- No authentication

### Production Recommendations

#### 1. Job Queue
```python
# Replace in-memory with Redis
import redis
from rq import Queue

redis_conn = redis.Redis()
queue = Queue(connection=redis_conn)

# Enqueue job
job = queue.enqueue(process_download, job_id, url, quality)
```

#### 2. Database Persistence
```python
# Replace in-memory job storage
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

# Use PostgreSQL for job tracking
DATABASE_URL = "postgresql://user:pass@localhost/spowlo"
```

#### 3. File Storage
```python
# Use S3 or similar for file storage
import boto3

s3 = boto3.client('s3')
s3.upload_file(local_file, 'bucket-name', 'key')
```

#### 4. Caching
```python
# Add Redis caching for metadata
@cache.cached(timeout=3600, key_prefix='spotify_metadata')
def get_spotify_metadata(track_id):
    pass
```

#### 5. Load Balancing
```
Client → Load Balancer → [API Server 1]
                       → [API Server 2]
                       → [API Server 3]
                            ↓
                       Redis Queue
                            ↓
                   [Worker 1] [Worker 2] [Worker 3]
```

---

## Security Considerations

### Android App
- ✅ Uses HTTPS in production
- ✅ Network security config
- ✅ ProGuard obfuscation
- ✅ No hardcoded secrets (use env vars)
- ⚠️ Add API key authentication
- ⚠️ Add certificate pinning

### Backend
- ✅ CORS configured
- ✅ Request validation (Pydantic)
- ⚠️ Add rate limiting
- ⚠️ Add authentication/authorization
- ⚠️ Add input sanitization
- ⚠️ Add API key validation
- ⚠️ Use HTTPS in production

---

## Testing Strategy

### Android App Tests
```kotlin
// Unit Tests (ViewModels)
class HomeViewModelTest {
    @Test
    fun testFetchMetadata_Success() { }
    
    @Test
    fun testStartDownload_CreatesJob() { }
}

// Integration Tests (Repository)
class MusicRepositoryTest {
    @Test
    fun testGetSpotifyMetadata_ReturnsData() { }
}

// UI Tests (Compose)
@Composable
class HomeScreenTest {
    @Test
    fun testUrlInput_DisplaysCorrectly() { }
}
```

### Backend Tests
```python
# Unit Tests
def test_detect_url_type_spotify():
    assert detect_url_type("spotify.com/track/123") == Platform.SPOTIFY

# Integration Tests
async def test_download_endpoint():
    response = await client.post("/api/download", json={...})
    assert response.status_code == 200
    assert "job_id" in response.json()
```

---

## Performance Optimization

### Android App
- Lazy loading with Compose
- Image caching with Coil
- Database queries on background thread
- Pagination for large lists
- Memory leak prevention (ViewModel scope)

### Backend
- Async I/O operations
- Connection pooling
- Response compression
- Caching frequently requested data
- Background task processing

---

## Monitoring & Logging

### Android App
```kotlin
// Timber for logging
Timber.d("Download started: $jobId")
Timber.e(exception, "Download failed")

// Firebase Crashlytics
FirebaseCrashlytics.getInstance().recordException(exception)
```

### Backend
```python
# Python logging
import logging

logging.info(f"Job {job_id} started")
logging.error(f"Download failed: {error}")

# Add structured logging
logger.info("download_started", extra={
    "job_id": job_id,
    "url": url,
    "quality": quality
})
```

---

## Conclusion

This architecture provides:
- ✅ Clean separation of concerns
- ✅ Testable components
- ✅ Scalable design
- ✅ Maintainable codebase
- ✅ Type-safe implementations
- ✅ Modern best practices

The system is designed to be extended and scaled as requirements grow.
