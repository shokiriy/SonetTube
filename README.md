# SonetTube

SonetTube opens YouTube's official mobile website in an Android WebView. The same responsive page fills the phone or a wide head-unit window.

## What it does

- Renders YouTube's site in Android System WebView
- Keeps JavaScript, DOM storage, and cookies enabled for normal site behavior and session state
- Supports YouTube's HTML5 player and fullscreen mode
- Requires a user gesture before media playback starts
- Pauses page video when the activity goes into the background
- Sends links outside YouTube and Google's supporting domains to the device browser
- Uses Android Back to navigate WebView history

The app does not download or extract media, block ads, or modify DRM behavior.

## Android Auto

The launcher activity has an AABrowser-style `CAR_LAUNCHER` entry and a resizable 800×480 head-unit window. For sideloaded builds, Android Auto's Developer settings must allow unknown sources. Android Auto version and vehicle compatibility can affect whether the app appears.

This is a **parked-use-only** video/browser experiment. Its launcher activity deliberately **does not** declare `distractionOptimized`. Android Auto may pause or dismiss parked activities as soon as the vehicle starts moving; SonetTube also pauses its WebView on activity pause. Neither a launcher intent-filter nor Android Auto Developer Mode overrides that behavior.

**Compatibility caveat:** The experimental manifest currently uses `android:appCategory="game"` even though SonetTube plays web video. This is *not* a correct product-category declaration or an official supported video-app integration. As of October 2026, Android Auto's generally available parked app category is games; Android Auto video support is being introduced separately for eligible devices and partners. Do not expect this sideloaded video app to work reliably on a car head unit. In particular, do not treat its presence in the launcher as authorization to show video while driving.

**What is not implemented:** SonetTube has no Android Auto media browsing service or Android `MediaSession` integration. Adding these can support appropriate audio controls, but it does not grant permission to render video while moving. Fermata uses additional car-specific services, so the two apps are not equivalent.

## Requirements

- Android Studio with Android SDK Platform 35
- JDK 17
- Android phone running API 26 or newer
- Android Auto on the phone and a compatible head unit for car projection
- Internet access and an up-to-date Android System WebView

The active YouTube website experience does not require a YouTube Data API key. The original API search, Room, and IFrame-player source modules remain in the repository but are not wired into the current app entry point.

## Build and install

Open the repository in Android Studio and run the `app` configuration. To build from a terminal:

```bash
./gradlew testDebugUnitTest assembleDebug
```

The debug APK is produced at `app/build/outputs/apk/debug/app-debug.apk`. It is signed with the debug key and can be installed on your phone for testing.

To allow a sideloaded app in Android Auto, open Android Auto settings, tap the version entry repeatedly to enable Developer settings, then enable **Unknown sources**. Menu wording can vary by Android Auto version.

## WebView behavior

The app loads the official YouTube mobile site and leaves its interface and playback behavior under YouTube's control. The WebView allows YouTube and supporting Google domains for site navigation and sign-in flows; other HTTP(S) links open in the default browser. File and content access are disabled, and mixed-content loading is blocked.

## Legacy source

The first SonetTube version included YouTube Data API search, Room-backed favorites and history, and an IFrame player. Those source files and their unit tests remain for now, but the current launcher screen opens the full YouTube website.
