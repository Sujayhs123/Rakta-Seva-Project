# Rakta-Seva Connect - Precise Build Brief

## 1. Purpose
Rakta-Seva Connect is an Android app for emergency blood donation. It connects requesters with available matching donors within a 10 km radius, protects donor contact details until acceptance, and prevents recently donated users from receiving requests for 90 days.

## 2. MVP Scope
Build the MVP with five core screens:
1. Splash / Auth Check
2. Login / Register
3. Home Dashboard
4. Emergency Blood Request
5. Donor Profile

Keep request status, donor search, notification inbox, settings, hospital coordinator tools, analytics, multilingual support, NGO partnerships, and wearable alerts as future enhancements unless specifically required.

## 3. Tech Stack
| Layer | Choice |
|---|---|
| Platform | Android native |
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM with Repository pattern |
| Auth | Firebase Authentication with phone OTP |
| Database | Cloud Firestore |
| Notifications | Firebase Cloud Messaging |
| Backend logic | Firebase Cloud Functions |
| Location | Google Play Services Location API |
| Maps | Google Maps SDK, only where location picking is needed |
| Async | Kotlin Coroutines + Flow |

## 4. Design Rules
- Primary color: #D32F2F.
- Accent color: #FF6F00.
- Use Material 3 components.
- Keep emergency actions red and visually dominant.
- Minimum tap target: 48 dp.
- Critical actions should be reachable within two taps from Home.
- Show inline validation errors.
- Use clear loading, empty, success, and failure states.

## 5. Core Screens

### 5.1 Splash / Auth Check
Purpose: launch screen and routing.

Requirements:
- Show app name, logo placeholder, and short tagline.
- Check Firebase auth state.
- If logged in, open Home.
- If not logged in, open Login / Register.
- Clear the back stack after routing.

### 5.2 Login / Register
Purpose: authentication and donor profile creation.

Requirements:
- Phone number input with India country code support.
- OTP send and verify flow using Firebase Auth.
- Registration fields: full name, phone, blood group, date of birth, gender, address, location permission, optional email.
- Require user age of 18+.
- Save the user profile to `users/{uid}` after successful registration.
- Store/update FCM token.

### 5.3 Home Dashboard
Purpose: central action hub.

Requirements:
- Greeting with user name and current/local saved location.
- Availability toggle synced with Firestore.
- Primary action: Request Blood.
- Secondary action: My Profile.
- Active emergency alerts matching donor eligibility.
- Bottom navigation with Home and Profile for MVP.

### 5.4 Emergency Blood Request
Purpose: create and broadcast a blood request.

Fields:
- Patient name
- Required blood group
- Units required, 1 to 10
- Urgency: Critical, Urgent, Within 24 hours
- Hospital name
- Hospital address
- Auto-detected or selected location
- Requester contact number
- Optional notes

Submit behavior:
- Validate all required fields.
- Create a document in `bloodRequests`.
- Trigger donor matching through Cloud Functions.
- Notify matching donors by FCM.
- Show confirmation and return to Home.

### 5.5 Donor Profile
Purpose: manage donor information and availability.

Requirements:
- Show name, blood group, phone, email, address, location, last donation date, and donation count.
- Allow editing of profile fields.
- Save changes to `users/{uid}`.
- Auto-disable availability for 90 days after donation.
- Include logout.

## 6. User Flow
1. App opens and checks auth.
2. New user verifies phone and completes profile.
3. User lands on Home and can enable availability.
4. Requester creates a blood request.
5. Cloud Function finds eligible donors: matching blood group, within 10 km, available, not blocked by 90-day rule.
6. Matching donors receive high-priority notifications.
7. Donor accepts or declines.
8. Requester can see accepted donor contact details.

## 7. Firestore Schema

### users/{uid}
| Field | Type | Notes |
|---|---|---|
| uid | string | Firebase UID |
| name | string | Required |
| phone | string | Required, verified |
| email | string | Optional |
| bloodGroup | string | A+, A-, B+, B-, AB+, AB-, O+, O- |
| dob | timestamp/string | Must indicate age 18+ |
| gender | string | Male/Female/Other |
| address | string | Optional but recommended |
| latitude | number | Required for donor matching |
| longitude | number | Required for donor matching |
| geohash | string | Required for proximity queries |
| isAvailable | boolean | Controlled by user and 90-day rule |
| lastDonationDate | timestamp/null | Used for cooldown |
| donationCount | number | Default 0 |
| fcmToken | string | Used for push notifications |
| createdAt | timestamp | Server timestamp |
| updatedAt | timestamp | Server timestamp |

### bloodRequests/{requestId}
| Field | Type | Notes |
|---|---|---|
| requesterUid | string | Creator UID |
| patientName | string | Required |
| bloodGroup | string | Required |
| unitsRequired | number | 1-10 |
| urgency | string | Critical/Urgent/Within 24 hours |
| hospitalName | string | Required |
| hospitalAddress | string | Required |
| latitude | number | Request location |
| longitude | number | Request location |
| geohash | string | For location matching |
| contactNumber | string | Revealed to accepting donors/requester flow as needed |
| notes | string | Optional |
| status | string | active, fulfilled, expired, cancelled |
| notifiedDonors | array | Donor UIDs notified |
| acceptedDonors | array | Donor UIDs accepted |
| createdAt | timestamp | Server timestamp |
| expiresAt | timestamp | createdAt + 24 hours |

### donations/{donationId}
| Field | Type | Notes |
|---|---|---|
| donorUid | string | Donor UID |
| requestId | string | Linked request |
| date | timestamp | Donation date |
| hospitalName | string | Donation location |
| bloodGroup | string | Donated group |

## 8. Cloud Functions

### onBloodRequestCreated
When a request is created:
- Query donors by nearby geohash range.
- Filter exact distance within 10 km.
- Filter by matching blood group.
- Filter `isAvailable == true`.
- Exclude donors whose `lastDonationDate` is within the last 90 days.
- Send high-priority FCM notification.
- Save notified donor IDs to the request.

### expireOldRequests
Scheduled function:
- Mark active requests as `expired` after 24 hours.

### onDonorAccept
When a donor accepts:
- Add donor UID to `acceptedDonors`.
- Notify requester.
- Reveal donor contact only after acceptance.

## 9. Business Rules
- Only verified users can create requests or receive donor alerts.
- Donor matching radius is 10 km.
- Donors are unavailable for 90 days after a donation.
- Blood requests expire after 24 hours.
- Contact details remain private until a donor accepts.
- All writes must use server timestamps where possible.
- No API keys or secrets should be committed.

## 10. Android Permissions
Required:
- INTERNET
- ACCESS_FINE_LOCATION
- ACCESS_COARSE_LOCATION
- POST_NOTIFICATIONS for Android 13+

Request location and notification permissions only when needed, with clear user-facing context.

## 11. Acceptance Criteria
- User can register and verify phone OTP.
- User profile saves to Firestore.
- User can update availability.
- User can create a valid blood request.
- Request is saved with correct location, urgency, and expiry fields.
- Matching donors receive notifications.
- Donors inside 10 km with the right blood group are included.
- Donors outside 10 km or inside the 90-day cooldown are excluded.
- Contact details are hidden until acceptance.
- Expired requests no longer appear as active.
- App handles loading, offline, permission denied, and empty states.

## 12. Final Build Prompt
Build the Rakta-Seva Connect Android MVP using Kotlin, Jetpack Compose, Material 3, MVVM, Firebase Auth, Firestore, FCM, Cloud Functions, and Google Location Services. Implement the five MVP screens, Firestore schema, donor matching rules, privacy rules, request expiry, and acceptance criteria listed in this document. Provide setup instructions for Firebase configuration and local Android Studio execution.