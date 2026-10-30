# Datability Notify App — Firebase Notification Inbox

A native Android application that receives push notifications through Firebase Cloud Messaging (FCM), displays Android system notifications, and maintains a persistent local Notification Inbox.

Built with **Kotlin**, **Jetpack Compose (Material 3)**, **MVVM**, **Repository Pattern**, and **Room Database**.

---

## Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Project Architecture](#project-architecture)
- [Requirements](#requirements)
- [Clone and Build](#clone-and-build)
- [Firebase Project Setup](#firebase-project-setup)
- [How Notifications Are Processed](#how-notifications-are-processed)
- [Testing Notifications](#testing-notifications)
  - [A. Firebase Console Campaign](#a-test-using-firebase-console-campaign)
  - [B. Postman Data-only FCM Message](#b-test-using-postman--data-only-fcm-message)
- [Expected Notification Payload](#expected-notification-payload)
- [Assumptions and Design Decisions](#assumptions-and-design-decisions)
- [Known Limitations](#known-limitations)
- [Submission Notes](#submission-notes)

---

## Features

### Firebase Cloud Messaging

- Receives push messages through Firebase Cloud Messaging.
- Displays Android system notifications with app icon, title, and description.
- Uses the required notification channel — **ID:** `assignment`; **Name:** `Assignment`.
- Tapping a system notification opens the Notification Inbox.
- Handles the Android 13+ `POST_NOTIFICATIONS` runtime permission.

### Local Notification Inbox

- Stores notification history and read state locally using Room.
- Uses notification `id` as the unique database key.
- Uses `OnConflictStrategy.IGNORE` to prevent duplicate database records and preserve an existing read state.
- Displays notifications newest first.

### Inbox and Details

- Displays banner image, title, description, timestamp, and read/unread state.
- Opens a Notification Details screen when an inbox item is selected.
- Marks a notification as read when its Details screen is opened.
- Includes unread count, Mark All as Read, individual deletion, and Clear All with confirmation.
- Displays an empty state when there are no notifications.
- Formats timestamps into a readable date and time.

### Error Handling

- Shows a fallback icon when a banner URL is missing or cannot be loaded.
- Handles missing optional payload values and invalid timestamps without crashing.
- Keeps notification text visible when an image cannot be loaded.

---

## Technology Stack

| Technology | Version / Usage |
|---|---|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose, Material 3 |
| Compose BOM | 2024.09.00 |
| Architecture | MVVM + Repository Pattern |
| Database | Room 2.6.1 |
| Database code generation | KSP 2.0.21-1.0.28 |
| Navigation | Navigation Compose 2.8.5 |
| Image loading | Coil Compose 2.7.0 |
| Push notifications | Firebase Cloud Messaging |
| Firebase BOM | 33.7.0 |
| Lifecycle | 2.8.7 |
| Dependency injection | Manual AppContainer |
| Gradle | 8.11.1 |
| Android Gradle Plugin | 8.10.1 |
| compileSdk / targetSdk | 35 |
| minSdk | 26 |

---

## Project Architecture

The project uses a simple layered structure with MVVM and the Repository Pattern.

```
com.mks.databilitynotifyapp/
├── DatabilityApp.kt
├── MainActivity.kt
├── di/
│   └── AppContainer.kt
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt
│   │   ├── NotificationDao.kt
│   │   └── NotificationEntity.kt
│   └── repository/
│       └── NotificationRepositoryImpl.kt
├── domain/
│   ├── model/
│   │   └── NotificationItem.kt
│   └── repository/
│       └── NotificationRepository.kt
├── notification/
│   ├── MyFirebaseMessagingService.kt
│   └── NotificationHelper.kt
├── presentation/
│   ├── navigation/
│   │   ├── Screen.kt
│   │   └── NavGraph.kt
│   ├── inbox/
│   │   ├── InboxScreen.kt
│   │   ├── InboxViewModel.kt
│   │   └── InboxUiState.kt
│   ├── details/
│   │   ├── NotificationDetailsScreen.kt
│   │   ├── NotificationDetailsViewModel.kt
│   │   └── NotificationDetailsUiState.kt
│   └── ViewModelFactory.kt
└── util/
    └── DateUtils.kt
```

| Component | Responsibility |
|---|---|
| `MyFirebaseMessagingService` | Receives FCM messages and processes their data |
| `NotificationHelper` | Creates the notification channel and displays system notifications |
| `NotificationEntity` | Defines the Room notification record |
| `NotificationDao` | Contains database queries and update/delete operations |
| `AppDatabase` | Provides the Room database singleton |
| `NotificationRepository` | Defines notification data operations (interface) |
| `NotificationRepositoryImpl` | Implements repository operations using Room |
| `InboxViewModel` | Exposes inbox state and handles inbox actions |
| `NotificationDetailsViewModel` | Loads a notification and marks it as read |
| `InboxScreen` | Displays the notification list and inbox actions |
| `NotificationDetailsScreen` | Displays the selected notification |
| `AppContainer` | Provides dependencies using manual dependency injection |

---

## Requirements

- Android Studio
- JDK 17 (recommended for this Gradle / Android Gradle Plugin setup)
- Android SDK Platform 35
- An Android device or emulator with Google Play services for FCM testing

---

## Clone and Build

### 1. Clone the repository

```bash
git clone https://github.com/monukr26/Datability_Notify_App.git
cd Datability_Notify_App
```

### 2. Configure Firebase

Follow [Firebase Project Setup](#firebase-project-setup) and place `google-services.json` inside the `app/` directory.

### 3. Build the debug APK

**Windows (PowerShell or Command Prompt):**
```powershell
.\gradlew.bat assembleDebug
```

**macOS / Linux:**
```bash
./gradlew assembleDebug
```

### 4. APK output

After a successful build:
```
app/build/outputs/apk/debug/app-debug.apk
```

Install this APK on an Android device to test the application.

---

## Firebase Project Setup

> The Firebase configuration file is **not included** in this public repository.

1. Open the [Firebase Console](https://console.firebase.google.com/).
2. Create a Firebase project or select an existing project.
3. Add an Android app using this exact package name:
   ```
   com.mks.databilitynotifyapp
   ```
4. Download the generated `google-services.json`.
5. Place it here:
   ```
   Datability_Notify_App/app/google-services.json
   ```
6. Sync or rebuild the Android project.

The build configuration applies the Google Services plugin when this file is present. A valid Firebase configuration is required for real FCM registration and message delivery.

### Get the device FCM registration token

1. Install and run the app on a device or emulator with Google Play services.
2. Open Android Studio **Logcat**.
3. Search for the tag `FCM_TOKEN`.
4. Copy the current device registration token for testing.

> **Keep this token private.** Do not add it to source code, README files, screenshots, or Git commits.

---

## How Notifications Are Processed

This app can be tested using two different FCM sending approaches. They are related, but not identical.

### Data-only FCM message

A data-only message carries custom key-value data. The app's `MyFirebaseMessagingService` processes the data in `onMessageReceived()`, saves the notification to Room, and asks `NotificationHelper` to display a system notification.

This is the **recommended method** for verifying that all five assignment payload fields are processed by the app.

### Firebase Console notification campaign

A Firebase Console notification campaign is primarily designed to send a user-visible notification. When a notification-type message is received while the app is in the background, Android/FCM may display it automatically rather than calling the app's `onMessageReceived()`.

When the user taps that notification, the app's notification-intent handling processes the available extras and saves the notification. The exact fields available depend on the message configured and sent by the console.

> **Important:** Seeing a system notification is not, by itself, proof that all custom data fields were saved to Room. Verify the Inbox entry and its details separately.

---

## Testing Notifications

### A. Test using Firebase Console Campaign

This test verifies the Firebase Console campaign and notification-tap flow.

1. Open the [Firebase Console](https://console.firebase.google.com/).
2. Select the Firebase project connected to the app.
3. Open **Messaging** and create a notification campaign.
4. Enter a notification title and description.
5. If the composer provides custom data fields, add the fields from [Expected Notification Payload](#expected-notification-payload).
6. Set the Android notification channel to `assignment` if the composer exposes that setting.
7. Use the console's test-send option and enter the device's FCM registration token.
8. Send the test notification.

**Verify on the Android device:**
- The system notification appears.
- Tapping it opens the Notification Inbox.
- The notification appears in the Inbox.
- Opening it displays the Details screen.
- Closing and reopening the app does not remove the entry.

> Firebase Console campaign behavior can differ from a data-only message. Use the Postman procedure below to verify the complete custom data payload independently.

---

### B. Test using Postman — Data-only FCM message

This test sends a data-only message through the Firebase Cloud Messaging HTTP v1 API. It verifies the custom payload handling path in `MyFirebaseMessagingService`.

#### Prerequisites

You need:
- Firebase Project ID
- Device FCM registration token
- OAuth 2.0 access token authorized for the Firebase Cloud Messaging API
- [Postman](https://www.postman.com/)
- A Google account with permission to send messages for the Firebase project

> The device FCM registration token and OAuth access token are different credentials.

---

#### Step 1: Get the Firebase Project ID

1. Open Firebase Console and select your project.
2. Open **Project settings → General**.
3. Copy the **Project ID**.

Example: `datability-notify-app`

---

#### Step 2: Generate an OAuth 2.0 access token

For manual testing, use [Google OAuth 2.0 Playground](https://developers.google.com/oauthplayground/):

1. Enter this custom scope:
   ```
   https://www.googleapis.com/auth/firebase.messaging
   ```
2. Click **Authorize APIs**, sign in with an account that has permission to send FCM messages for the project, and grant access.
3. In Step 2, click **Exchange authorization code for tokens**.
4. Copy the generated **Access Token** for use in Postman.

> The access token is short-lived. Generate a new one when it expires. If the Playground authorization method is unavailable, follow Google's [FCM HTTP v1 authorization documentation](https://firebase.google.com/docs/cloud-messaging/send/v1-api).

> **Never** paste your access token into this README, commit it to Git, or share it in screenshots.

---

#### Step 3: Create the Postman request

- **Method:** `POST`
- **URL:**
  ```
  https://fcm.googleapis.com/v1/projects/YOUR_PROJECT_ID/messages:send
  ```
  Replace `YOUR_PROJECT_ID` with your Firebase Project ID.

  Example:
  ```
  https://fcm.googleapis.com/v1/projects/datability-notify-app/messages:send
  ```

---

#### Step 4: Configure request headers

Open the **Headers** tab and add:

| Key | Value |
|---|---|
| `Authorization` | `Bearer YOUR_OAUTH_ACCESS_TOKEN` |
| `Content-Type` | `application/json` |

Replace `YOUR_OAUTH_ACCESS_TOKEN` with the access token from Step 2.

---

#### Step 5: Add the data-only JSON body

Open **Body → raw → JSON** and paste:

```json
{
  "message": {
    "token": "YOUR_DEVICE_FCM_TOKEN",
    "data": {
      "id": "notification_postman_001",
      "title": "Postman Test Notification",
      "description": "Testing custom FCM data payload for Datability assignment.",
      "bannerImg": "https://picsum.photos/600/300",
      "timestamp": "1791873000000"
    },
    "android": {
      "priority": "high"
    }
  }
}
```

Replace `YOUR_DEVICE_FCM_TOKEN` with the current registration token from your test device.

**Payload rules:**
- Keep all values inside `data` as strings.
- Use a new `id` for each new notification test.
- To test duplicate handling, send the same `id` again.
- The `timestamp` is milliseconds since the Unix epoch.
- Use a valid HTTPS image URL for the banner test.

---

#### Step 6: Send the request

Click **Send** in Postman.

A successful HTTP v1 send returns `HTTP 200 OK` and a response containing a `name` field. This means FCM accepted the send request — it does not, by itself, guarantee immediate device delivery.

---

#### Step 7: Verify on the Android device

- The Android system notification appears (when notification permission is granted).
- The app receives the data message.
- The notification is saved in the local Room database.
- The Inbox displays the title, description, banner image (if it loads), and readable timestamp.
- Tapping the inbox item opens Notification Details.
- Opening Details marks the notification as read.
- Restarting the app does not remove the saved notification.
- For duplicate testing, send the same `id` again and verify that the Inbox does **not** create a second entry.

---

#### Postman troubleshooting

| HTTP response / symptom | What to check |
|---|---|
| `401 Unauthorized` | Generate a fresh OAuth access token and check the Authorization header |
| `403 Forbidden` | Confirm the account has permission to send FCM messages and the FCM HTTP v1 API is enabled |
| `404 Not Found` | Verify the Firebase Project ID in the request URL |
| `400 Bad Request` | Check JSON structure, field names, and that all `data` values are strings |
| `200 OK`, but no device notification | Check device token, internet connection, Google Play services, app state, and notification permission |
| Notification appears but no Inbox entry | Inspect Logcat and verify `onMessageReceived()`, payload parsing, and Room insertion |

Official reference: [Firebase Cloud Messaging HTTP v1 API](https://firebase.google.com/docs/cloud-messaging/send/v1-api)

---

## Expected Notification Payload

The application supports the assignment's notification data format:

```json
{
  "id": "notification_101",
  "title": "New Notification",
  "description": "This is a sample notification for the assignment.",
  "bannerImg": "https://example.com/banner.jpg",
  "timestamp": "1791873000000"
}
```

| Field | Description |
|---|---|
| `id` | Unique identifier for the notification |
| `title` | Notification title |
| `description` | Notification description / body |
| `bannerImg` | Optional banner image URL |
| `timestamp` | Milliseconds since the Unix epoch, sent as a string in the FCM `data` payload |

The `id` is used as the Room primary key to prevent duplicate inbox records.

---

## Assumptions and Design Decisions

**Manual dependency injection**
The project uses a manual `AppContainer` rather than Hilt. This keeps dependency creation straightforward for a small assignment and avoids additional DI setup.

**Room for local persistence**
Room stores notification metadata and read state locally. This allows the inbox to remain available after the application is closed and reopened.

**Duplicate notification handling**
The notification `id` is the Room primary key. `OnConflictStrategy.IGNORE` prevents a second database row from being inserted when the same ID is received again. It also avoids overwriting the existing record's read state.

**FCM message handling**
Data-only FCM messages are processed by `MyFirebaseMessagingService`. Notification-type messages sent through Firebase Console follow a different background handling path; notification-tap handling covers the campaign flow supported by this implementation.

**Image handling**
Coil loads remote banner images. If the URL is missing or the image cannot be loaded, the UI displays a fallback icon while keeping notification text visible.

---

## Known Limitations

- **Google Play services:** FCM registration and delivery require a device or emulator with compatible Google Play services.
- **Network availability for remote images:** Notification metadata is stored locally, but a remote banner that has not been loaded may not be available offline. The UI displays a fallback when loading fails.
- **FCM delivery conditions:** Android background restrictions, device state, message priority, and force-stop behavior can affect delivery. FCM acceptance by the server does not guarantee immediate device delivery.
- **Local-only inbox:** Notification history is stored on the device in Room; it is not synchronized between devices.
- **Different FCM paths:** Firebase Console campaigns and data-only HTTP v1 messages have different handling paths. Both should be tested when validating the app.

---

## Submission Notes

The assignment submission includes:

- Complete Android source code in the GitHub repository.
- A buildable Android APK.
- This README with project overview, setup, Firebase configuration, technologies, testing instructions, assumptions, and limitations.

**Repository:** [Datability Notify App](https://github.com/monukr26/Datability_Notify_App)

---

### Security checklist before pushing to GitHub

- [ ] Do not commit `app/google-services.json` — it is excluded by the project's `.gitignore`.
- [ ] Never commit FCM registration tokens.
- [ ] Never commit OAuth access tokens or refresh tokens.
- [ ] Never commit service-account JSON private keys.
- [ ] Review staged files before every commit.

---

## License

Add a license here if you intend to distribute this project beyond the assignment.
