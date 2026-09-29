---
name: android-ci-cd
family: software-delivery
description: Comprehensive CI/CD automation, pipeline engineering, and release governance skill for Android applications. Use this skill whenever designing, building, troubleshooting, or optimizing Android Continuous Integration (Gradle builds, unit tests, Android Lint, static analysis, dependency scanning, build caching) and Continuous Delivery/Deployment (GitHub Actions workflows, keystore signing, GitHub Releases, Google Play publishing, Firebase App Distribution, direct APK delivery, in-app updates, Room database migrations, and rollback/fix-forward strategies). Supports both Public and Private GitHub repositories.
---

# Android CI/CD Pipeline & Release Engineering Skill

An end-to-end engineering standard and operational guide for automating Continuous Integration (CI) and Continuous Delivery/Deployment (CD) for Android applications across **Public and Private** repositories.

---

## 1. System Architecture & The Two-Track Pipeline

A production Android pipeline strictly separates **Continuous Integration** (pull requests and verification) from **Continuous Delivery** (immutable releases and distribution):

```text
┌────────────────────────────────────────────────────────────────────────┐
│                        CONTINUOUS INTEGRATION (CI)                     │
│  Trigger: Pull Request / Branch Push to main                           │
│                                                                        │
│  Checkout ──► Set up JDK/SDK ──► Gradle Cache ──► Lint & Detekt        │
│                                                        │               │
│  CI Artifacts ◄── Assemble Debug ◄── Unit Tests ◄──────┘               │
│  (Test Reports)   (Build Verification)                                 │
└────────────────────────────────────────────────────────────────────────┘
                                   │
                                   │ PR Approved & Merged
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        CONTINUOUS DELIVERY (CD)                        │
│  Trigger: Git Release Tag (v*.*.*) / Manual Dispatch                   │
│                                                                        │
│  Validate Tag ──► Decode Keystore ──► Assemble Release (APK/AAB)       │
│                                              │                         │
│  Publish ◄── SHA-256 & version.json ◄── apksigner (V2/V3/V4) ◄────────┘
│                                              │
│        ┌─────────────────────────────────────┼─────────────────────────┐
│        ▼                                     ▼                         ▼
│  Track A: Direct APK                 Track B: Google Play      Track C: Firebase
│  GitHub Releases +                   Internal / Alpha /        App Distribution
│  version.json (Public / Private)     Production Track          (QA Testers)
└────────────────────────────────────────────────────────────────────────┘
```

### Core Tenets of Android CI/CD
1. **Never sign production binaries in CI on PRs**: Pull request builds only compile debug variants and run tests. Release signing keys are strictly reserved for protected release jobs.
2. **Build Once, Promote Everywhere**: An APK or AAB compiled from a specific Git tag must be the exact binary promoted through testing to production. Never rebuild from source for different release stages.
3. **Every Release is Traceable**: Every published artifact must map directly to an immutable Git commit, a monotonic integer `versionCode`, a SemVer `versionName`, and a SHA-256 cryptographic checksum.
4. **Android Rejects Downgrades**: Android OS blocks installing older APKs over newer ones (`INSTALL_FAILED_VERSION_DOWNGRADE`). Rollbacks are executed as **fix-forward releases** with an incremented `versionCode`.
5. **Data Preservation Over Code**: App updates must never destroy local SQLite/Room databases, DataStore preferences, or user files.
6. **No Client Secrets in Private Repos**: When distributing private repository APKs, the client app must NEVER contain GitHub Personal Access Tokens (PAT). Distribution must occur through an authenticated proxy backend.

---

## 2. Public vs. Private Repository Distribution Architecture

Direct APK distribution differs fundamentally between public and private repositories:

```text
=== PUBLIC REPOSITORY ARCHITECTURE ===
[Android App] ──────────► GET api.github.com/repos/{owner}/{repo}/releases/latest
      │                   (Returns public metadata + browser_download_url)
      ▼
[Android App] ──────────► Direct Download from GitHub CDN (No token needed)
      │
      ▼
Verify SHA-256 ──► Install via FileProvider


=== PRIVATE REPOSITORY ARCHITECTURE (SECURE PROXY PATTERN) ===
[Android App] ──────────► GET https://backend.myorg.com/api/v1/app/version
                                  │
                                  ▼
                            [Proxy Backend] (Holds GITHUB_TOKEN securely in env)
                                  │ Calls GitHub API with Bearer Token
                                  ▼
[Android App] ◄────────── Returns version metadata (versionCode, SHA-256, downloadUrl)
      │
      ▼
[Android App] ──────────► GET https://backend.myorg.com/api/v1/app/download
                                  │
                                  ▼
                            [Proxy Backend]
                            Streams binary from:
                            GET api.github.com/.../releases/assets/{asset_id}
                            Header: Authorization: Bearer ${GITHUB_TOKEN}
                            Header: Accept: application/octet-stream
                                  │
[Android App] ◄────────── Streams APK bytes to local storage
      │
      ▼
Verify SHA-256 ──► Install via FileProvider
```

### Why the Proxy Pattern is Mandatory for Private Repos
- GitHub Release downloads for private repos require `Authorization: Bearer <GITHUB_TOKEN>` with `Accept: application/octet-stream`.
- Direct links (`browser_download_url`) return `404 Not Found` to unauthenticated browsers or apps.
- Decompiling an Android APK takes seconds (`jadx`). Any token placed in `BuildConfig`, strings, or assets is immediately compromised.
- The proxy backend acts as a security boundary: authenticates client requests, retrieves the asset using server-side secrets, and streams the binary to the device.

---

## 3. Repository & Keystore Foundation

### `.gitattributes` Binary Enforcement (CRITICAL)
Git line-ending conversions (CRLF ↔ LF) will corrupt binary keystores and cause `INSTALL_FAILED_UPDATE_INCOMPATIBLE` ("package conflicts with existing package").
Create `.gitattributes` in the repository root before adding any binary files:

```gitattributes
*.keystore binary
*.jks binary
*.p12 binary
*.apk binary
*.aab binary
*.jar binary
```

### Keystore Generation & Base64 Secret Export
1. **Generate Production Keystore**:
   ```bash
   keytool -genkeypair -v -keystore release.keystore \
     -alias mykeyalias -keyalg RSA -keysize 2048 -validity 10000 \
     -storepass "MyStrongPassword123" -keypass "MyStrongPassword123" \
     -dname "CN=My Org, OU=Engineering, O=My Org, L=City, S=State, C=US"
   ```

2. **Encode to Base64 (Single Line)**:
   - **Linux / macOS**:
     ```bash
     base64 -w 0 release.keystore > keystore_base64.txt
     ```
   - **Windows PowerShell**:
     ```powershell
     [Convert]::ToBase64String([IO.File]::ReadAllBytes('release.keystore')) | Out-File -Encoding ascii -NoNewline keystore_base64.txt
     ```

3. **Verify Fingerprint Before Uploading**:
   ```bash
   keytool -list -v -keystore release.keystore -alias mykeyalias -storepass "MyStrongPassword123"
   ```
   Save the SHA-256 certificate fingerprint in your records. Every future release must have the identical fingerprint.

### `app/build.gradle.kts` Configuration
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.myapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myapp"
        minSdk = 26
        targetSdk = 35

        versionCode = project.findProperty("versionCode")?.toString()?.toIntOrNull() ?: 1
        versionName = project.findProperty("versionName")?.toString() ?: "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_PATH") ?: "dummy.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (System.getenv("KEYSTORE_PATH") != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }
}
```

---

## 4. Continuous Integration (CI) Workflow

File: `.github/workflows/ci.yml`
Validates code on PRs and main merges without exposing release credentials.

```yaml
name: Android CI

on:
  pull_request:
    branches: [ main, develop ]
  push:
    branches: [ main ]

concurrency:
  group: ci-${{ github.ref }}
  cancel-in-progress: true

jobs:
  validate-and-test:
    runs-on: ubuntu-latest
    timeout-minutes: 30

    steps:
      - name: Checkout Source Code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v3
        with:
          cache-read-only: ${{ github.ref != 'refs/heads/main' }}

      - name: Make Gradle Wrapper Executable
        run: chmod +x gradlew

      - name: Run Static Analysis & Lint
        run: ./gradlew lintDebug --stacktrace

      - name: Run JVM Unit Tests
        run: ./gradlew testDebugUnitTest --continue

      - name: Build Verification (Debug APK)
        run: ./gradlew assembleDebug

      - name: Upload Test Reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-results
          path: app/build/reports/tests/testDebugUnitTest/
          retention-days: 7
```

---

## 5. Continuous Delivery (CD) Workflow

File: `.github/workflows/release.yml`
Handles tagged releases (`v*.*.*`) and manual dispatch. Generates release binaries, verifies signatures, creates `version.json` metadata, and publishes assets. Works for both public and private repositories.

```yaml
name: Android CD Release

on:
  push:
    tags:
      - 'v*'
  workflow_dispatch:
    inputs:
      version_name:
        description: 'Semantic Version Name (e.g. 1.0.1)'
        required: true
        default: '1.0.1'
      version_code:
        description: 'Monotonic Version Code (leave empty to use git commit count)'
        required: false
        default: ''

permissions:
  contents: write

jobs:
  build-and-publish:
    runs-on: ubuntu-latest
    timeout-minutes: 40

    steps:
      - name: Checkout Tagged Commit
        uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v3

      - name: Make Gradle Wrapper Executable
        run: chmod +x gradlew

      - name: Resolve Release Versions
        id: versioning
        run: |
          if [ "${{ github.event_name }}" = "workflow_dispatch" ] && [ -n "${{ github.event.inputs.version_code }}" ]; then
            V_CODE="${{ github.event.inputs.version_code }}"
          else
            V_CODE=$(git rev-list --count HEAD)
          fi

          if [ "${{ github.event_name }}" = "workflow_dispatch" ]; then
            V_NAME="${{ github.event.inputs.version_name }}"
          else
            V_NAME="${GITHUB_REF#refs/tags/v}"
          fi

          echo "version_name=$V_NAME" >> $GITHUB_OUTPUT
          echo "version_code=$V_CODE" >> $GITHUB_OUTPUT
          echo "Building versionName: $V_NAME, versionCode: $V_CODE"

      - name: Decode Production Keystore
        env:
          KEYSTORE_BASE64: ${{ secrets.KEYSTORE_BASE64 }}
        run: |
          echo "$KEYSTORE_BASE64" | base64 --decode > /tmp/release.keystore

      - name: Assemble Release APK and AAB
        env:
          KEYSTORE_PATH: /tmp/release.keystore
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
        run: |
          ./gradlew assembleRelease bundleRelease \
            -PversionName="${{ steps.versioning.outputs.version_name }}" \
            -PversionCode="${{ steps.versioning.outputs.version_code }}"

      - name: Securely Wipe Keystore
        if: always()
        run: rm -f /tmp/release.keystore

      - name: Package Artifacts & Calculate Checksums
        id: artifacts
        run: |
          VERSION="${{ steps.versioning.outputs.version_name }}"
          CODE="${{ steps.versioning.outputs.version_code }}"
          APK_SRC=$(find app/build/outputs/apk/release/ -name "*.apk" | head -n 1)
          AAB_SRC=$(find app/build/outputs/bundle/release/ -name "*.aab" | head -n 1)
          MAPPING_SRC="app/build/outputs/mapping/release/mapping.txt"

          APK_DEST="app-release-v${VERSION}.apk"
          AAB_DEST="app-release-v${VERSION}.aab"

          cp "$APK_SRC" "$APK_DEST"
          cp "$AAB_SRC" "$AAB_DEST"

          APK_SHA256=$(sha256sum "$APK_DEST" | awk '{print $1}')
          AAB_SHA256=$(sha256sum "$AAB_DEST" | awk '{print $1}')

          # Generate standardized version.json manifest
          cat <<EOF > version.json
          {
            "versionCode": $CODE,
            "versionName": "$VERSION",
            "sha256": "$APK_SHA256",
            "apkFileName": "$APK_DEST",
            "releaseNotes": "Release v$VERSION (Build $CODE)"
          }
          EOF

          echo "apk_path=$APK_DEST" >> $GITHUB_OUTPUT
          echo "aab_path=$AAB_DEST" >> $GITHUB_OUTPUT
          echo "apk_sha256=$APK_SHA256" >> $GITHUB_OUTPUT
          echo "mapping_path=$MAPPING_SRC" >> $GITHUB_OUTPUT

      - name: Verify Signature with apksigner
        run: |
          $ANDROID_HOME/build-tools/34.0.0/apksigner verify --verbose "${{ steps.artifacts.outputs.apk_path }}"

      - name: Publish GitHub Release
        uses: softprops/action-gh-release@v2
        with:
          name: "Release v${{ steps.versioning.outputs.version_name }}"
          tag_name: ${{ github.ref_name }}
          files: |
            ${{ steps.artifacts.outputs.apk_path }}
            ${{ steps.artifacts.outputs.aab_path }}
            ${{ steps.artifacts.outputs.mapping_path }}
            version.json
          body: |
            ## Android Release v${{ steps.versioning.outputs.version_name }} (Code: ${{ steps.versioning.outputs.version_code }})
            - **APK:** `${{ steps.artifacts.outputs.apk_path }}`
            - **SHA-256:** `${{ steps.artifacts.outputs.apk_sha256 }}`
            - **Commit:** `${{ github.sha }}`
          draft: false
          prerelease: false

      - name: Notify Backend (Optional / Best Effort)
        if: env.BACKEND_WEBHOOK_URL != ''
        env:
          BACKEND_WEBHOOK_URL: ${{ secrets.BACKEND_WEBHOOK_URL }}
        continue-on-error: true
        run: |
          curl -s -X POST "$BACKEND_WEBHOOK_URL" \
            -H "Content-Type: application/json" \
            -d @version.json || echo "Webhook notification skipped or unreachable"
```

---

## 6. Backend Integration Patterns (Public vs. Private)

### FastAPI Proxy Implementation (Supports Public & Private Repos)
```python
import os
import httpx
from fastapi import FastAPI, HTTPException
from fastapi.responses import StreamingResponse
from contextlib import asynccontextmanager

GITHUB_REPO = os.getenv("GITHUB_REPO", "myorg/my-android-app")
GITHUB_TOKEN = os.getenv("GITHUB_TOKEN", "")  # Required for private repos, optional for public
IS_PRIVATE = os.getenv("IS_PRIVATE_REPO", "false").lower() == "true"

state = {
    "versionCode": 1,
    "versionName": "1.0.0",
    "downloadUrl": "",
    "sha256": "",
    "assetId": None
}

def get_headers():
    headers = {"Accept": "application/vnd.github.v3+json"}
    if GITHUB_TOKEN:
        headers["Authorization"] = f"Bearer {GITHUB_TOKEN}"
    return headers

async def sync_latest_release():
    url = f"https://api.github.com/repos/{GITHUB_REPO}/releases/latest"
    async with httpx.AsyncClient() as client:
        resp = await client.get(url, headers=get_headers())
        if resp.status_code != 200:
            return
        data = resp.json()
        
        # Locate APK and version.json assets
        apk_asset = next((a for a in data.get("assets", []) if a["name"].endswith(".apk")), None)
        version_asset = next((a for a in data.get("assets", []) if a["name"] == "version.json"), None)
        
        if version_asset:
            v_headers = get_headers()
            if GITHUB_TOKEN:
                v_headers["Accept"] = "application/octet-stream"
            v_resp = await client.get(version_asset["url"] if GITHUB_TOKEN else version_asset["browser_download_url"], headers=v_headers)
            if v_resp.status_code == 200:
                v_data = v_resp.json()
                state["versionCode"] = v_data.get("versionCode", 1)
                state["versionName"] = v_data.get("versionName", data.get("tag_name", "").lstrip("v"))
                state["sha256"] = v_data.get("sha256", "")

        if apk_asset:
            state["assetId"] = apk_asset["id"]
            if IS_PRIVATE:
                # App downloads via backend proxy endpoint
                state["downloadUrl"] = "/api/v1/app/download"
            else:
                # App downloads directly from GitHub CDN
                state["downloadUrl"] = apk_asset["browser_download_url"]

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Auto-sync state on container/server startup
    await sync_latest_release()
    yield

app = FastAPI(lifespan=lifespan)

@app.get("/api/v1/app/version")
async def get_version():
    return state

@app.get("/api/v1/app/download")
async def proxy_download():
    """Streams APK from private GitHub release without exposing token to mobile client."""
    if not state["assetId"]:
        raise HTTPException(status_code=404, detail="No release asset available")
    
    asset_url = f"https://api.github.com/repos/{GITHUB_REPO}/releases/assets/{state['assetId']}"
    headers = {"Authorization": f"Bearer {GITHUB_TOKEN}", "Accept": "application/octet-stream"}
    
    client = httpx.AsyncClient()
    req = client.build_request("GET", asset_url, headers=headers)
    resp = await client.send(req, stream=True)
    
    return StreamingResponse(
        resp.aiter_bytes(),
        status_code=resp.status_code,
        headers={"Content-Type": "application/vnd.android.package-archive"}
    )
```

---

## 7. In-App Client Update Engine (Android / Kotlin)

### Unit-Testable String & Status Contract
To prevent test drift between ViewModel updates and test assertions:
```kotlin
object UpdateStrings {
    const val STATUS_IDLE = "Ready"
    const val STATUS_CHECKING = "Checking for updates..."
    const val STATUS_UP_TO_DATE = "App is up to date"
    const val STATUS_DOWNLOADING = "Downloading update..."
    const val STATUS_VERIFYING = "Verifying package integrity..."
    const val STATUS_READY_TO_INSTALL = "Ready to install"
    const val STATUS_ERROR_NETWORK = "Failed to connect to update server"
    const val STATUS_ERROR_CORRUPT = "Downloaded file failed SHA-256 integrity check"
}

sealed class UpdateStatus {
    object Idle : UpdateStatus()
    object Checking : UpdateStatus()
    object UpToDate : UpdateStatus()
    data class UpdateAvailable(val version: String, val code: Int) : UpdateStatus()
    data class Downloading(val progressPercent: Int) : UpdateStatus()
    data class ReadyToInstall(val apkFile: File) : UpdateStatus()
    data class Error(val message: String) : UpdateStatus()
}
```

### SHA-256 Stream Verification & Installation Flow
```kotlin
fun verifySha256(file: File, expectedHash: String): Boolean {
    if (expectedHash.isBlank()) return false
    val digest = MessageDigest.getInstance("SHA-256")
    file.inputStream().use { input ->
        val buffer = ByteArray(8192)
        var bytesRead: Int
        while (input.read(buffer).also { bytesRead = it } != -1) {
            digest.update(buffer, 0, bytesRead)
        }
    }
    val calculated = digest.digest().joinToString("") { "%02x".format(it) }
    return calculated.equals(expectedHash.trim(), ignoreCase = true)
}

fun launchPackageInstaller(context: Context, apkFile: File) {
    val contentUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        apkFile
    )
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(contentUri, "application/vnd.android.package-archive")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
    }
    context.startActivity(intent)
}
```

---

## 8. Rollback & Fix-Forward Recovery Protocol

### Why Android Rejects Downgrades
The Android `PackageManager` strictly checks `installedVersionCode`. If an incoming APK has `newVersionCode < installedVersionCode`, the OS aborts with:
```text
INSTALL_FAILED_VERSION_DOWNGRADE
```
Users cannot downgrade without uninstalling the app, which completely deletes all local SQLite/Room databases and DataStore settings.

### The Fix-Forward Procedure
1. **Step 1: Point Backend / Manifest to Safe State**:
   Update `version.json` on the server to stop routing devices to the broken version.
2. **Step 2: Revert Defective Code**:
   ```bash
   git checkout main
   git revert <defective_commit_hash>
   ```
3. **Step 3: Increment Version Tag & Code**:
   Increment `versionName` (e.g. `v1.0.6`) and ensure `versionCode` is higher than the defective release.
4. **Step 4: Tag & Trigger Release**:
   ```bash
   git tag v1.0.6
   git push origin v1.0.6
   ```
5. All devices upgrade cleanly to `v1.0.6` without data loss.

---

## 9. Comprehensive Troubleshooting & Diagnostic Matrix

| Symptom / Error | Root Cause | Permanent Solution |
| :--- | :--- | :--- |
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` ("package conflicts with existing package") | Keystore mismatch or Git CRLF binary corruption between builds. | 1. Ensure `.gitattributes` marks `*.keystore binary`.<br>2. Decode keystore base64 without line breaks.<br>3. Verify fingerprint using `keytool -list -v -keystore release.keystore`. |
| App never detects update ("Up to date" false negative) | Remote `versionCode` in metadata is lower than or equal to local `BuildConfig.VERSION_CODE`. | 1. Use `git rev-list --count HEAD` for monotonic codes.<br>2. Ensure backend syncs from `version.json` asset on boot.<br>3. Verify `latestVersionCode > BuildConfig.VERSION_CODE`. |
| HTTP 404 / 401 when downloading APK from Private Repo | GitHub direct download URLs require authorization for private repositories. | Use **Proxy Backend Pattern**: Backend retrieves asset via GitHub API with `Bearer ${GITHUB_TOKEN}` and streams bytes to client. Never put token in client. |
| CI Unit tests fail after UI/Feature changes | Test assertions hardcode divergent UI strings. | Centralize strings in `UpdateStrings` object; reference identical constants in both prod and test suites. |
| Backend serves stale version after container restart | State stored only in memory without persistence or startup discovery. | Implement FastAPI `lifespan` handler that queries GitHub API on container startup. |
| APK installs fail with SHA-256 mismatch | Incomplete CI checksum generation or uppercase/lowercase hash comparison error. | Calculate SHA-256 in CI runner; write to `version.json`; use `equalsIgnoreCase()` in client verification. |
| CI runner fails on LAN webhook notification | Cloud GitHub Actions runner cannot reach private RFC1918 LAN IP (`192.168.x.x`). | Set `continue-on-error: true` on webhook step. Use startup sync or Cloudflare tunnel for automated notification. |
| `INSTALL_FAILED_VERSION_DOWNGRADE` | Attempted rollback to a previous version code. | Follow Fix-Forward protocol: revert code in Git, tag new release with incremented `versionCode`. |
| `FileProvider: IllegalArgumentException: Failed to find configured root` | File saved outside paths defined in `res/xml/file_paths.xml`. | Align download directory with `<external-files-path path="Download/" />` or `<cache-path />`. |
| `apksigner: command not found` in CI | Android SDK build-tools not in PATH. | Invoke via `$ANDROID_HOME/build-tools/<version>/apksigner` inside runner scripts. |
