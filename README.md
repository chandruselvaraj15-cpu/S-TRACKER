# S-TRACK

S-TRACK (Smart Sales Tracker) is an offline-first Android sales tracker for showroom promoters. This repository contains a native Kotlin + Jetpack Compose application using Room for durable local sales storage.

## Current implementation

- Record brand, model, and quantity in a few taps.
- Persist sale entries locally with Room.
- Calculate today's FTD, current-month MTD, and brand share from stored sales using the device's local timezone.
- Browse sales history and brand totals.
- Dedicated **Nila** assistant identity: a friendly, anime-inspired visual character for the future mobile-sales assistant. Nila's interface is clearly marked as not connected to a live AI service; it does not pretend to generate AI answers or invent verified product specifications.
- Responsive dark-first Material 3 interface.

## Build

Open this repository in Android Studio (Hedgehog or newer) and allow Gradle sync. The app module targets Android 8.0 (API 26) and compiles against API 35. To build from a machine with Gradle installed:

```bash
gradle assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`. For a release APK, configure a signing key in your local Gradle configuration, then run `gradle assembleRelease`; never commit signing credentials.

## Data and privacy

Sales are stored on-device in Room. No login or network connection is required for sale entry, history, FTD/MTD, or brand totals. This MVP does not collect customer personal information.

## Package

`com.chandru.strack`
