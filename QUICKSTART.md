# Spowlo Music Downloader - Quick Start Guide

Get up and running in 5 minutes! ⚡

---

## 🎯 Prerequisites Checklist

Before starting, ensure you have:

- [ ] Python 3.8+ installed
- [ ] ffmpeg installed
- [ ] Android Studio Hedgehog or later
- [ ] JDK 17 or later
- [ ] Git (for cloning)

---

## 🚀 Step 1: Clone Repository

```bash
git clone https://github.com/dileepkumar35/cursor-agent.git
cd cursor-agent
```

---

## 🐍 Step 2: Start Backend (5 minutes)

### Install Dependencies
```bash
cd backend
pip install -r requirements.txt
```

### Install ffmpeg
**macOS:**
```bash
brew install ffmpeg
```

**Ubuntu/Debian:**
```bash
sudo apt-get update && sudo apt-get install ffmpeg
```

**Windows:**
Download from https://ffmpeg.org/download.html and add to PATH

### Start Server
```bash
python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

### Verify Backend
Open browser: http://localhost:8000

You should see:
```json
{
  "status": "online",
  "service": "Spowlo Music API",
  "version": "1.0.0"
}
```

✅ Backend is ready!

---

## 📱 Step 3: Setup Android App (5 minutes)

### Open in Android Studio
1. Launch Android Studio
2. Click "Open"
3. Select the project root directory
4. Wait for Gradle sync (~5 minutes first time)

### Configure Backend URL

Edit `app/src/main/java/com/spowlo/musicdownloader/SpowloApplication.kt`:

**For Android Emulator:**
```kotlin
RetrofitClient.setBaseUrl("http://10.0.2.2:8000/")
```

**For Physical Device:**
1. Find your computer's IP:
   - Windows: `ipconfig`
   - macOS/Linux: `ifconfig` or `ip addr`
2. Update URL:
   ```kotlin
   RetrofitClient.setBaseUrl("http://YOUR_IP_HERE:8000/")
   ```
   Example: `http://192.168.1.100:8000/`

### Run the App
1. Click Run (▶️) button or press Shift+F10
2. Select emulator or connected device
3. Wait for build and installation (~2 minutes first time)

✅ App is running!

---

## 🎵 Step 4: Test Download (2 minutes)

### In the Android App

1. **Paste a Spotify URL:**
   ```
   https://open.spotify.com/track/3n3Ppam7vgaVa1iaRUc9Lp
   ```

2. **Click "Fetch Song Info"**
   - Wait for metadata to load
   - You'll see song title, artist, album, and cover art

3. **Select Quality**
   - Choose "M4A 320 kbps (Best Quality)"

4. **Click "Download"**
   - App navigates to Downloads tab
   - Watch real-time progress
   - When complete, check History tab

---

## 🧪 Alternative: Test Backend Only (1 minute)

### Test with curl

**Health Check:**
```bash
curl http://localhost:8000/
```

**Get Metadata:**
```bash
curl http://localhost:8000/api/metadata/spotify/3n3Ppam7vgaVa1iaRUc9Lp
```

**Start Download:**
```bash
curl -X POST http://localhost:8000/api/download \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://open.spotify.com/track/3n3Ppam7vgaVa1iaRUc9Lp",
    "quality": "m4a_320"
  }'
```

**Check Job Status (replace with your job_id):**
```bash
curl http://localhost:8000/api/job/YOUR_JOB_ID_HERE
```

---

## 📋 Supported URLs

### Spotify
```
https://open.spotify.com/track/3n3Ppam7vgaVa1iaRUc9Lp
```

### JioSaavn
```
https://www.jiosaavn.com/song/tum-hi-ho/GQsZfSh5aGU
```

### YouTube
```
https://www.youtube.com/watch?v=kJQP7kiw5Fk
```

---

## 🎨 Quality Options

| Quality | Format | Bitrate | File Size | Use Case |
|---------|--------|---------|-----------|----------|
| M4A 320 | M4A/AAC | 320 kbps | ~9-10 MB | Best quality |
| Opus 160 | Opus | 160 kbps | ~4-5 MB | High efficiency |
| Best | Variable | Variable | Varies | Keep original |

---

## ❓ Troubleshooting Quick Fixes

### Backend Won't Start

**Issue**: Port 8000 already in use
```bash
# Use different port
python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8001
```

**Issue**: ffmpeg not found
```bash
# Verify installation
ffmpeg -version

# If not installed, see Step 2
```

### Android App Can't Connect

**Issue**: Connection refused (Emulator)
```kotlin
// Make sure you're using 10.0.2.2, not localhost
RetrofitClient.setBaseUrl("http://10.0.2.2:8000/")
```

**Issue**: Connection timeout (Physical Device)
1. Ensure phone and computer on same WiFi
2. Check firewall not blocking port 8000
3. Test backend from phone browser: `http://YOUR_IP:8000`

**Issue**: Gradle sync fails
```bash
# Clean and rebuild
./gradlew clean
./gradlew build
```

### Download Fails

**Issue**: Spotify metadata not loading
- Default Spotify credentials are included
- Should work out of the box

**Issue**: YouTube download fails
- Skip metadata fetch for YouTube
- Directly select quality and download

---

## 🎯 What's Next?

### Explore Features
- ✅ Try different platforms (Spotify, JioSaavn, YouTube)
- ✅ Test all quality options
- ✅ Check download history
- ✅ Monitor real-time progress

### Read Documentation
- 📖 [README_ANDROID.md](README_ANDROID.md) - Full Android app guide
- 📖 [USAGE_GUIDE.md](USAGE_GUIDE.md) - Detailed usage instructions
- 📖 [ARCHITECTURE.md](ARCHITECTURE.md) - System architecture
- 📖 [example_config.md](example_config.md) - Configuration examples

### Customize
- Change theme colors in `ui/theme/Color.kt`
- Modify backend URL in `SpowloApplication.kt`
- Add your Spotify credentials (optional)

---

## 📊 System Architecture (Quick View)

```
┌─────────────────┐
│  Android App    │  • Jetpack Compose UI
│  (MVVM)         │  • Room Database
└────────┬────────┘  • Retrofit API Client
         │
    HTTP REST
         │
┌────────▼────────┐
│  FastAPI        │  • Metadata fetching
│  Backend        │  • Download management
└────────┬────────┘  • Job tracking
         │
    Subprocess
         │
┌────────▼────────┐
│  yt-dlp         │  • Audio extraction
│  + ffmpeg       │  • Format conversion
└─────────────────┘  • Metadata embedding
```

---

## 💡 Pro Tips

### Backend
- Use `--reload` flag during development for auto-restart
- Check logs in terminal for download progress
- API docs available at http://localhost:8000/docs

### Android App
- Enable "Show layout bounds" in Developer Options to debug UI
- Use Logcat in Android Studio to see debug logs
- Clear app data if database issues occur

### Development
- Keep both backend and app running simultaneously
- Test on physical device for best experience
- Use Android Studio's "Apply Changes" for faster iteration

---

## 🎉 Success!

If you've completed all steps, you now have:
- ✅ Working backend API server
- ✅ Running Android app
- ✅ Successfully downloaded a song
- ✅ Viewed download history

**Total Time**: ~12 minutes

**Congratulations!** You're ready to use Spowlo Music Downloader! 🎵

---

## 🆘 Need Help?

1. Check [USAGE_GUIDE.md](USAGE_GUIDE.md) for detailed troubleshooting
2. Review [ARCHITECTURE.md](ARCHITECTURE.md) for system design
3. See [example_config.md](example_config.md) for configuration help
4. Check backend logs for API errors
5. Check Logcat for Android app errors

---

## 📚 Learn More

Want to understand how it works?

- **Backend**: See `backend/README.md` for API documentation
- **Android**: See `README_ANDROID.md` for app architecture
- **Architecture**: See `ARCHITECTURE.md` for system design
- **Examples**: See `example_config.md` for advanced configuration

---

## 🔄 Common Workflows

### Adding a New Platform
1. Add client in `backend/app/` (similar to spotify_client.py)
2. Add endpoint in `backend/app/main.py`
3. Update platform detection in Android Repository
4. Test with sample URLs

### Changing Theme
1. Edit `app/src/main/java/com/spowlo/musicdownloader/ui/theme/Color.kt`
2. Rebuild and run
3. See changes immediately

### Adding Features
1. Create domain model (if needed)
2. Add repository method
3. Update ViewModel
4. Update UI Screen
5. Test thoroughly

---

## ✨ That's It!

You now have a complete music downloader system. Happy downloading! 🎶

---

**Next**: Read [USAGE_GUIDE.md](USAGE_GUIDE.md) for advanced features and examples.
