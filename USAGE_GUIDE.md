# Spowlo Music Downloader - Complete Usage Guide

This guide provides step-by-step instructions for setting up and using the Spowlo Music Downloader system (Backend + Android App).

## Table of Contents

1. [System Requirements](#system-requirements)
2. [Backend Setup](#backend-setup)
3. [Android App Setup](#android-app-setup)
4. [Usage Examples](#usage-examples)
5. [API Examples](#api-examples)
6. [Troubleshooting](#troubleshooting)

---

## System Requirements

### Backend Server
- Python 3.8 or higher
- ffmpeg installed
- 2GB RAM minimum
- Network access for API calls

### Android App
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or higher
- Android device/emulator running Android 13+ (API 33+)
- 4GB RAM minimum (for Android Studio)

---

## Backend Setup

### 1. Install Python Dependencies

```bash
cd backend
pip install -r requirements.txt
```

**Required packages:**
- fastapi
- uvicorn
- yt-dlp
- requests
- python-multipart
- pydantic
- aiofiles

### 2. Install ffmpeg

**Ubuntu/Debian:**
```bash
sudo apt-get update
sudo apt-get install ffmpeg
```

**macOS:**
```bash
brew install ffmpeg
```

**Windows:**
Download from https://ffmpeg.org/download.html and add to PATH

### 3. Configure Spotify API (Optional)

The backend includes default Spotify API credentials, but you can use your own:

1. Go to https://developer.spotify.com/dashboard
2. Create an app
3. Copy Client ID and Client Secret
4. Set environment variables:

```bash
export SPOTIFY_CLIENT_ID="your_client_id"
export SPOTIFY_CLIENT_SECRET="your_client_secret"
```

### 4. Start Backend Server

**Development mode:**
```bash
cd backend
python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

**Production mode:**
```bash
python -m uvicorn app.main:app --host 0.0.0.0 --port 8000 --workers 4
```

Server will start at: http://localhost:8000

**Verify it's running:**
```bash
curl http://localhost:8000/
```

Expected response:
```json
{
  "status": "online",
  "service": "Spowlo Music API",
  "version": "1.0.0"
}
```

---

## Android App Setup

### 1. Open Project in Android Studio

1. Launch Android Studio
2. Click "Open"
3. Navigate to the project root directory
4. Click "OK"
5. Wait for Gradle sync to complete (~5-10 minutes first time)

### 2. Configure Backend URL

Edit `app/src/main/java/com/spowlo/musicdownloader/SpowloApplication.kt`:

**For Android Emulator:**
```kotlin
RetrofitClient.setBaseUrl("http://10.0.2.2:8000/")
```

**For Physical Device (same WiFi network):**
```kotlin
// Replace with your computer's IP address
RetrofitClient.setBaseUrl("http://192.168.1.100:8000/")
```

**To find your computer's IP:**
- **Windows**: `ipconfig` (look for IPv4 Address)
- **macOS/Linux**: `ifconfig` or `ip addr show`

### 3. Build the App

**Option A: Using Android Studio**
1. Click "Build" > "Make Project" (or Ctrl+F9)
2. Wait for build to complete
3. Check "Build" tab for any errors

**Option B: Using Command Line**
```bash
# Debug build
./gradlew assembleDebug

# Release build (requires signing)
./gradlew assembleRelease
```

### 4. Run the App

**On Emulator:**
1. Click "Tools" > "Device Manager"
2. Create/Start an emulator (Android 13+ recommended)
3. Click "Run" button (▶️) or Shift+F10
4. Select your emulator

**On Physical Device:**
1. Enable Developer Options on your device:
   - Settings > About Phone
   - Tap "Build Number" 7 times
2. Enable USB Debugging:
   - Settings > Developer Options > USB Debugging
3. Connect device via USB
4. Click "Run" button (▶️) or Shift+F10
5. Select your device

---

## Usage Examples

### Example 1: Download from Spotify

1. **Start Backend Server**
   ```bash
   cd backend
   python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
   ```

2. **Open Android App**
   - Launch Spowlo app on your device

3. **Enter Spotify URL**
   ```
   https://open.spotify.com/track/3n3Ppam7vgaVa1iaRUc9Lp
   ```

4. **Fetch Metadata**
   - Click "Fetch Song Info" button
   - Wait for metadata to load
   - Review song details (title, artist, album, duration)

5. **Select Quality**
   - Choose "M4A 320 kbps (Best Quality)"
   - Or select your preferred quality

6. **Download**
   - Click "Download" button
   - App automatically navigates to Downloads tab
   - Watch real-time progress

### Example 2: Download from JioSaavn

1. **Enter JioSaavn URL**
   ```
   https://www.jiosaavn.com/song/tum-hi-ho/GQsZfSh5aGU
   ```

2. **Follow Steps 4-6** from Example 1

### Example 3: Download from YouTube

1. **Enter YouTube URL**
   ```
   https://www.youtube.com/watch?v=kJQP7kiw5Fk
   ```

2. **Note**: YouTube metadata fetching is not supported
   - Skip metadata fetch
   - Directly select quality and download

3. **Download will use YouTube search** or direct video URL

---

## API Examples

### Test Backend API Using curl

#### 1. Health Check
```bash
curl http://localhost:8000/
```

#### 2. Get Spotify Metadata
```bash
curl http://localhost:8000/api/metadata/spotify/3n3Ppam7vgaVa1iaRUc9Lp
```

Response:
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

#### 3. Get JioSaavn Metadata
```bash
curl http://localhost:8000/api/metadata/jiosaavn/GQsZfSh5aGU
```

#### 4. Start Download
```bash
curl -X POST http://localhost:8000/api/download \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://open.spotify.com/track/3n3Ppam7vgaVa1iaRUc9Lp",
    "quality": "m4a_320",
    "metadata": {
      "id": "3n3Ppam7vgaVa1iaRUc9Lp",
      "title": "Mr. Brightside",
      "artists": ["The Killers"],
      "album": "Hot Fuss",
      "thumbnailUrl": "https://...",
      "duration": 222,
      "platform": "spotify"
    }
  }'
```

Response:
```json
{
  "job_id": "uuid-here",
  "status": "started",
  "message": "Download job started successfully"
}
```

#### 5. Check Job Status
```bash
curl http://localhost:8000/api/job/uuid-here
```

Response:
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

---

## Troubleshooting

### Backend Issues

#### Issue: "Module not found" error
**Solution:**
```bash
pip install --upgrade -r requirements.txt
```

#### Issue: "ffmpeg not found"
**Solution:**
- Install ffmpeg (see Backend Setup step 2)
- Verify: `ffmpeg -version`

#### Issue: "Port already in use"
**Solution:**
```bash
# Find process using port 8000
lsof -i :8000  # macOS/Linux
netstat -ano | findstr :8000  # Windows

# Kill the process or use different port
python -m uvicorn app.main:app --host 0.0.0.0 --port 8001
```

#### Issue: Spotify API authentication fails
**Solution:**
- Use provided default credentials, or
- Set your own credentials (see Backend Setup step 3)

### Android App Issues

#### Issue: "Unable to connect to backend"
**Solution:**

1. **Check backend is running:**
   ```bash
   curl http://localhost:8000/
   ```

2. **Emulator - Use correct URL:**
   ```kotlin
   RetrofitClient.setBaseUrl("http://10.0.2.2:8000/")
   ```

3. **Physical Device - Check same network:**
   - Computer and phone on same WiFi
   - Use computer's local IP
   - Check firewall not blocking port 8000

4. **Test connection from device:**
   - Open Chrome on device
   - Navigate to `http://YOUR_IP:8000`
   - Should see API response

#### Issue: Gradle sync fails
**Solution:**
```bash
# Clean project
./gradlew clean

# Refresh dependencies
./gradlew --refresh-dependencies

# Rebuild
./gradlew build
```

#### Issue: "SDK not found" or "Build Tools not found"
**Solution:**
1. Android Studio > Settings > Android SDK
2. Check "Android 13.0 (API 33)" and "Android 14.0 (API 34)"
3. Click "Apply" to install

#### Issue: App crashes on launch
**Solution:**
1. Check Logcat in Android Studio
2. Common causes:
   - Network permission not granted
   - Backend URL incorrect
   - Room database migration issue

**Fix database issues:**
```kotlin
// In AppDatabase.kt, temporarily add:
.fallbackToDestructiveMigration()
```

#### Issue: Downloads not saving
**Solution:**
1. Grant storage permission:
   - Settings > Apps > Spowlo > Permissions > Storage
2. For Android 13+, grant "Music and audio" permission

#### Issue: Images not loading
**Solution:**
1. Check internet connection
2. Verify Coil dependency in build.gradle
3. Check network security config allows HTTP (if using HTTP)

### Network Issues

#### Issue: CORS errors in browser testing
**Solution:** Backend already has CORS enabled for all origins:
```python
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)
```

#### Issue: SSL/TLS errors
**Solution:**
- Backend uses HTTP by default (suitable for local development)
- For production, use HTTPS with proper certificates
- Update Android network security config if needed

---

## Quality Comparison

| Quality | Format | Bitrate | File Size (4 min song) | Use Case |
|---------|--------|---------|------------------------|----------|
| M4A 320 | M4A/AAC | 320 kbps | ~9-10 MB | Best quality, audiophile |
| Opus 160 | Opus | 160 kbps | ~4-5 MB | High efficiency, good quality |
| Best Available | Variable | Variable | Varies | Keep original, no conversion |

---

## Support URLs

### Spotify
- Track: `https://open.spotify.com/track/{track_id}`
- Example: `https://open.spotify.com/track/3n3Ppam7vgaVa1iaRUc9Lp`

### JioSaavn
- Song: `https://www.jiosaavn.com/song/{song-name}/{song_id}`
- Example: `https://www.jiosaavn.com/song/tum-hi-ho/GQsZfSh5aGU`

### YouTube
- Video: `https://www.youtube.com/watch?v={video_id}`
- Example: `https://www.youtube.com/watch?v=kJQP7kiw5Fk`

---

## Additional Resources

- Backend API Docs: `http://localhost:8000/docs` (Swagger UI)
- Backend Redoc: `http://localhost:8000/redoc`
- Android Developer Docs: https://developer.android.com/
- Jetpack Compose Docs: https://developer.android.com/jetpack/compose

---

## Getting Help

If you encounter issues:

1. Check this troubleshooting guide
2. Review logs:
   - Backend: Terminal output
   - Android: Logcat in Android Studio
3. Verify all dependencies are installed
4. Ensure backend and app are configured correctly
5. Test API endpoints with curl first

---

## Notes

- **Development Only**: Current setup is for development/testing
- **Production**: For production, add:
  - HTTPS/SSL
  - Authentication/Authorization
  - Rate limiting
  - Error monitoring
  - Database persistence (Redis for job queue)
  - File storage service (S3, etc.)
  - Proper app signing and release build
