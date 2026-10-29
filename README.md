# Datability — Firebase Notification Inbox

A production-conscious native Android application that integrates **Firebase Cloud Messaging (FCM)** and provides a locally maintained, persistent **Notification Inbox** using **Jetpack Compose (Material 3)**, **MVVM architecture**, and the **Repository Pattern** with **Room**.

---

## 📱 Features

- **Push Notifications (FCM)**:
  - Receives push notifications from Firebase Cloud Messaging.
  - Formats and displays received notifications as Android system notifications.
  - Configured with the required notification channel:
    - **Channel ID**: `assignment`
    - **Channel Name**: `Assignment`
  - System notification displays: App icon, Title, Description, and a `PendingIntent` navigating to the Inbox.
  - Full support for Android 13+ (API 33+) `POST_NOTIFICATIONS` runtime permission.
- **Local Persistence (Room Database)**:
  - SQLite storage powered by Room ensures notification history survives app restarts and reboots.
  - Automatically prevents duplicate records using primary key `id` with `OnConflictStrategy.IGNORE` (preventing duplicates and preserving existing read states).
- **Notification Inbox (Jetpack Compose & Material 3)**:
  - Reactive list observation using Kotlin Coroutines `Flow` and `collectAsStateWithLifecycle()`.
  - Ordered by timestamp descending (newest first).
  - **Horizontal Card Layout**: Left square thumbnail image, middle title/description/timestamp, and right delete button.
  - **Unread Notification Count**: Dynamic badge in the TopAppBar driven directly by a Room reactive query.
  - **Mark All as Read**: TopAppBar action to mark all notifications as read in a single click (automatically hides when no unread notifications exist).
  - **Delete Individual Notifications**: Quick delete button on each card removing the entry from SQLite and updating the inbox immediately.
  - **Clear Notification Inbox**: TopAppBar action with confirmation `AlertDialog` before clearing all history.
  - **Empty-State UI**: Centered "No notifications yet" prompt when the inbox is cleared.
- **Notification Details Screen**:
  - Displays full details: full-size banner image with loading/fallback states, headline title, complete scrollable description, and formatted timestamp.
  - Automatically marks the notification as **Read** in Room upon opening (executed once to prevent repeated DB operations on recomposition).
  - Navigation updates the inbox state immediately via reactive Flow streams.
- **Error & Edge-Case Handling**:
  - Missing or invalid banner images gracefully fall back to an image placeholder without obstructing text content.
  - Missing optional fields and non-numeric timestamps are safely defaulted without crashing.
  - Foreground and background FCM messages (including tray taps) are captured and saved to Room.

---

## 🏛 Architecture & Tech Stack

```
com.mks.databilitynotifyapp/
├── DatabilityApp.kt                  # Application class (creates channel, initializes AppContainer)
├── MainActivity.kt                   # Single Activity host & background intent handler
├── di/
│   └── AppContainer.kt               # Clean, manual dependency injection container
├── data/
│   ├── local/                        # Room SQLite persistence
│   │   ├── AppDatabase.kt            # RoomDatabase definition
│   │   ├── NotificationDao.kt        # DAO with queries & IGNORE insert conflict strategy
│   │   └── NotificationEntity.kt     # SQLite table entity
│   └── repository/
│       └── NotificationRepositoryImpl.kt # Concrete repository with entity <-> domain mapping
├── domain/
│   ├── model/
│   │   └── NotificationItem.kt       # Pure domain data class
│   └── repository/
│       └── NotificationRepository.kt # Repository interface
├── notification/
│   ├── MyFirebaseMessagingService.kt # FCM service parsing payload & persisting to Room
│   └── NotificationHelper.kt         # System notification builder & channel creator
├── presentation/
│   ├── navigation/
│   │   ├── Screen.kt                 # Sealed routes (Inbox, Details/{id})
│   │   └── NavGraph.kt               # Jetpack Compose NavHost
│   ├── inbox/
│   │   ├── InboxScreen.kt            # Compose Material 3 Inbox UI
│   │   ├── InboxViewModel.kt         # State holder with StateFlow
│   │   └── InboxUiState.kt           # UI State (Loading, Empty, Success)
│   ├── details/
│   │   ├── NotificationDetailsScreen.kt   # Compose Material 3 Details UI
│   │   ├── NotificationDetailsViewModel.kt # Marks as read & loads by ID
│   │   └── NotificationDetailsUiState.kt  # UI State (Loading, NotFound, Success)
│   └── ViewModelFactory.kt           # ViewModel factories for manual DI
└── util/
    └── DateUtils.kt                  # Date/time formatting helper
```

### Libraries Used

- **Language**: Kotlin `2.0.21`
- **UI Toolkit**: Jetpack Compose with Material 3 (Compose BOM `2024.09.00`)
- **Navigation**: Jetpack Navigation Compose `2.8.5`
- **Architecture**: Android Architecture Components (Lifecycle `2.8.7`, ViewModel Compose)
- **Local Database**: AndroidX Room `2.6.1` with Kotlin Symbol Processing (**KSP** `2.0.21-1.0.28`)
- **Image Loading**: Coil Compose `2.7.0`
- **Push Messaging**: Firebase Cloud Messaging (Firebase BOM `33.7.0`)
- **Build System**: Gradle `8.11.1`, AGP `8.10.1`, compileSdk & targetSdk `35`, minSdk `26`

---

## 🚀 Setup & Build Instructions

### Prerequisites
- **Android Studio**: Ladybug / Hedgehog / Iguana or later
- **JDK**: Java 17 or Java 21/22
- **Android SDK**: API Level 35 (Android 15)

### Building the Project
1. Clone the repository:
   ```bash
   git clone https://github.com/monukr26/Datability_Notify_App.git
   cd Datability_Notify_App
   ```
2. Build the debug APK using the Gradle wrapper:
   - On Windows:
     ```powershell
     .\gradlew.bat assembleDebug
     ```
   - On macOS/Linux:
     ```bash
     ./gradlew assembleDebug
     ```
3. The generated APK will be available at:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 🔥 Firebase Setup Instructions

> ⚠️ **Note**: As per security guidelines and assignment requirements, `google-services.json` is ignored in `.gitignore` and must not be committed to public version control.

To connect your own Firebase project:
1. Go to the [Firebase Console](https://console.firebase.google.com/) and create a new project.
2. Add an Android app with Package Name:
   ```
   com.mks.databilitynotifyapp
   ```
3. Download the generated `google-services.json` file.
4. Place `google-services.json` in the `app/` directory (`Datability/app/google-services.json`).
5. Rebuild or run the app. The build script automatically detects the file and applies the Google Services plugin!

---

## 🧪 Testing Push Notifications

### 1. Retrieve Device FCM Token
Run the app on a physical device or an emulator with Google Play Services. Look for the token printed in **Logcat**:
```
D/FCM_TOKEN: ==================================================
D/FCM_TOKEN: CURRENT FCM TOKEN: <YOUR_DEVICE_TOKEN>
D/FCM_TOKEN: ==================================================
```

### 2. Supported Notification Payload
The app supports the assignment's exact JSON format:
```json
{
  "id": "notification_101",
  "title": "New Notification",
  "description": "This is a sample notification for the assignment.",
  "bannerImg": "https://picsum.photos/600/300",
  "timestamp": "1791873000000"
}
```

### 3. Sending via Firebase Console Campaign
1. In Firebase Console, go to **Messaging** → **New campaign** → **Notifications**.
2. Fill in the Title and Description.
3. Under **Step 5 (Additional options) → Custom data**, add:
   - `id`: `test_notification_01` (use a unique ID for each notification)
   - `title`: `My Test Title`
   - `description`: `Test description for Datability inbox.`
   - `bannerImg`: `https://picsum.photos/600/300`
   - `timestamp`: `1791873000000` *(optional; defaults to arrival time)*
4. Click **Send test message**, paste your FCM token, and send.

---

## 💡 Assumptions & Design Decisions

1. **Manual Dependency Injection (`AppContainer`)**:
   - Rather than introducing Hilt and additional annotation processing overhead with Kotlin 2.0.21, manual dependency injection via `AppContainer` was used. This ensures simple, deterministic, and easily explainable code during technical discussions.
2. **Duplicate Notification Handling**:
   - The notification `id` is the primary key in Room. Using `OnConflictStrategy.IGNORE` ensures that if a duplicate ID arrives, the database does not overwrite an existing read status (`isRead = true`) or duplicate the row.
3. **Background FCM Delivery**:
   - Both pure `data` payloads (processed via `MyFirebaseMessagingService`) and Firebase Console campaigns (opened via notification tray tap) are captured and saved to Room.
4. **Resilient Image Handling**:
   - Coil's `SubcomposeAsyncImage` displays a custom placeholder whenever an image is missing or unreachable, ensuring that titles and descriptions always remain accessible.

---

## ⚠️ Known Limitations

1. **Google Play Services Dependency**:
   - Push notifications require Google Play Services on the host device or emulator. Running on pure AOSP emulators without Google Play will prevent FCM registration.
2. **Image Caching on Offline Cold Start**:
   - While notification metadata and read states are persisted locally in SQLite, Coil caches remote banner images in the HTTP cache. If the device is offline when viewing a notification whose banner was never loaded, the fallback placeholder is displayed.

