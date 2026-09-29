#!/usr/bin/env python3
"""
validate-mvvm.py
================
Static analysis and architecture linter for mobile codebases (Flutter/Dart, React Native, Kotlin, Swift).
Enforces MVVM layer separation:
  - View cannot call HTTP clients, construct API URLs, or query databases directly.
  - ViewModel cannot import raw HTTP clients (dio, http, axios) or build raw API URLs.
  - Repositories cannot import UI rendering components.
  - API Clients cannot contain UI or ViewModel references.

Exit codes:
  0: Architecture valid (zero violations)
  1: Architecture violations detected
"""

import sys
import os
import re
import argparse
from pathlib import Path
from typing import List, Dict, Tuple

CODE_EXTENSIONS = {".dart", ".ts", ".tsx", ".js", ".kt", ".swift"}

HTTP_PACKAGES = {
    "package:http/http.dart",
    "package:dio/dio.dart",
    "dio",
    "axios",
    "node-fetch",
    "retrofit",
    "okhttp3",
}

DATABASE_PACKAGES = {
    "package:sqflite/sqflite.dart",
    "package:hive/hive.dart",
    "package:shared_preferences/shared_preferences.dart",
    "package:isar/isar.dart",
    "realm",
    "sqlite3",
    "androidx.room",
    "CoreData",
}

UI_PACKAGES = {
    "package:flutter/material.dart",
    "package:flutter/cupertino.dart",
    "package:flutter/widgets.dart",
    "react-native",
    "androidx.compose",
    "SwiftUI",
}

class MvvmValidator:
    def __init__(self, target_dir: str, strict: bool = False):
        self.target_dir = Path(target_dir).resolve()
        self.strict = strict
        self.violations: List[Dict[str, str]] = []
        self.warnings: List[Dict[str, str]] = []
        self.files_scanned = 0

    def log_violation(self, file_path: Path, line_num: int, message: str, rule: str):
        rel_path = file_path.relative_to(self.target_dir) if file_path.is_relative_to(self.target_dir) else file_path
        self.violations.append({
            "file": str(rel_path),
            "line": line_num,
            "message": message,
            "rule": rule
        })

    def log_warning(self, file_path: Path, line_num: int, message: str, rule: str):
        rel_path = file_path.relative_to(self.target_dir) if file_path.is_relative_to(self.target_dir) else file_path
        self.warnings.append({
            "file": str(rel_path),
            "line": line_num,
            "message": message,
            "rule": rule
        })

    def run(self) -> bool:
        if not self.target_dir.exists():
            print(f"[FATAL] Directory not found: {self.target_dir}", file=sys.stderr)
            return False

        for root, _, files in os.walk(self.target_dir):
            # Skip build/cache/git directories
            parts = Path(root).parts
            if any(p in {".git", ".dart_tool", "build", "node_modules", ".gradle", "Pods"} for p in parts):
                continue

            for file in files:
                file_path = Path(root) / file
                if file_path.suffix in CODE_EXTENSIONS:
                    self._check_file(file_path)

        return len(self.violations) == 0

    def _determine_layer(self, file_path: Path) -> str:
        name_lower = file_path.name.lower()
        path_str = str(file_path).replace("\\", "/").lower()

        if any(x in path_str for x in ["/views/", "/screens/", "/presentation/widgets/", "/pages/"]) or \
           any(name_lower.endswith(x) for x in ["_view.dart", "_screen.dart", "_page.dart", "screen.tsx", "view.swift"]):
            return "view"

        if any(x in path_str for x in ["/viewmodels/", "/view_models/", "/cubits/", "/blocs/"]) or \
           any(name_lower.endswith(x) for x in ["_viewmodel.dart", "_vm.dart", "_cubit.dart", "_bloc.dart", "viewmodel.kt", "viewmodel.swift"]):
            return "viewmodel"

        if "/repositories/" in path_str or any(name_lower.endswith(x) for x in ["_repository.dart", "_repo.dart", "repository.kt"]):
            return "repository"

        if any(x in path_str for x in ["/services/", "/api/", "/clients/", "/datasource/"]) or \
           any(name_lower.endswith(x) for x in ["_api.dart", "_client.dart", "_service.dart", "api.kt"]):
            return "api_client"

        return "other"

    def _check_file(self, file_path: Path):
        self.files_scanned += 1
        layer = self._determine_layer(file_path)

        try:
            with open(file_path, "r", encoding="utf-8", errors="ignore") as f:
                lines = f.readlines()
        except Exception:
            return

        for i, line in enumerate(lines, start=1):
            line_clean = line.strip()
            # Ignore single line comments
            if line_clean.startswith("//") or line_clean.startswith("#"):
                continue

            # -------------------------------------------------------------
            # 1. VIEW LAYER CHECKS
            # -------------------------------------------------------------
            if layer == "view":
                # Check HTTP imports
                for pkg in HTTP_PACKAGES:
                    if pkg in line_clean:
                        self.log_violation(
                            file_path, i,
                            f"View layer importing HTTP package: '{pkg}'",
                            "VIOLATION: View -> HTTP Client"
                        )

                # Check Database imports
                for db in DATABASE_PACKAGES:
                    if db in line_clean:
                        self.log_violation(
                            file_path, i,
                            f"View layer importing Database package: '{db}'",
                            "VIOLATION: View -> Direct Database Access"
                        )

                # Check raw HTTP methods
                if re.search(r"\b(http\.get|http\.post|dio\.get|dio\.post|axios\.get|fetch\()\b", line_clean):
                    self.log_violation(
                        file_path, i,
                        "Direct HTTP call inside View layer.",
                        "VIOLATION: View -> Direct HTTP Call"
                    )

                # Check hardcoded API paths
                if re.search(r"['\"]/api/v[0-9]+/", line_clean):
                    self.log_violation(
                        file_path, i,
                        "Hardcoded API endpoint in View. Endpoints belong in API Client.",
                        "VIOLATION: View -> Hardcoded API URL"
                    )

            # -------------------------------------------------------------
            # 2. VIEWMODEL LAYER CHECKS
            # -------------------------------------------------------------
            elif layer == "viewmodel":
                # Check raw HTTP package imports
                for pkg in HTTP_PACKAGES:
                    if pkg in line_clean:
                        self.log_violation(
                            file_path, i,
                            f"ViewModel importing HTTP package: '{pkg}'. ViewModels must call Repositories, not HTTP clients directly.",
                            "VIOLATION: ViewModel -> HTTP Client"
                        )

                # Check raw HTTP calls
                if re.search(r"\b(http\.get|http\.post|dio\.get|dio\.post|axios\.get|fetch\()\b", line_clean):
                    self.log_violation(
                        file_path, i,
                        "Direct HTTP call inside ViewModel. Delegate to a Repository.",
                        "VIOLATION: ViewModel -> Direct HTTP Call"
                    )

                # Check hardcoded API paths
                if re.search(r"['\"]/api/v[0-9]+/", line_clean):
                    self.log_violation(
                        file_path, i,
                        "Hardcoded API path in ViewModel. Route details belong exclusively in API Client.",
                        "VIOLATION: ViewModel -> Hardcoded API URL"
                    )

                # Check UI imports in ViewModel (warn on Flutter Material/Cupertino)
                if any(x in line_clean for x in ["package:flutter/material.dart", "package:flutter/cupertino.dart"]):
                    self.log_warning(
                        file_path, i,
                        "ViewModel imports UI rendering library (material.dart / cupertino.dart). Prefer importing only 'package:flutter/foundation.dart'.",
                        "WARNING: ViewModel -> UI Library Import"
                    )

            # -------------------------------------------------------------
            # 3. REPOSITORY LAYER CHECKS
            # -------------------------------------------------------------
            elif layer == "repository":
                for ui_pkg in UI_PACKAGES:
                    if ui_pkg in line_clean:
                        self.log_violation(
                            file_path, i,
                            f"Repository importing UI rendering package: '{ui_pkg}'. Repositories must remain UI-agnostic.",
                            "VIOLATION: Repository -> UI Library"
                        )

            # -------------------------------------------------------------
            # 4. API CLIENT LAYER CHECKS
            # -------------------------------------------------------------
            elif layer == "api_client":
                for ui_pkg in UI_PACKAGES:
                    if ui_pkg in line_clean:
                        self.log_violation(
                            file_path, i,
                            f"API Client importing UI rendering package: '{ui_pkg}'. API Clients must remain UI-agnostic.",
                            "VIOLATION: API Client -> UI Library"
                        )


def main():
    parser = argparse.ArgumentParser(description="Validate Mobile MVVM architecture compliance")
    parser.add_argument(
        "--dir", "-d",
        default=".",
        help="Root directory of the mobile project to analyze (default: current directory)"
    )
    parser.add_argument(
        "--strict",
        action="store_true",
        help="Treat warnings as blocking errors"
    )

    args = parser.parse_args()

    print("=" * 70)
    print(" MOBILE MVVM ARCHITECTURE VALIDATOR (validate-mvvm.py)")
    print("=" * 70)
    print(f"Target Directory : {Path(args.dir).resolve()}")
    print(f"Strict Mode      : {'Enabled' if args.strict else 'Disabled'}")
    print("-" * 70)

    validator = MvvmValidator(target_dir=args.dir, strict=args.strict)
    passed = validator.run()

    print(f"Files Scanned    : {validator.files_scanned}")
    print(f"Violations Found : {len(validator.violations)}")
    print(f"Warnings Found   : {len(validator.warnings)}")
    print("-" * 70)

    if validator.warnings:
        print("\n[WARNINGS]")
        for w in validator.warnings:
            print(f"  * {w['file']}:{w['line']} - {w['message']}")

    if validator.violations:
        print("\n[ARCHITECTURAL VIOLATIONS]")
        for v in validator.violations:
            print(f"  ! {v['file']}:{v['line']} [{v['rule']}]")
            print(f"    -> {v['message']}")
        print("\nRESULT: FAILED (Resolve architecture violations to adhere to MVVM)")
        sys.exit(1)

    if args.strict and validator.warnings:
        print("\nRESULT: FAILED (--strict mode enabled and warnings were found)")
        sys.exit(1)

    print("\nRESULT: PASSED (All mobile files respect MVVM layer boundaries)")
    sys.exit(0)


if __name__ == "__main__":
    main()
