# Remote Update Demo

## Purpose

This project is a sample native Android application and companion backend service used specifically to learn, design, test, and automate a private APK remote-update system and CI/CD release workflow:

* **Android MVVM Architecture**: Strict separation of concerns (View -> ViewModel -> Repository -> API Client -> Backend).
* **Local LAN Communication**: The Android application queries the version control server running in Docker on your local network (`http://192.168.68.78:8080/`).
* **Internet Updates via GitHub Actions**: Compiled, signed APK binaries and release manifests originate from GitHub Actions workflows and are served globally via GitHub Releases.
* **APK Signing**: Secure keystore management without exposing secrets in source code.
* **APK Distribution**: Fast metadata checking over LAN + immutable global APK asset delivery over the Internet.
* **Remote Update Checking**: Comparing installed `versionCode` against authoritative server manifests.
* **In-App APK Updates**: Streaming downloads, SHA-256 integrity verification, and handoff to Android's `PackageInstaller`.

---

## Current Version

```text
0.0.0.1
```
* **Application ID**: `com.example.remoteupdatedemo`
* **Version Name**: `0.0.0.1`
* **Version Code**: `1`

---

## Current Status

```text
Initial application created.
Remote update functionality is not implemented yet.
```

The application features a clean, responsive Jetpack Compose Material 3 UI displaying the dynamic build configuration version, app logo, and backend connectivity indicator. Pressing **Check for Update** queries the custom backend service and displays the designated initial status message:
> `Remote update functionality will be added later.`

---

## Hybrid Network Architecture: LAN Coordination + Internet Delivery

```text
┌────────────────────────────────────────────────────────┐
│                   GitHub Repository                    │
│   (https://github.com/perfectmens/CICD-application)    │
│                           │                            │
│                           ▼ Git Release Tag (e.g. v0.0.0.2)
│                  GitHub Actions Runner                 │
│         • Compiles & signs APK                         │
│         • Computes SHA-256                             │
│         • Creates GitHub Release                       │
│         • Notifies Backend Service with release info   │
│                           │                            │
└───────────────────────────┼────────────────────────────┘
                            │
              ┌─────────────┴─────────────┐
              │                           │ (Publish Release)
              ▼                           ▼
┌───────────────────────────┐   ┌───────────────────────────┐
│     Docker Backend        │   │      GitHub Releases      │
│  (192.168.68.78:8080 LAN) │   │     (Internet / Public)   │
│                           │   │                           │
│  Maintains authoritative  │   │  Hosts immutable APK:     │
│  version manifest:        │   │  https://github.com/.../  │
│  latest: v0.0.0.2           │   │  releases/download/v0.0.0.2/│
│  downloadUrl: GitHub URL  │   │  app-release-v0.0.0.2.apk   │
└─────────────┬─────────────┘   └─────────────┬─────────────┘
              │                               │
              │ 1. Check for Update (LAN)     │ 2. Download APK (Internet)
              ▼                               ▼
┌───────────────────────────────────────────────────────────┐
│                   Android Application                     │
│                (com.example.remoteupdatedemo)             │
│                                                           │
│  1. Inquires Backend via LAN (192.168.68.78:8080)        │
│  2. Receives update manifest pointing to GitHub Release   │
│  3. Downloads newer APK via Internet from GitHub Release  │
│  4. Verifies SHA-256 & triggers Android installer         │
└───────────────────────────────────────────────────────────┘
```

---

## Future Learning Stages

```text
1. Create basic Android application
2. Push application to private GitHub repository
3. Add GitHub Actions CI
4. Add automated testing
5. Add lint validation
6. Create production signing configuration
7. Secure signing credentials
8. Build signed APK using GitHub Actions
9. Create GitHub Releases
10. Create version/update metadata
11. Implement Update button
12. Download APK from the application
13. Trigger Android installation
14. Test upgrade from 0.0.0.1 → 0.0.0.2
15. Test rollback/recovery scenarios
```

---

## API Governance (`api-mapping.yaml`)

Every API contract is defined in `api-mapping.yaml` as the single source of truth:

| Contract ID | HTTP Method | Path | Purpose |
| :--- | :--- | :--- | :--- |
| `system.health.check` | `GET` | `/api/v1/health` | Validates backend connectivity over LAN (`192.168.68.78:8080`). |
| `version.update.check` | `GET` | `/api/v1/version/check` | Compares client `version_code` against server release manifest. |
| `version.metadata.latest`| `GET` | `/api/v1/updates/latest` | Retrieves full changelog and metadata for latest release. |
| `version.github.publish`| `POST` | `/api/v1/github/release-published` | Webhook invoked by GitHub Actions when an APK is released. |
| `version.admin.update` | `POST` | `/api/v1/admin/version` | Simulator endpoint to bump version (e.g. simulate v0.0.0.2). |
| `version.download.apk` | `GET` | `/api/v1/updates/download/latest.apk` | Local fallback APK binary download endpoint. |

To validate API governance:
```bash
python "C:\Users\Admin B\.gemini\config\skills\api-governance\scripts\validate-api-mapping.py" --file api-mapping.yaml --strict
```

To validate MVVM architecture compliance:
```bash
python "C:\Users\Admin B\.gemini\config\skills\mobile-mvvm-architecture\scripts\validate-mvvm.py" --dir app --strict
```

---

## Running the Backend in Docker

The backend runs as a containerized FastAPI service accessible across your LAN at `http://192.168.68.78:8080/`:

```bash
# Start backend in background
docker compose up -d --build

# Verify container is running
docker ps

# Check backend health over LAN
curl http://192.168.68.78:8080/api/v1/health

# Run backend unit test suite
python backend/test_main.py
```

### Simulating a GitHub Actions Release (Internet Download URL)
You can test the client's "Update Available" screen at any time by simulating GitHub Actions publishing a new release:
```bash
# Simulate GitHub Actions publishing version 0.0.0.2 to GitHub Releases
curl -X POST http://192.168.68.78:8080/api/v1/github/release-published \
  -H "Content-Type: application/json" \
  -d '{"versionName": "0.0.0.2", "versionCode": 2, "releaseNotes": "Built and signed via GitHub Actions", "downloadUrl": "https://github.com/perfectmens/CICD-application/releases/download/v0.0.0.2/app-release-v0.0.0.2.apk"}'

# Reset back to baseline v0.0.0.1
curl -X POST http://192.168.68.78:8080/api/v1/admin/reset
```

---

## Building and Running the Android App

### Gradle Build Commands
```bash
# Run JVM Unit Tests
./gradlew testDebugUnitTest

# Run Android Lint Code Analysis
./gradlew lintDebug

# Build Debug APK
./gradlew assembleDebug
```
The compiled APK will be at:
`app/build/outputs/apk/debug/app-debug.apk`

### Network Connectivity
* **Physical Device on Wi-Fi**: The app automatically defaults to `http://192.168.68.78:8080/`. Tap the status pill on the screen to view or change settings anytime.
* **Android Emulator**: In the settings dialog, tap the **Emulator (10.0.2.2)** preset button.
