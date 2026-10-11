# SonetTube

SonetTube is a phone-first Android app that opens YouTube's official mobile website inside the app. The home screen uses `https://m.youtube.com`, so YouTube provides the feed, search, Shorts, subscriptions, playlists, account, and watch history.

## What it does

- Renders YouTube's site in Android System WebView
- Keeps JavaScript, DOM storage, and cookies enabled for normal site behavior and session state
- Supports YouTube's HTML5 player and its fullscreen mode
- Requires a user gesture before media playback starts
- Sends links outside YouTube and Google's supporting domains to the device browser
- Uses the Android back action to navigate the WebView's page history

The app does not download or extract media, block ads, modify DRM behavior, or add Android Auto video support.

## Technology

- Kotlin and Gradle Kotlin DSL
- Jetpack Compose and Material 3
- Android WebView and WebChromeClient
- Android System WebView for site rendering and playback

## Requirements

- Android Studio with Android SDK Platform 35
- JDK 17
- Android phone or emulator running API 26 or newer
- Internet access and an up-to-date Android System WebView

The active YouTube website experience does not require a YouTube Data API key. The original API search, Room, and IFrame-player source modules remain in the repository but are not wired into the current app entry point.

## Open and run

Open the repository directory in Android Studio, allow Gradle to sync, and run the `app` configuration on a device or emulator.

From a terminal:

```bash
./gradlew test
./gradlew assembleDebug
```

On Windows PowerShell, use `./gradlew.bat test` and `./gradlew.bat assembleDebug`.

## WebView behavior

The app loads the official YouTube mobile site and leaves its interface and playback behavior under YouTube's control. The WebView allows YouTube and supporting Google domains for site navigation and sign-in flows; other HTTP(S) links open in the default browser. File and content access are disabled, and mixed-content loading is blocked.

## Android Auto and vehicle use

This repository remains a phone app. It does not register a car launcher activity, declare itself distraction-optimized, or attempt to bypass Android Auto restrictions. The AABrowser project describes its video feature for parked use; SonetTube does not add that project's Android Auto integration.

## Legacy source

The first SonetTube version included YouTube Data API search, Room-backed favorites and history, and an IFrame player. Those source files and their unit tests remain for now, but the current launcher screen opens the full YouTube website.
