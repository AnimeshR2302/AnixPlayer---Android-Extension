# Project Plan

Create a modern Android application named AnixPlayer using Kotlin + Jetpack Compose that acts as an advanced external video player companion app. It should feature gesture controls (brightness, volume, seek, speed), floating overlay controls, playback history, bookmarking, and subtitle management. The architecture should be MVVM Clean Architecture with Hilt, Room, DataStore, and Media3/ExoPlayer support. Screens include Splash, Permission Setup, Home Dashboard, Video Session Detection, Overlay Controls Settings, Playback History, and Settings.

## Project Brief

# AnixPlayer Project Brief

AnixPlayer is an advanced external video player companion designed to enhance the viewing experience on Android. It integrates seamlessly with existing apps to provide professional-grade playback controls, gesture-based interactions, and powerful overlay features.

## Features
*   **External Stream Interception:** Capability to launch and play video content from third-party apps or local file explorers using high-performance intent filters.
*   **Enhanced Playback Controls:** Advanced gesture support for intuitive control over brightness, volume, and seeking, alongside precision playback speed and aspect ratio adjustments.
*   **Floating Picture-in-Picture (PiP):** A robust PiP implementation that allows users to continue watching videos in a resizable overlay while multitasking across other applications.
*   **Advanced Subtitle Customization:** Comprehensive subtitle engine supporting multiple formats, real-time sync adjustments, and deep visual customization (font, size, color, and background).

## High-Level Tech Stack

*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose with **Material Design 3** for a modern, vibrant aesthetic.
*   **Navigation:** **Jetpack Navigation 3** (state-driven architecture).
*   **Adaptive Layout:** **Compose Material Adaptive** library to ensure a seamless experience across handhelds, foldables, and tablets.
*   **Video Engine:** **Media3 ExoPlayer** for high-performance, low-latency video rendering and format support.
*   **Concurrency:** Kotlin Coroutines for non-blocking UI and background processing.
*   **Asynchronous Data:** Kotlin Flow for reactive state management.

## Implementation Steps

### Task_1_Base_Architecture_and_Data: Setup Hilt for dependency injection, Room for video history/bookmarks, and DataStore for user preferences. Configure the AndroidManifest with intent filters to intercept video streams.
- **Status:** IN_PROGRESS
- **Acceptance Criteria:**
  - Hilt is successfully integrated into the project
  - Room database and DataStore are functional
  - Intent filters for video playback are defined in the Manifest
  - Project builds successfully

### Task_2_Media3_Player_and_Gestures: Integrate Media3 ExoPlayer for video playback. Implement a custom player UI with gesture controls for brightness, volume, and seeking. Add subtitle support and customization options.
- **Status:** PENDING
- **Acceptance Criteria:**
  - Video playback works via external intents
  - Gesture controls (brightness, volume, seek) are intuitive and functional
  - Subtitles can be loaded and customized
  - Player UI follows Material 3 guidelines

### Task_3_Navigation_and_Screens: Implement Jetpack Navigation 3 to manage Splash, Home, History, and Settings screens. Integrate Picture-in-Picture (PiP) mode and ensure adaptive layouts for different device configurations.
- **Status:** PENDING
- **Acceptance Criteria:**
  - Navigation 3 manages all screen transitions
  - Splash, Home, History, and Settings screens are implemented
  - PiP mode works correctly
  - UI adapts to handheld, foldable, and tablet screens

### Task_4_Polish_and_Final_Verification: Apply a vibrant Material 3 theme with Edge-to-Edge display. Create an adaptive app icon. Perform a final run to verify stability and requirement alignment.
- **Status:** PENDING
- **Acceptance Criteria:**
  - Material 3 theme and Edge-to-Edge are fully implemented
  - Adaptive app icon is present
  - Application is stable and does not crash
  - All features (History, Bookmarking, Gestures) are verified as functional

