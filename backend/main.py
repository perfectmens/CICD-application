"""
Remote Update Demo - Custom Backend Service
============================================
Authoritative version control, release metadata, and direct APK delivery backend
governed strictly by api-mapping.yaml contracts.

Clients connect locally via LAN (e.g. 192.168.68.64:8080).
Published APKs and release assets are hosted on GitHub Releases (Internet)
and generated automatically via GitHub Actions CI/CD.
"""

from datetime import datetime, timezone
import os
import random
from typing import Optional, List
from fastapi import FastAPI, Query, HTTPException, Response
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field

app = FastAPI(
    title="Remote Update Demo Backend",
    version="1.0.0",
    description="Backend microservice for APK update checking and GitHub Releases distribution",
)

# Enable CORS for local testing, LAN access, emulator, and web dashboards
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

GITHUB_REPO = "perfectmens/CICD-application"

# In-memory release state (updated via GitHub Actions webhook or admin endpoint)
DEFAULT_STATE = {
    "latestVersionName": "0.0.4",
    "latestVersionCode": 8,   # = git rev-list --count HEAD at v0.0.4 tag
    "minSupportedCode": 1,
    "isMandatory": False,
    # Primary distribution through GitHub Releases over the Internet:
    "downloadUrl": f"https://github.com/{GITHUB_REPO}/releases/download/v0.0.4/app-release-v0.0.4.apk",
    "sha256": "",
    "releaseNotes": "v0.0.4 — Full OTA update flow\n• In-app update detection via Docker backend\n• GitHub Actions notifies backend on every release\n• Download progress with SHA-256 verification\n• Seamless install via PackageInstaller API",
    "publishedAt": datetime.now(timezone.utc).isoformat(),
    "distributionSource": "github_releases"
}

current_release_state = dict(DEFAULT_STATE)


# -----------------------------------------------------------------------------
# Data Models (governed by api-mapping.yaml schemas)
# -----------------------------------------------------------------------------
class HealthResponse(BaseModel):
    status: str = Field("ok", description="Operational health status")
    service: str = Field("remote-update-server", description="Service identifier")
    timestamp: str = Field(..., description="ISO 8601 UTC timestamp")
    version: str = Field("1.0.0", description="Backend service implementation version")
    lan_ip: str = Field("192.168.68.64", description="Host LAN IP address")


class UpdateCheckResponse(BaseModel):
    latestVersionName: str
    latestVersionCode: int
    hasUpdate: bool
    isMandatory: bool
    downloadUrl: str
    sha256: str
    releaseNotes: str
    publishedAt: str


class ReleaseMetadataResponse(BaseModel):
    versionName: str
    versionCode: int
    minSupportedCode: int
    downloadUrl: str
    sha256: str
    releaseNotes: str
    publishedAt: str


class VersionUpdateRequest(BaseModel):
    latestVersionName: str
    latestVersionCode: int
    isMandatory: Optional[bool] = False
    downloadUrl: Optional[str] = None
    sha256: Optional[str] = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
    releaseNotes: Optional[str] = "Simulated update release notes."


class GitHubReleasePayload(BaseModel):
    versionName: str
    versionCode: int
    downloadUrl: Optional[str] = None
    sha256: Optional[str] = ""
    releaseNotes: Optional[str] = "Automated release build from GitHub Actions"
    isMandatory: Optional[bool] = False


class GreetingItem(BaseModel):
    id: int
    text: str
    category: str
    emoji: str


class GreetingsResponse(BaseModel):
    greetings: List[GreetingItem]
    count: int
    timestamp: str


GREETINGS_POOL = [
    GreetingItem(id=1, text="v0.0.4 is live! Full OTA update flow is now operational.", category="Release", emoji="🚀"),
    GreetingItem(id=2, text="Your Docker backend on LAN is delivering live broadcasts.", category="System", emoji="🐳"),
    GreetingItem(id=3, text="GitHub Actions now notifies this backend the moment a release is published.", category="CI/CD", emoji="⚙️"),
    GreetingItem(id=4, text="APK signing certificate is now consistent across all builds — no more conflicts!", category="Security", emoji="🔒"),
    GreetingItem(id=5, text="Have an incredible, productive day building amazing software!", category="Motivation", emoji="☀️"),
    GreetingItem(id=6, text="Jetpack Compose Material 3 brings adaptive native beauty.", category="UI", emoji="🎨"),
    GreetingItem(id=7, text="Over-the-air updates let your users stay on the latest build instantly.", category="Feature", emoji="📲"),
    GreetingItem(id=8, text="Architecture matters: Unidirectional MVVM keeps code clean.", category="Architecture", emoji="🏛️"),
    GreetingItem(id=9, text="Every APK download is SHA-256 verified before installation.", category="Security", emoji="🔑"),
    GreetingItem(id=10, text="Download progress is streamed in real-time to the UI.", category="Feature", emoji="📊"),
    GreetingItem(id=11, text="Greetings from your local Docker microservice container!", category="Docker", emoji="📦"),
    GreetingItem(id=12, text="Zero downtime version deployments are the future of mobile engineering.", category="DevOps", emoji="⚡"),
    GreetingItem(id=13, text="Clear architecture boundaries mean zero regression bugs.", category="Architecture", emoji="🛡️"),
    GreetingItem(id=14, text="PackageInstaller API handles in-place Android app replacement seamlessly.", category="Android", emoji="🤖"),
    GreetingItem(id=15, text="You've mastered full-stack Android CI/CD pipelines!", category="Celebration", emoji="🎉"),
    GreetingItem(id=16, text="Seamless LAN communication paired with global GitHub CDN delivery.", category="Networking", emoji="🌐"),
    GreetingItem(id=17, text="Kotlin Coroutines & Flow make asynchronous networking smooth.", category="Kotlin", emoji="🌊"),
    GreetingItem(id=18, text="The .gitattributes binary marker keeps keystores byte-perfect across platforms.", category="DevOps", emoji="🛠️"),
    GreetingItem(id=19, text="Live version control keeps all distributed clients synchronized.", category="Governance", emoji="📡"),
    GreetingItem(id=20, text="Keep innovating and building extraordinary Android experiences!", category="Inspiration", emoji="✨")
]


# -----------------------------------------------------------------------------
# Endpoints (mapped to api-mapping.yaml)
# -----------------------------------------------------------------------------
@app.get("/api/v1/health", response_model=HealthResponse, tags=["System"])
def get_health():
    """
    Contract ID: system.health.check
    Validates backend service operational status and LAN connectivity.
    """
    return HealthResponse(
        status="ok",
        service="remote-update-server",
        timestamp=datetime.now(timezone.utc).isoformat(),
        version="1.0.0",
        lan_ip="192.168.68.64"
    )


@app.get("/api/v1/messages/random", response_model=GreetingsResponse, tags=["Messaging"])
def get_random_greetings(
    count: int = Query(10, ge=1, le=20, description="Number of random greetings to retrieve")
):
    """
    Contract ID: messages.greetings.random
    Returns randomized greetings from the Docker backend service.
    """
    sample_size = min(count, len(GREETINGS_POOL))
    selected = random.sample(GREETINGS_POOL, sample_size)
    return GreetingsResponse(
        greetings=selected,
        count=len(selected),
        timestamp=datetime.now(timezone.utc).isoformat()
    )


@app.get("/api/v1/version/check", response_model=UpdateCheckResponse, tags=["Updates"])
def check_version(
    current_version: str = Query(..., description="Currently installed semantic version"),
    version_code: int = Query(..., description="Currently installed integer version code"),
):
    """
    Contract ID: version.update.check
    Compares the client's current version against the authoritative release manifest.
    Download URL directs client to the GitHub Release APK over the Internet.
    """
    latest_code = current_release_state["latestVersionCode"]
    latest_name = current_release_state["latestVersionName"]

    # Android rule: update is available if server code is strictly greater than installed code
    has_update = latest_code > version_code

    return UpdateCheckResponse(
        latestVersionName=latest_name,
        latestVersionCode=latest_code,
        hasUpdate=has_update,
        isMandatory=current_release_state["isMandatory"] and has_update,
        downloadUrl=current_release_state["downloadUrl"],
        sha256=current_release_state["sha256"],
        releaseNotes=current_release_state["releaseNotes"],
        publishedAt=current_release_state["publishedAt"],
    )


@app.get("/api/v1/updates/latest", response_model=ReleaseMetadataResponse, tags=["Updates"])
def get_latest_metadata():
    """
    Contract ID: version.metadata.latest
    Returns the latest published release metadata and changelog.
    """
    return ReleaseMetadataResponse(
        versionName=current_release_state["latestVersionName"],
        versionCode=current_release_state["latestVersionCode"],
        minSupportedCode=current_release_state["minSupportedCode"],
        downloadUrl=current_release_state["downloadUrl"],
        sha256=current_release_state["sha256"],
        releaseNotes=current_release_state["releaseNotes"],
        publishedAt=current_release_state["publishedAt"],
    )


@app.post("/api/v1/github/release-published", tags=["GitHub Actions"])
def on_github_release_published(req: GitHubReleasePayload):
    """
    Contract ID: version.github.publish
    Webhook endpoint invoked by GitHub Actions release workflow when an APK is published.
    Updates the local manifest so all LAN clients discover the newly published GitHub Release.
    """
    current_release_state["latestVersionName"] = req.versionName
    current_release_state["latestVersionCode"] = req.versionCode
    current_release_state["downloadUrl"] = req.downloadUrl or f"https://github.com/{GITHUB_REPO}/releases/download/v{req.versionName}/app-release-v{req.versionName}.apk"
    if req.sha256:
        current_release_state["sha256"] = req.sha256
    if req.releaseNotes:
        current_release_state["releaseNotes"] = req.releaseNotes
    if req.isMandatory is not None:
        current_release_state["isMandatory"] = req.isMandatory
    current_release_state["publishedAt"] = datetime.now(timezone.utc).isoformat()
    current_release_state["distributionSource"] = "github_actions"

    return {
        "success": True,
        "message": f"GitHub Actions published v{req.versionName} (code: {req.versionCode})",
        "downloadUrl": current_release_state["downloadUrl"],
        "state": current_release_state
    }


@app.post("/api/v1/admin/version", tags=["Admin"])
def update_target_version(req: VersionUpdateRequest):
    """
    Contract ID: version.admin.update
    Administrative simulator endpoint to dynamically bump target version
    (e.g., test upgrading from 0.0.1 -> 0.0.2 in learning exercises).
    """
    current_release_state["latestVersionName"] = req.latestVersionName
    current_release_state["latestVersionCode"] = req.latestVersionCode
    if req.isMandatory is not None:
        current_release_state["isMandatory"] = req.isMandatory
    if req.downloadUrl:
        current_release_state["downloadUrl"] = req.downloadUrl
    else:
        current_release_state["downloadUrl"] = f"https://github.com/{GITHUB_REPO}/releases/download/v{req.latestVersionName}/app-release-v{req.latestVersionName}.apk"
    if req.sha256:
        current_release_state["sha256"] = req.sha256
    if req.releaseNotes:
        current_release_state["releaseNotes"] = req.releaseNotes
    current_release_state["publishedAt"] = datetime.now(timezone.utc).isoformat()

    return {
        "success": True,
        "message": f"Updated active release to {req.latestVersionName} (code: {req.latestVersionCode})",
        "state": current_release_state,
    }


@app.post("/api/v1/admin/reset", tags=["Admin"])
def reset_to_default_version():
    """
    Resets the backend release state back to initial v0.0.1 (code: 1).
    """
    global current_release_state
    current_release_state = dict(DEFAULT_STATE)
    return {"success": True, "message": "Reset to v0.0.4 (code 8)", "state": current_release_state}


@app.get("/api/v1/updates/download/latest.apk", tags=["Updates"])
def download_apk():
    """
    Contract ID: version.download.apk
    Local fallback APK download endpoint.
    """
    apk_path = os.path.join(os.path.dirname(__file__), "storage", "app-release.apk")
    if os.path.exists(apk_path):
        with open(apk_path, "rb") as f:
            content = f.read()
    else:
        content = b"PK\x03\x04DEMO_STANDIN_APK_BINARY_PAYLOAD"

    return Response(
        content=content,
        media_type="application/vnd.android.package-archive",
        headers={"Content-Disposition": 'attachment; filename="RemoteUpdateDemo-latest.apk"'},
    )


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8080, reload=True)
