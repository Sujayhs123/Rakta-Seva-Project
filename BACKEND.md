# Rakta-Seva Connect Backend

Firebase backend for the emergency blood donation workflow.

## What Is Included

- Cloud Functions in `functions/src/index.ts`
- Firestore security rules in `firestore.rules`
- Required composite indexes in `firestore.indexes.json`
- Firebase project config in `firebase.json`

## Main Collections

### `users/{uid}`

Stores donor/requester profile data.

Required fields:

- `uid`
- `name`
- `phone`
- `bloodGroup`
- `location.lat`
- `location.lng`
- `isAvailable`
- `fcmToken`

### `bloodRequests/{requestId}`

Stores emergency blood requests.

Required fields:

- `requesterId`
- `patientName`
- `bloodGroup`
- `unitsRequired`
- `urgency`
- `hospitalName`
- `hospitalAddress`
- `location.lat`
- `location.lng`
- `contactNumber`
- `status: "active"`

### `bloodRequests/{requestId}/responses/{donorId}`

Stores donor accept/decline responses.

### `donations/{donationId}`

Triggers the 90-day donor cooldown.

## Cloud Functions

- `notifyMatchingDonors`
  - Runs when a blood request is created.
  - Finds matching available donors within 10 km.
  - Excludes donors who donated in the last 90 days.
  - Sends high-priority FCM notifications.

- `getIncomingRequest`
  - Callable function for donors.
  - Returns sanitized request details without exposing requester contact.

- `respondToBloodRequest`
  - Callable function for donors.
  - Accept reveals requester contact.
  - Decline records a silent decline.

- `expireOldBloodRequests`
  - Scheduled hourly.
  - Expires active requests after 24 hours.

- `recordDonationCooldown`
  - Runs when a donation record is created.
  - Marks the donor unavailable and starts cooldown.

- `refreshDonorAvailability`
  - Scheduled daily.
  - Re-enables donors whose 90-day cooldown has passed.

## Setup

1. Replace the placeholder Firebase project id in `.firebaserc`.
2. From `functions`, install dependencies:

```bash
npm install
```

3. Build functions:

```bash
npm run build
```

4. Deploy backend:

```bash
npm run deploy
```

The Android app should call `getIncomingRequest` before showing donor request details, and `respondToBloodRequest` for accept/decline.
