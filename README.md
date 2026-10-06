# SonetTube

SonetTube is a small, modern Android application for searching YouTube, watching videos in-app, saving favorites, and keeping a local watch history. It is intentionally a phone-first learning project. Version 1 does not implement Android Auto, screen mirroring, driving-mode workarounds, downloading, ad blocking, or background-audio bypasses.

## Features

- YouTube Data API v3 search with thumbnails, channel names, and publication dates
- YouTube IFrame Player API hosted in a carefully configured WebView
- Portrait playback and fullscreen landscape playback
- Open a video from a YouTube watch URL, short URL, Shorts URL, embed URL, or raw video ID
- Favorites and a persistent, newest-first watch history capped at 100 entries
- Share a video URL or open it in the official YouTube app/browser
- Light and dark Material 3 themes
- Clear error states for missing API keys, invalid keys/quota errors, network failures, and empty searches

## Technology stack

- Kotlin and Gradle Kotlin DSL
- Jetpack Compose and Material 3
- Navigation Compose, ViewModel, Coroutines, and StateFlow
- Retrofit, OkHttp, and Gson
- Room for favorites and watch history
- Coil for thumbnail loading
- Android WebView with the official YouTube IFrame Player API
- Manual application dependency wiring; no Hilt/Koin is required

## Requirements

- Android Studio with Android SDK Platform 35
- JDK 17
- A YouTube Data API v3 key for search
- Android phone or emulator running API 26 or newer

## API key setup

The API key is never hardcoded. `local.properties` is ignored by Git and is read only at build time into `BuildConfig.YOUTUBE_API_KEY`.

1. Open the [Google Cloud Console](https://console.cloud.google.com/).
2. Create or select a project.
3. Enable **YouTube Data API v3** under APIs & Services → Library.
4. Open APIs & Services → Credentials.
5. Create an API key. Restrict it where appropriate for your development setup.
6. In the project root, create or edit `local.properties` and add:

   ```properties
   YOUTUBE_API_KEY=YOUR_API_KEY
   ```

   `local.properties.example` is included as a template. If the key is missing, the app still compiles and shows a clear setup message when a search is attempted.

## Open and run

Open the repository directory in Android Studio, allow Gradle to sync, configure the API key, and run the `app` configuration on a device or emulator.

From a terminal:

```bash
./gradlew test
./gradlew assembleDebug
```

On Windows PowerShell, use `./gradlew.bat test` and `./gradlew.bat assembleDebug`.

## Debug APK

A debug APK built from the `main` branch is available at [`artifacts/SonetTube-debug.apk`](https://github.com/shokiriy/SonetTube/raw/refs/heads/main/artifacts/SonetTube-debug.apk). Enable installation from the APK source on your Android device if prompted.

This is a development build. Because the YouTube Data API key is supplied at build time to a client application, restrict the key to **YouTube Data API v3** in Google Cloud Console and do not use this debug APK as a production distribution.

## Architecture

The app uses a small manual dependency container created by `SonetTubeApp`:

```text
Compose UI → ViewModel → Repository → Retrofit / Room
```

Remote API DTOs are mapped to the `Video` domain model before reaching UI code. Room stores `FavoriteVideoEntity` and `RecentVideoEntity`. Navigation passes only a video ID; metadata is loaded from local history/favorites after a video is opened.

The WebView player loads the official YouTube embed URL with an app-identifying `Referer` request header. JavaScript is enabled because YouTube playback requires it; file access and generic external navigation are restricted.

## Project structure

```text
app/src/main/java/com/shokirjon/sonettube/
├── app/          Application, Activity, and dependency container
├── data/         Retrofit DTOs/API and Room database/DAOs/entities
├── model/        Domain models
├── navigation/  Routes and Navigation Compose graph
├── repository/  Remote and local data access
├── ui/           Compose screens, ViewModels, player, components, theme
└── util/         Reusable YouTube URL parser
```

## Tests

The project includes meaningful unit tests for YouTube URL parsing and remote DTO-to-domain mapping. Run all tests with:

```bash
./gradlew test
```

## Future plans

- Playlists
- Improved player-state reporting
- Optional Google account support
- Tablet-optimized UI
- Android Auto integration only if official platform support becomes available and suitable

The current version does **not** bypass Android Auto restrictions and does not implement Android Automotive OS, screen mirroring, AAAD, CarStream, Fermata Auto integration, DRM bypass, downloading, ad blocking, or background-audio workarounds.
