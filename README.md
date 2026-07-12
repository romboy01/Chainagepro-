# Chainage Navigator

Chainage Navigator is a modern drayage and fleet logistics Android application built with **Kotlin** and **Jetpack Compose**. It provides real-time GPS tracking, weighbridge job syncing, and notifications for dray operations.

---

## Technical Stack & Architecture

- **UI Framework:** Jetpack Compose (Material Design 3 styling, Edge-to-Edge window insets)
- **State Management:** MVVM Architecture with Jetpack `ViewModel` and `MutableStateFlow`
- **Data Persistence:** Offline-first Room SQLite Database
- **Background Tasks:** WorkManager for asynchronous syncing and Location Service for foreground tracking
- **Push Notifications:** Firebase Cloud Messaging (FCM) integration
- **Config Management:** Secrets Gradle Plugin for secure API keys (via `.env`)

---

## Local Build Instructions

You do not need Android Studio to build this project. You only need **JDK 17** installed on your system.

### 1. Set Up Environment Variables
Create a `.env` file at the root of the project (copying from `.env.example` as a template) and supply any required integration keys:
```bash
cp .env.example .env
```

### 2. Build Debug and Release APKs
Use the standard Gradle wrapper command:

- **Generate Debug APK:**
  ```bash
  ./gradlew assembleDebug
  ```
  *Output location:* `app/build/outputs/apk/debug/app-debug.apk`

- **Generate Release APK:**
  ```bash
  ./gradlew assembleRelease
  ```
  *Output location:* `app/build/outputs/apk/release/app-release.apk`

### 3. Run Unit and Robolectric Tests
To run local JVM tests and screenshot verifications:
```bash
./gradlew :app:testDebugUnitTest
```

---

## GitHub Actions CI/CD Integration

We have set up an automated CI/CD workflow under `.github/workflows/android.yml` that triggers on every push and pull request to the `main` branch.

### CI Workflow Steps
1. **Code Checkout:** Checks out the latest source code.
2. **Java 17 Setup:** Installs Temurin JDK 17 with Gradle action caching.
3. **Gradle Configuration:** Uses the checked-in Gradle wrapper.
4. **Keystore Decoding:** Decodes `debug.keystore.base64` into a localized `debug.keystore` file to assure signing integrity.
5. **Debug Compilation:** Compiles the debug variant using `./gradlew assembleDebug`.
6. **Release Compilation:** Compiles the signed release variant using `./gradlew assembleRelease`.
7. **Artifact Upload:** Uploads both `app-debug.apk` and `app-release.apk` as accessible run artifacts in GitHub Actions.

### Customizing Release Signing in GitHub
By default, if you don't configure any secrets, the release build will gracefully **fall back to signing with the debug keystore** so your build succeeds out-of-the-box in GitHub Actions.

To sign your release builds with a production key in GitHub Actions, configure the following **Secrets** in your GitHub Repository under `Settings -> Secrets and Variables -> Actions`:

| Secret Name | Description |
|---|---|
| `KEYSTORE_PATH` | The relative path to your production `.jks` or `.keystore` file (or set via repository file check-in) |
| `STORE_PASSWORD` | The store password for your production keystore |
| `KEY_ALIAS` | The alias name of your production key |
| `KEY_PASSWORD` | The key password for your production key |

---

## Code Quality & Verification

To keep the project clean, always check that the compilation is clean before committing:
```bash
./gradlew compileDebugKotlin
```
