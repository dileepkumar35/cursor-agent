# Spowlo Music Downloader - Android App

Modern Android application for downloading music from Spotify, JioSaavn, and YouTube.

## Features

- **Multi-Platform Support**: Download from Spotify, JioSaavn, and YouTube
- **Automatic Platform Detection**: Automatically detects the platform from URL
- **Quality Selection**: Choose between M4A 320kbps, Opus 160kbps, or Best Available
- **Real-Time Progress**: Track download progress with live updates
- **Download History**: View and manage all your downloads
- **Material 3 Design**: Modern UI built with Jetpack Compose
- **Offline-First**: Local Room database for download history

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM (Model-View-ViewModel)
- **Networking**: Retrofit 2 + OkHttp
- **Database**: Room
- **Async**: Kotlin Coroutines + Flow
- **Image Loading**: Coil
- **Dependency Injection**: Manual (for simplicity)
- **Target SDK**: 34 (Android 14)
- **Minimum SDK**: 33 (Android 13)

## Project Structure

```
app/
├── src/main/
│   ├── java/com/spowlo/musicdownloader/
│   │   ├── data/
│   │   │   ├── local/          # Room database entities and DAOs
│   │   │   ├── remote/         # Retrofit API service and models
│   │   │   └── repository/     # Repository layer
│   │   ├── domain/             # Domain models
│   │   ├── ui/
│   │   │   ├── components/     # Reusable Compose components
│   │   │   ├── screens/        # Screen composables and ViewModels
│   │   │   └── theme/          # Material 3 theme
│   │   ├── util/               # Utility classes and extensions
│   │   ├── MainActivity.kt     # Main activity
│   │   └── SpowloApplication.kt # Application class
│   ├── res/                    # Resources (strings, themes, etc.)
│   └── AndroidManifest.xml
├── build.gradle.kts
└── proguard-rules.pro
```

## Architecture

The app follows Clean Architecture principles with MVVM pattern:

### Data Layer
- **Local Data Source**: Room database for storing download history
- **Remote Data Source**: Retrofit API client for backend communication
- **Repository**: Mediates between data sources and ViewModels

### Domain Layer
- Domain models that represent business entities
- Use cases (if needed for complex business logic)

### Presentation Layer
- **ViewModels**: Manage UI state and handle business logic
- **Compose UI**: Declarative UI components
- **Navigation**: Jetpack Navigation Compose

## API Integration

The app communicates with a FastAPI backend (see `/backend` directory):

### Endpoints Used

1. **Get Spotify Metadata**
   ```
   GET /api/metadata/spotify/{track_id}
   ```

2. **Get JioSaavn Metadata**
   ```
   GET /api/metadata/jiosaavn/{song_id}
   ```

3. **Start Download**
   ```
   POST /api/download
   Body: {
     "url": "https://...",
     "quality": "m4a_320",
     "metadata": {...}
   }
   ```

4. **Get Job Status**
   ```
   GET /api/job/{job_id}
   ```

## Setup Instructions

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17 or later
- Android SDK 34
- Backend server running (see `/backend/README.md`)

### Running the App

1. **Configure Backend URL**
   
   Edit `SpowloApplication.kt` to point to your backend:
   
   ```kotlin
   // For Android Emulator (localhost)
   RetrofitClient.setBaseUrl("http://10.0.2.2:8000/")
   
   // For Physical Device (use your computer's IP)
   RetrofitClient.setBaseUrl("http://192.168.1.100:8000/")
   ```

2. **Start Backend Server**
   
   ```bash
   cd backend
   python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
   ```

3. **Open in Android Studio**
   
   - Open the project root directory in Android Studio
   - Wait for Gradle sync to complete
   - Connect a device or start an emulator
   - Click Run

### Build APK

```bash
# Debug APK
./gradlew assembleDebug

# Release APK (requires signing configuration)
./gradlew assembleRelease
```

APKs will be in `app/build/outputs/apk/`

## Screens

### 1. Home Screen
- URL input field for Spotify, JioSaavn, or YouTube links
- Fetch metadata button to preview song information
- Quality selector (M4A 320kbps, Opus 160kbps, Best Available)
- Download button to start the download

### 2. Downloads Screen
- Live view of active downloads
- Real-time progress updates
- Retry/Cancel actions for failed/in-progress downloads

### 3. History Screen
- Complete download history
- Filter by status (completed, failed, etc.)
- Search by title or artist
- Delete individual items or clear all history

## Permissions

The app requires the following permissions:

- `INTERNET`: Network access for API calls
- `ACCESS_NETWORK_STATE`: Check network connectivity
- `READ_MEDIA_AUDIO` (Android 13+): Access to media files
- `READ_EXTERNAL_STORAGE` (Android 12 and below): Legacy storage access

## Storage

The app uses Scoped Storage (Android 13+) to save downloaded files:

- Downloads are saved to the app-specific directory
- Files are accessible through the device's file manager
- No special storage permissions required for app-specific storage

## Dependencies

Key dependencies (see `app/build.gradle.kts` for versions):

```kotlin
// Jetpack Compose
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.navigation:navigation-compose")

// Room Database
implementation("androidx.room:room-runtime")
implementation("androidx.room:room-ktx")
ksp("androidx.room:room-compiler")

// Retrofit & OkHttp
implementation("com.squareup.retrofit2:retrofit")
implementation("com.squareup.retrofit2:converter-gson")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android")

// Coil (Image Loading)
implementation("io.coil-kt:coil-compose")
```

## Quality Options

- **M4A 320kbps**: Best quality, larger file size (~8-10 MB per song)
- **Opus 160kbps**: High efficiency, smaller file size (~4-5 MB per song)
- **Best Available**: No conversion, keeps original format and quality

## Known Limitations

- YouTube metadata fetching is not supported (requires separate implementation)
- Download progress may not be accurate for some sources
- No concurrent download limit (all downloads run simultaneously)

## Future Enhancements

- [ ] Search functionality without URL input
- [ ] Playlist/Album download support
- [ ] Download queue management
- [ ] Custom download location
- [ ] Dark theme toggle
- [ ] Export/Import history
- [ ] Download notifications

## Troubleshooting

### Backend Connection Issues

1. **Emulator**: Use `10.0.2.2` instead of `localhost`
2. **Physical Device**: Ensure phone and computer are on the same network
3. **Firewall**: Check if firewall is blocking port 8000
4. **Backend**: Verify backend is running with `--host 0.0.0.0`

### Permission Issues

- Grant storage permissions in device Settings > Apps > Spowlo
- For Android 13+, ensure READ_MEDIA_AUDIO permission is granted

### Build Issues

```bash
# Clean and rebuild
./gradlew clean
./gradlew build

# Clear Gradle cache
rm -rf ~/.gradle/caches/
```

## Contributing

When contributing to the Android app:

1. Follow Kotlin coding conventions
2. Use Jetpack Compose for UI
3. Write ViewModel tests for business logic
4. Follow Material 3 design guidelines
5. Ensure backward compatibility with Android 13

## License

MIT License - See LICENSE file for details
