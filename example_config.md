# Example Configuration Files

This document provides example configuration for different deployment scenarios.

## Backend Configuration Examples

### 1. Development Configuration

**File**: `backend/.env.development`

```env
# Server Configuration
HOST=0.0.0.0
PORT=8000
WORKERS=1
RELOAD=true

# Spotify API
SPOTIFY_CLIENT_ID=2079ef31b1bb4feaaaa811d9f280faef
SPOTIFY_CLIENT_SECRET=2e76121a91864d41a768ba1eaf80610e

# Download Settings
DOWNLOAD_DIR=downloads
MAX_CONCURRENT_DOWNLOADS=3

# Logging
LOG_LEVEL=DEBUG
```

**Start command**:
```bash
python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

---

### 2. Production Configuration

**File**: `backend/.env.production`

```env
# Server Configuration
HOST=0.0.0.0
PORT=8000
WORKERS=4
RELOAD=false

# Spotify API (Use your own!)
SPOTIFY_CLIENT_ID=your_production_client_id
SPOTIFY_CLIENT_SECRET=your_production_client_secret

# Download Settings
DOWNLOAD_DIR=/var/www/spowlo/downloads
MAX_CONCURRENT_DOWNLOADS=5

# Logging
LOG_LEVEL=INFO

# Security
ALLOWED_ORIGINS=https://yourdomain.com,https://app.yourdomain.com
API_KEY_REQUIRED=true
API_KEY=your_secure_api_key_here

# Redis (for job queue in production)
REDIS_URL=redis://localhost:6379/0
```

**Start command** (with systemd):
```bash
# Create systemd service: /etc/systemd/system/spowlo-api.service
[Unit]
Description=Spowlo Music API
After=network.target

[Service]
Type=simple
User=www-data
WorkingDirectory=/var/www/spowlo/backend
Environment="PATH=/var/www/spowlo/venv/bin"
ExecStart=/var/www/spowlo/venv/bin/uvicorn app.main:app --host 0.0.0.0 --port 8000 --workers 4
Restart=always

[Install]
WantedBy=multi-user.target
```

---

### 3. Docker Configuration

**File**: `backend/Dockerfile`

```dockerfile
FROM python:3.11-slim

# Install ffmpeg
RUN apt-get update && \
    apt-get install -y ffmpeg && \
    rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy requirements
COPY requirements.txt .

# Install Python dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Copy application code
COPY app/ ./app/

# Create downloads directory
RUN mkdir -p downloads

# Expose port
EXPOSE 8000

# Run application
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
```

**File**: `docker-compose.yml`

```yaml
version: '3.8'

services:
  backend:
    build: ./backend
    ports:
      - "8000:8000"
    environment:
      - SPOTIFY_CLIENT_ID=${SPOTIFY_CLIENT_ID}
      - SPOTIFY_CLIENT_SECRET=${SPOTIFY_CLIENT_SECRET}
    volumes:
      - ./backend/downloads:/app/downloads
    restart: unless-stopped

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    restart: unless-stopped
```

**Start command**:
```bash
docker-compose up -d
```

---

## Android App Configuration Examples

### 1. Development Configuration

**File**: `app/src/main/java/com/spowlo/musicdownloader/SpowloApplication.kt`

```kotlin
class SpowloApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Development - Android Emulator
        RetrofitClient.setBaseUrl("http://10.0.2.2:8000/")
        
        // Development - Physical Device (same WiFi)
        // RetrofitClient.setBaseUrl("http://192.168.1.100:8000/")
    }
}
```

**File**: `app/build.gradle.kts` (Debug build type)

```kotlin
buildTypes {
    debug {
        isMinifyEnabled = false
        isDebuggable = true
        applicationIdSuffix = ".debug"
        versionNameSuffix = "-debug"
    }
}
```

---

### 2. Production Configuration

**File**: `app/src/main/java/com/spowlo/musicdownloader/SpowloApplication.kt`

```kotlin
class SpowloApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Production - Use your production API URL
        RetrofitClient.setBaseUrl("https://api.yourdomain.com/")
        
        // Optional: Add API key for production
        RetrofitClient.setApiKey("your_production_api_key")
    }
}
```

**File**: `app/build.gradle.kts` (Release build type)

```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
        
        // Signing configuration
        signingConfig = signingConfigs.getByName("release")
    }
}

signingConfigs {
    create("release") {
        storeFile = file("../keystore/release.keystore")
        storePassword = System.getenv("KEYSTORE_PASSWORD")
        keyAlias = System.getenv("KEY_ALIAS")
        keyPassword = System.getenv("KEY_PASSWORD")
    }
}
```

---

### 3. Build Variants Configuration

**File**: `app/build.gradle.kts`

```kotlin
android {
    flavorDimensions += "environment"
    
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            
            buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8000/\"")
        }
        
        create("staging") {
            dimension = "environment"
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            
            buildConfigField("String", "API_BASE_URL", "\"https://staging-api.yourdomain.com/\"")
        }
        
        create("prod") {
            dimension = "environment"
            
            buildConfigField("String", "API_BASE_URL", "\"https://api.yourdomain.com/\"")
        }
    }
}
```

**Usage**:
```kotlin
// In SpowloApplication.kt
RetrofitClient.setBaseUrl(BuildConfig.API_BASE_URL)
```

---

## Network Security Configuration

### For Development (HTTP support)

**File**: `app/src/main/res/xml/network_security_config.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <!-- Allow cleartext traffic for localhost during development -->
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">10.0.2.2</domain>
        <domain includeSubdomains="true">localhost</domain>
        <domain includeSubdomains="true">192.168.0.0/16</domain>
    </domain-config>
</network-security-config>
```

**AndroidManifest.xml**:
```xml
<application
    android:networkSecurityConfig="@xml/network_security_config"
    ...>
```

---

### For Production (HTTPS only)

**File**: `app/src/main/res/xml/network_security_config.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <!-- Production - Only HTTPS -->
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
</network-security-config>
```

---

## API Key Configuration (Optional)

### Backend with API Key

**File**: `backend/app/main.py` (Add API key middleware)

```python
from fastapi import Header, HTTPException

async def verify_api_key(x_api_key: str = Header(...)):
    if x_api_key != os.getenv("API_KEY", ""):
        raise HTTPException(status_code=403, detail="Invalid API Key")
    return x_api_key

# Add dependency to protected routes
@app.post("/api/download", dependencies=[Depends(verify_api_key)])
async def start_download(request: DownloadRequest):
    ...
```

### Android App with API Key

**File**: `app/src/main/java/com/spowlo/musicdownloader/data/remote/RetrofitClient.kt`

```kotlin
object RetrofitClient {
    private var apiKey: String? = null
    
    fun setApiKey(key: String) {
        apiKey = key
    }
    
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                apiKey?.let { key ->
                    request.addHeader("X-API-Key", key)
                }
                chain.proceed(request.build())
            }
            .build()
    }
}
```

---

## Environment-Specific Settings

### Development
- **Backend**: Local server, debug logging, hot reload
- **Android**: Debug build, verbose logging, emulator
- **Database**: In-memory or local SQLite
- **Storage**: Local filesystem

### Staging
- **Backend**: Staging server, info logging, stable
- **Android**: Debug build with staging API
- **Database**: Staging database
- **Storage**: Cloud storage (S3, GCS)

### Production
- **Backend**: Production server, error logging only, multiple workers
- **Android**: Release build, minified, signed
- **Database**: Production database with backups
- **Storage**: Production cloud storage with CDN

---

## Monitoring Configuration (Optional)

### Backend Monitoring

```python
# Add to requirements.txt
prometheus-fastapi-instrumentator==6.1.0

# Add to app/main.py
from prometheus_fastapi_instrumentator import Instrumentator

instrumentator = Instrumentator()
instrumentator.instrument(app).expose(app)
```

### Android App Monitoring

**Add Firebase Crashlytics**:

```kotlin
// app/build.gradle.kts
plugins {
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:32.5.0"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
}
```

---

## Performance Optimization

### Backend

```python
# Add caching
from functools import lru_cache

@lru_cache(maxsize=100)
def get_track_metadata(track_id: str):
    # Cache metadata for 1 hour
    pass

# Add request limits
from slowapi import Limiter
from slowapi.util import get_remote_address

limiter = Limiter(key_func=get_remote_address)
app.state.limiter = limiter

@app.post("/api/download")
@limiter.limit("10/minute")
async def start_download(request: DownloadRequest):
    pass
```

### Android App

```kotlin
// Retrofit caching
val cacheSize = 10 * 1024 * 1024 // 10 MB
val cache = Cache(context.cacheDir, cacheSize.toLong())

val okHttpClient = OkHttpClient.Builder()
    .cache(cache)
    .addInterceptor { chain ->
        var request = chain.request()
        request = if (hasNetwork(context))
            request.newBuilder()
                .header("Cache-Control", "public, max-age=60")
                .build()
        else
            request.newBuilder()
                .header("Cache-Control", "public, only-if-cached, max-stale=604800")
                .build()
        chain.proceed(request)
    }
    .build()
```

---

## Summary

- **Development**: Focus on ease of debugging and fast iteration
- **Staging**: Mirror production but with test data
- **Production**: Optimize for performance, security, and reliability

Choose the appropriate configuration for your deployment scenario and customize as needed.
