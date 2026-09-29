---
name: android-ci-cd
family: software-delivery
description: Comprehensive CI/CD automation, pipeline engineering, and release governance skill for Android applications. Use this skill whenever designing, building, troubleshooting, or optimizing Android Continuous Integration (Gradle builds, unit tests, Android Lint, static analysis, dependency scanning, build caching) and Continuous Delivery/Deployment (GitHub Actions workflows, keystore signing, GitHub Releases, Google Play publishing, Firebase App Distribution, direct APK delivery, in-app updates, Room database migrations, and rollback/fix-forward strategies).
---

# Android CI/CD Pipeline & Release Engineering Skill

An end-to-end engineering standard and operational guide for automating Continuous Integration (CI) and Continuous Delivery/Deployment (CD) for Android applications.

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
│  Publish ◄── SHA-256 & Mapping ◄── apksigner (V2/V3/V4) ◄──────────────┘
│                                              │
│                 ┌────────────────────────────┼─────────────────────────┐
│                 ▼                            ▼                         ▼
│         Track A: Direct APK          Track B: Google Play      Track C: Firebase
│         GitHub Releases +            Internal / Alpha /        App Distribution
│         version.json Metadata        Production Track          (QA Testers)
└────────────────────────────────────────────────────────────────────────┘
```

### Core Tenets of Android CI/CD
1. **Never sign production binaries in CI on PRs**: Pull request builds only compile debug variants and run tests. Release signing keys are strictly reserved for protected release jobs.
2. **Build Once, Promote Everywhere**: An APK or AAB compiled from a specific Git tag must be the exact binary promoted through testing to production. Never rebuild from source for different release stages.
3. **Every Release is Traceable**: Every published artifact must map directly to an immutable Git commit, a monotonic integer `versionCode`, a SemVer `versionName`, and a SHA-256 cryptographic checksum.
4. **Android Rejects Downgrades**: Android OS blocks installing older APKs over newer ones (`INSTALL_FAILED_VERSION_DOWNGRADE`). Rollbacks are executed as **fix-forward releases** with an incremented `versionCode`.
5. **Data Preservation Over Code**: App updates must never destroy local SQLite/Room databases, DataStore preferences, or user files.

---

## 2. Repository & Gradle Configuration Foundation

### Project Directory Structure
```text
your-android-repo/
├── .github/
│   └── workflows/
│       ├── ci.yml                 # PR & branch validation workflow
│       └── release.yml            # Tagged production release workflow
├── app/
│   ├── src/
│   │   ├── main/                  # Application source code & AndroidManifest.xml
│   │   ├── test/                  # JVM Unit Tests
│   │   └── androidTest/           # Instrumented / UI tests
│   ├── build.gradle.kts           # App-level build definition
│   └── proguard-rules.pro         # R8 / ProGuard obfuscation rules
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── build.gradle.kts               # Root build definition
├── settings.gradle.kts            # Plugin & module management
├── gradle.properties              # JVM memory & build flags
└── README.md
```

### `gradle.properties` Performance Optimization
Ensure reproducible, parallel, and cached builds in CI runners:
```properties
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=1g -XX:+UseG1GC
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configuration-cache=true
android.useAndroidX=true
```

### App-level `app/build.gradle.kts` Blueprint
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.myapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myapp"
        minSdk = 26
        targetSdk = 34

        // Integer monotonically incremented for every release
        versionCode = project.findProperty("versionCode")?.toString()?.toIntOrNull() ?: 1
        // Human-facing semantic version string
        versionName = project.findProperty("versionName")?.toString() ?: "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            // Populated from environment variables during CI execution
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
            // Use release signing config only if keystore is present
            if (System.getenv("KEYSTORE_PATH") != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}
```

---

## 3. Continuous Integration (CI) Workflow

File: `.github/workflows/ci.yml`
Triggers on Pull Requests and pushes to `main`. It validates compilation, code quality, and test health without exposing production signing keys.

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

      - name: Upload Lint Report
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: lint-report
          path: app/build/reports/lint-results-debug.html
          retention-days: 7

      - name: Upload Unit Test Results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-results
          path: app/build/reports/tests/testDebugUnitTest/
          retention-days: 7
```

---

## 4. Continuous Delivery (CD) Workflow

File: `.github/workflows/release.yml`
Triggers when a release tag (e.g. `v1.2.0`) is pushed or manually triggered. Decodes signing secrets, builds release APK & AAB, computes cryptographic hashes, and creates a tagged GitHub Release.

```yaml
name: Android CD Release

on:
  push:
    tags:
      - 'v*'
  workflow_dispatch:
    inputs:
      version_name:
        description: 'Semantic Version Name (e.g. 1.2.0)'
        required: true
        default: '1.2.0'
      version_code:
        description: 'Monotonic Version Code (e.g. 12)'
        required: true
        default: '12'

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
          if [ "${{ github.event_name }}" = "workflow_dispatch" ]; then
            V_NAME="${{ github.event.inputs.version_name }}"
            V_CODE="${{ github.event.inputs.version_code }}"
          else
            # Extract version from Git tag v1.2.0 -> 1.2.0
            RAW_TAG="${GITHUB_REF#refs/tags/v}"
            V_NAME="$RAW_TAG"
            # Extract numeric version code (e.g. commit count or derived integer)
            V_CODE=$(git rev-list --count HEAD)
          fi
          echo "Building versionName: $V_NAME, versionCode: $V_CODE"
          echo "version_name=$V_NAME" >> $GITHUB_OUTPUT
          echo "version_code=$V_CODE" >> $GITHUB_OUTPUT

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

      - name: Securely Wipe Keystore File
        if: always()
        run: rm -f /tmp/release.keystore

      - name: Package Artifacts & Checksums
        id: artifacts
        run: |
          VERSION="${{ steps.versioning.outputs.version_name }}"
          APK_SRC=$(find app/build/outputs/apk/release/ -name "*.apk" | head -n 1)
          AAB_SRC=$(find app/build/outputs/bundle/release/ -name "*.aab" | head -n 1)
          MAPPING_SRC="app/build/outputs/mapping/release/mapping.txt"

          APK_DEST="app-release-v${VERSION}.apk"
          AAB_DEST="app-release-v${VERSION}.aab"

          cp "$APK_SRC" "$APK_DEST"
          cp "$AAB_SRC" "$AAB_DEST"

          APK_SHA256=$(sha256sum "$APK_DEST" | awk '{print $1}')
          AAB_SHA256=$(sha256sum "$AAB_DEST" | awk '{print $1}')

          echo "APK SHA256: $APK_SHA256"
          echo "AAB SHA256: $AAB_SHA256"

          echo "apk_path=$APK_DEST" >> $GITHUB_OUTPUT
          echo "aab_path=$AAB_DEST" >> $GITHUB_OUTPUT
          echo "apk_sha256=$APK_SHA256" >> $GITHUB_OUTPUT
          echo "mapping_path=$MAPPING_SRC" >> $GITHUB_OUTPUT

      - name: Verify APK Signature (apksigner)
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
          body: |
            ## Android Release v${{ steps.versioning.outputs.version_name }} (Code: ${{ steps.versioning.outputs.version_code }})

            ### Checksums & Verification
            - **APK (`${{ steps.artifacts.outputs.apk_path }}`):**
              `SHA-256: ${{ steps.artifacts.outputs.apk_sha256 }}`
            - **Commit:** `${{ github.sha }}`

            ### Changelog
            See git commit history for full details.
          draft: false
          prerelease: false
```

---

## 5. Multi-Track Distribution Strategies

### Track A: Direct APK Distribution (Private & Enterprise Apps)
For applications distributed directly outside Google Play (~100 users, enterprise, or internal tools):
1. **GitHub Releases Hosting**: The APK is published as a GitHub Release asset with an immutable download URL:
   `https://github.com/<owner>/<repo>/releases/download/v<version>/app-release-v<version>.apk`
2. **Version Manifest Endpoint (`version.json`)**: A static JSON metadata file is updated during release:
   ```json
   {
     "versionCode": 12,
     "versionName": "1.2.0",
     "downloadUrl": "https://github.com/myorg/myapp/releases/download/v1.2.0/app-release-v1.2.0.apk",
     "sha256": "4b6f12a95c3b1e7...48d1",
     "isMandatory": false,
     "releaseNotes": "• Real-time sync engine\n• Database performance enhancements\n• Bug fixes"
   }
   ```
3. **In-App Client Update Engine (MVVM)**:
   - The app checks `version.json` over HTTPS.
   - Compares integer codes: `if (remote.versionCode > BuildConfig.VERSION_CODE)`.
   - Downloads the APK with progress streaming and validates the SHA-256 hash.
   - Hands off the APK to Android's `PackageInstaller` via `FileProvider` (`Intent.ACTION_VIEW` with `FLAG_GRANT_READ_URI_PERMISSION`).

### Track B: Google Play Store Track Publishing
To automatically distribute `.aab` bundles to Google Play tracks (Internal Testing, Alpha, Beta, Production), add the Google Play step to `release.yml`:

```yaml
      - name: Upload to Google Play Track
        uses: r0adkll/upload-google-play@v1
        with:
          serviceAccountJsonPlainText: ${{ secrets.PLAY_CONSOLE_SERVICE_ACCOUNT_JSON }}
          packageName: com.example.myapp
          releaseFiles: ${{ steps.artifacts.outputs.aab_path }}
          track: internal # Options: internal, alpha, beta, production
          mappingFile: ${{ steps.artifacts.outputs.mapping_path }}
          status: completed
```

### Track C: Firebase App Distribution (Internal QA)
To push debug or staging APKs directly to test groups on every merge to `main`:

```yaml
      - name: Upload to Firebase App Distribution
        uses: wzieba/Firebase-Distribution-Github-Action@v1
        with:
          appId: ${{ secrets.FIREBASE_APP_ID }}
          serviceCredentialsFileContent: ${{ secrets.FIREBASE_SERVICE_ACCOUNT_JSON }}
          groups: internal-testers
          file: ${{ steps.artifacts.outputs.apk_path }}
          releaseNotes: "Automated CI/CD build from commit ${{ github.sha }}"
```

---

## 6. Local Data Preservation & Room Migrations

Updating an application must never result in data loss. Android preserves the app's internal sandbox (`/data/data/<package_id>/`) across updates as long as the package ID and signing key remain identical.

### Room SQLite Migration Protocol
1. **Never use `fallbackToDestructiveMigration()` in production**: Doing so wipes all user SQLite tables when the schema version changes.
2. **Explicit Migration Objects**: Always write and register explicit migrations:
   ```kotlin
   val MIGRATION_1_2 = object : Migration(1, 2) {
       override fun migrate(db: SupportSQLiteDatabase) {
           db.execSQL("ALTER TABLE UserEntity ADD COLUMN email TEXT DEFAULT '' NOT NULL")
       }
   }
   ```
3. **Automated Migration Testing**: Use `MigrationTestHelper` in `app/src/test` to verify migrations against real SQLite databases before cutting a release:
   ```kotlin
   @RunWith(AndroidJUnit4::class)
   class DatabaseMigrationTest {
       @get:Rule
       val helper = MigrationTestHelper(
           InstrumentationRegistry.getInstrumentation(),
           AppDatabase::class.java
       )

       @Test
       fun migrate1To2_containsCorrectData() {
           var db = helper.createDatabase("test-db", 1).apply {
               execSQL("INSERT INTO UserEntity (id, name) VALUES (1, 'Alice')")
               close()
           }
           db = helper.runMigrationsAndValidate("test-db", 2, true, MIGRATION_1_2)
           // Assert Alice still exists and has email column populated
       }
   }
   ```

---

## 7. Security & Secrets Management Guardrails

### Secrets Inventory in GitHub Repository Settings
| Secret Name | Purpose | Scope |
| :--- | :--- | :--- |
| `KEYSTORE_BASE64` | Base64 string of the production `.jks` file | Release CD only |
| `KEYSTORE_PASSWORD` | Password unlocking the keystore container | Release CD only |
| `KEY_ALIAS` | Alias identifying the signing private key | Release CD only |
| `KEY_PASSWORD` | Password unlocking the private key | Release CD only |
| `PLAY_CONSOLE_SERVICE_ACCOUNT_JSON` | Google Play Developer API service credentials | Store deployment |

### Non-Negotiable Security Rules
* **Never commit keystores or credentials into Git**: Add `*.jks`, `*.keystore`, and `keystore_base64.txt` to `.gitignore`.
* **Zero Secrets in the APK**: Never embed GitHub Personal Access Tokens or cloud admin credentials in client source code or assets.
* **Keep an Offline Keystore Backup**: If the production keystore is permanently lost, no future updates can ever be installed on existing users' devices. Store an encrypted copy outside of GitHub.
* **Least-Privilege GitHub Permissions**: Set default repository permissions to read-only, granting `contents: write` only to the release job for uploading assets.

---

## 8. Rollback & Fix-Forward Recovery Protocol

### Why Android Rejects Downgrades
The Android `PackageManager` strictly checks `installedVersionCode`. If an incoming APK has `newVersionCode < installedVersionCode`, the OS aborts with:
```text
INSTALL_FAILED_VERSION_DOWNGRADE
```
Users cannot downgrade without uninstalling the app, which completely deletes all local databases and user settings.

### The Standard Fix-Forward Procedure
When a defective release (e.g. `v1.2.0` with `versionCode = 12`) reaches devices:

1. **Step 1: Halt Ingestion / Update Metadata**:
   Immediately update `version.json` or pause the Google Play track to prevent additional users from receiving `versionCode = 12`.
2. **Step 2: Revert Defective Code in Git**:
   ```bash
   git checkout main
   git revert <defective_commit_hash>
   ```
3. **Step 3: Increment `versionCode` (Fix-Forward)**:
   Set `versionCode = 13` and `versionName = 1.2.1`.
4. **Step 4: Tag & Trigger Release**:
   ```bash
   git tag v1.2.1
   git push origin v1.2.1
   ```
5. **Step 5: Publish v1.2.1**:
   GitHub Actions builds, signs, and publishes `versionCode = 13`. Both users on `v1.1.0` (code 11) and affected users on `v1.2.0` (code 12) seamlessly update to `13` with zero data loss!

---

## 9. Release Verification Checklist & Definition of Done

A release is marked **Production-Ready** only when all conditions are fulfilled:

```text
[ ] CI Quality Gates
    [ ] Lint passes with zero errors (lintDebug)
    [ ] All JVM unit tests pass (testDebugUnitTest)
    [ ] Gradle dependency cache functions properly

[ ] Release Build & Signature
    [ ] Keystore decodes and signs release APK & AAB
    [ ] apksigner verify confirms V2/V3/V4 signatures are valid
    [ ] ProGuard/R8 mapping.txt is archived as an artifact
    [ ] SHA-256 checksums are calculated and logged

[ ] Upgrade & Data Preservation Verification
    [ ] APK upgrades cleanly over the previous release on a test device
    [ ] Existing Room/SQLite records survive the upgrade intact
    [ ] In-app update check recognizes the new versionCode

[ ] Recovery & Distribution
    [ ] Artifacts uploaded to designated track (GitHub Release / Play Console / Firebase)
    [ ] Fix-forward rollback path is documented and operational
```

---

## 10. Troubleshooting & Diagnostic Runbook

| Symptom / Error | Root Cause | Solution |
| :--- | :--- | :--- |
| `OutOfMemoryError: Java heap space` in CI | Gradle runner exceeded memory limit | Add `org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=1g` to `gradle.properties`. |
| `Lint found errors in the project; aborting build` | Unhandled lint warnings or errors | Run `./gradlew lintDebug` locally. Fix issues or configure `lint { abortOnError = false }` only for non-critical rules. |
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | Signing key or package ID does not match installed app | Ensure the CI keystore (`KEY_ALIAS`, `.jks`) exactly matches the key used to sign the currently installed release. |
| `apksigner: command not found` | Android build-tools not in PATH | Call via `$ANDROID_HOME/build-tools/<version>/apksigner` inside runner scripts. |
| `FileProvider: IllegalArgumentException: Failed to find configured root` | `file_paths.xml` does not match the download directory | Match the download location with `<external-files-path path="Download/" />` or `<cache-path />`. |
| `Keystore was tampered with, or password was incorrect` | Corrupted base64 secret or incorrect password | Re-encode keystore with `base64 -w 0 release.keystore` and verify secrets in GitHub Settings. |
