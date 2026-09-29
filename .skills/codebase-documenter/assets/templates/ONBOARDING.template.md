# Developer Onboarding & Local Setup Guide

> **Target**: New engineers & AI development agents  
> **Goal**: First successful local run in **under 5 minutes**  
> **Freshness Rule**: Change-driven (must be verified when build scripts or environment configurations change).  
> **Last Audited**: [YYYY-MM-DD]

---

## 1. System Prerequisites

Verify and install the required tooling:

| Tool | Required Version | Verification Command | Installation Link |
| :--- | :--- | :--- | :--- |
| **Node.js** | `>= 20.0.0` | `node -v` | [nodejs.org](https://nodejs.org) |
| **Flutter** | `>= 3.24.0` | `flutter --version` | [docs.flutter.dev](https://docs.flutter.dev) |
| **Docker** | `>= 24.0.0` | `docker --version` | [docker.com](https://docker.com) |

---

## 2. 5-Minute Quickstart

### Step 1: Clone & Navigate
```bash
git clone <REPOSITORY_URL>
cd <PROJECT_DIR>
```

### Step 2: Configure Environment
Copy the validated environment template:
```bash
# Windows PowerShell
Copy-Item .env.example .env

# macOS / Linux bash
cp .env.example .env
```

> **Security Rule**: Never commit secrets. Inspect `.env` and fill in local developer credentials:
> - `API_URL=<LOCAL_OR_STAGING_URL>`
> - `DATABASE_URL=postgres://<DB_USER>:<DB_PASSWORD>@<DB_HOST>:<DB_PORT>/<DB_NAME>`

### Step 3: Install Dependencies
```bash
# Backend / Frontend
npm install

# Mobile (Flutter)
flutter pub get
```

### Step 4: Run Database Migrations
```bash
npm run db:migrate
```

### Step 5: Start the Development Server
```bash
npm run dev
# or for Flutter:
flutter run -d chrome
```

---

## 3. Verified Development Commands

All commands below have been tested and verified against project manifests:

### Running Tests
```bash
# Run unit tests
npm test

# Run tests with coverage
npm run test:cov
```

### Linting & Formatting
```bash
npm run lint
```
