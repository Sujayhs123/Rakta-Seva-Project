# Rakta-Seva Connect

Rakta-Seva Connect is an Android emergency blood donation project built with Kotlin, Jetpack Compose, Material 3, and Firebase backend components. The app demonstrates donor registration, donor availability, emergency blood request creation, nearby donor matching, request status, and donor profile screens.

## Features

- Splash and onboarding flow
- Login/register user interface
- Home dashboard with donor availability
- Emergency blood request form
- Nearby donor list and map view
- Incoming request and request status screens
- Donor profile management
- Firebase Cloud Functions for matching donors within 10 km
- Firestore rules and indexes
- 90-day donation cooldown backend logic

## Tech Stack

- Android native app
- Kotlin
- Jetpack Compose
- Material 3
- MVVM-style structure
- Firebase Auth, Firestore, Cloud Messaging
- Firebase Cloud Functions with TypeScript
- Google Maps and Location Services

## Project Structure

```text
.
├── app/                         Android app module
│   └── src/main/java/com/raktaseva/connect/
├── functions/                   Firebase Cloud Functions
│   └── src/index.ts
├── firestore.rules              Firestore security rules
├── firestore.indexes.json       Firestore indexes
├── firebase.json                Firebase project config
├── BACKEND.md                   Backend setup notes
└── README.md
```

## Android Setup

1. Open this folder in Android Studio.
2. Let Gradle sync finish.
3. Add your real Google Maps API key in a local `local.properties` file:

   ```properties
   MAPS_API_KEY=your_google_maps_api_key
   ```

4. If you want Firebase services connected in the Android app, create a Firebase Android app with package name `com.raktaseva.connect`, download `google-services.json`, and place it inside `app/`.
5. Run the app with:

   ```bash
   ./gradlew assembleDebug
   ```

   On Windows:

   ```bash
   gradlew.bat assembleDebug
   ```

## Firebase Functions Setup

```bash
cd functions
npm install
npm run build
```

To deploy after configuring Firebase CLI:

```bash
npm run deploy
```

## Submission Notes

This repository intentionally excludes local machine files, generated build folders, APKs, memory dumps, and private configuration. Add real API keys only on your machine or in your Firebase/Google Cloud project settings.

## Status

The Android app contains a complete Compose UI prototype with sample in-memory data. The backend folder contains Firebase Functions, Firestore rules, and indexes for the real donor-matching workflow.
