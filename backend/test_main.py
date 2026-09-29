"""
Integration and end-to-end tests for the Remote Update Backend service.
Uses standard library urllib to verify live endpoints without external dependencies.
"""

import json
import urllib.request
import urllib.error
import unittest

BASE_URL = "http://127.0.0.1:8080"


class TestRemoteUpdateBackend(unittest.TestCase):
    def setUp(self):
        # Reset to default state
        req = urllib.request.Request(
            f"{BASE_URL}/api/v1/admin/reset",
            data=b"{}",
            headers={"Content-Type": "application/json"},
            method="POST"
        )
        with urllib.request.urlopen(req) as resp:
            self.assertEqual(resp.status, 200)

    def test_health_check(self):
        with urllib.request.urlopen(f"{BASE_URL}/api/v1/health") as resp:
            self.assertEqual(resp.status, 200)
            data = json.loads(resp.read().decode())
            self.assertEqual(data["status"], "ok")
            self.assertEqual(data["service"], "remote-update-server")
            self.assertIn("timestamp", data)
            self.assertEqual(data["version"], "1.0.0")

    def test_version_check_same_version(self):
        url = f"{BASE_URL}/api/v1/version/check?current_version=0.0.1&version_code=1"
        with urllib.request.urlopen(url) as resp:
            self.assertEqual(resp.status, 200)
            data = json.loads(resp.read().decode())
            self.assertEqual(data["latestVersionName"], "0.0.1")
            self.assertEqual(data["latestVersionCode"], 1)
            self.assertFalse(data["hasUpdate"])
            self.assertFalse(data["isMandatory"])

    def test_version_check_older_version(self):
        url = f"{BASE_URL}/api/v1/version/check?current_version=0.0.0&version_code=0"
        with urllib.request.urlopen(url) as resp:
            self.assertEqual(resp.status, 200)
            data = json.loads(resp.read().decode())
            self.assertTrue(data["hasUpdate"])
            self.assertEqual(data["latestVersionName"], "0.0.1")

    def test_admin_bump_version_and_detect_update(self):
        # Simulate publishing version 0.0.2 with code 2
        payload = json.dumps({
            "latestVersionName": "0.0.2",
            "latestVersionCode": 2,
            "releaseNotes": "Automated update test"
        }).encode()
        req = urllib.request.Request(
            f"{BASE_URL}/api/v1/admin/version",
            data=payload,
            headers={"Content-Type": "application/json"},
            method="POST"
        )
        with urllib.request.urlopen(req) as resp:
            self.assertEqual(resp.status, 200)

        # Now client with 0.0.1 (code 1) should detect the update
        url = f"{BASE_URL}/api/v1/version/check?current_version=0.0.1&version_code=1"
        with urllib.request.urlopen(url) as resp:
            self.assertEqual(resp.status, 200)
            data = json.loads(resp.read().decode())
            self.assertTrue(data["hasUpdate"])
            self.assertEqual(data["latestVersionName"], "0.0.2")
            self.assertEqual(data["latestVersionCode"], 2)

    def test_github_release_publish(self):
        # Simulate GitHub Actions pushing a newly published release
        payload = json.dumps({
            "versionName": "0.0.3",
            "versionCode": 3,
            "downloadUrl": "https://github.com/perfectmens/CICD-application/releases/download/v0.0.3/app-release-v0.0.3.apk",
            "sha256": "gh_release_sha_256",
            "releaseNotes": "Automated build from commit xyz"
        }).encode()
        req = urllib.request.Request(
            f"{BASE_URL}/api/v1/github/release-published",
            data=payload,
            headers={"Content-Type": "application/json"},
            method="POST"
        )
        with urllib.request.urlopen(req) as resp:
            self.assertEqual(resp.status, 200)
            data = json.loads(resp.read().decode())
            self.assertTrue(data["success"])
            self.assertIn("github.com/perfectmens/CICD-application", data["downloadUrl"])

        # Client queries via LAN and receives the GitHub release Internet URL
        url = f"{BASE_URL}/api/v1/version/check?current_version=0.0.1&version_code=1"
        with urllib.request.urlopen(url) as resp:
            self.assertEqual(resp.status, 200)
            data = json.loads(resp.read().decode())
            self.assertTrue(data["hasUpdate"])
            self.assertEqual(data["latestVersionName"], "0.0.3")
            self.assertTrue(data["downloadUrl"].startswith("https://github.com/"))

    def test_download_apk_endpoint(self):
        url = f"{BASE_URL}/api/v1/updates/download/latest.apk"
        with urllib.request.urlopen(url) as resp:
            self.assertEqual(resp.status, 200)
            content_type = resp.headers.get("Content-Type")
            self.assertEqual(content_type, "application/vnd.android.package-archive")
            content = resp.read()
            self.assertTrue(len(content) > 0)

    def test_random_greetings_endpoint(self):
        url = f"{BASE_URL}/api/v1/messages/random?count=10"
        with urllib.request.urlopen(url) as resp:
            self.assertEqual(resp.status, 200)
            data = json.loads(resp.read().decode())
            self.assertEqual(data["count"], 10)
            self.assertEqual(len(data["greetings"]), 10)
            self.assertIn("timestamp", data)
            first = data["greetings"][0]
            self.assertIn("id", first)
            self.assertIn("text", first)
            self.assertIn("category", first)
            self.assertIn("emoji", first)


if __name__ == "__main__":
    unittest.main()

