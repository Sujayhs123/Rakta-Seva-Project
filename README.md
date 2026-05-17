# Rakta-Seva Connect 🩸

Rakta-Seva Connect is an Android emergency blood donation application built with **Kotlin**, **Jetpack Compose**, **Material 3**, and **Firebase** backend services. The application demonstrates donor registration, donor availability management, emergency blood request creation, nearby donor matching, request tracking, and donor profile management.

Rakta-Seva Connect is a modern, real-time Android application designed to bridge the gap between blood donors and recipients. Built with **Jetpack Compose** and **Firebase**, it focuses on speed, security, and creating a positive local community impact.

##  Key Features

- **Emergency Broadcasts**  
  Post blood requests that instantly reach matching donors within a **10 km radius**.

- **Privacy-First Design**  
  Personal contact details are only shared after a donor explicitly accepts a request.

- **Live Status Tracking**  
  Real-time updates on how many donors have been notified, viewed, and accepted your request.

- **Interactive Map View**  
  Visualize nearby donors and hospitals using **Google Maps integration**.

- **Donor History**  
  Track donations, lives saved, and cooling-down periods to ensure safe donation intervals.

- **Verified Profiles**  
  Support for hospital-verified donation history.

---

##  Tech Stack

- **UI Framework:** Jetpack Compose (100% Kotlin)
- **Architecture:** MVVM (Model-View-ViewModel)
- **Backend:** Firebase (Firestore, Authentication, Cloud Messaging)
- **Maps:** Google Maps SDK for Android (Maps Compose)
- **Navigation:** Compose Navigation
- **Graphics:** Custom Canvas-based logo rendering

---

##  Getting Started

### Prerequisites

Before running the application, ensure you have:

- Android Studio Ladybug or newer
- JDK 17
- Google Maps API Key
- `google-services.json` file from your Firebase project

### Installation

1. Clone the repository:

```bash
git clone https://github.com/yourusername/Rakta-Seva-Connect.git
```

2. Open the project in Android Studio.

3. Add your `google-services.json` file to the `app/` directory.

4. Add your Google Maps API key in `local.properties`:

```properties
MAPS_API_KEY=YOUR_API_KEY_HERE
```

5. Sync Gradle and run the application.

---

Built with ❤️ to connect donors and save lives.
