#!/usr/bin/env python3
"""
validate-api-mapping.py
======================
Automated validation and linting tool for api-mapping.yaml.
Enforces API governance invariants:
  - Valid YAML structure and syntax
  - Required root fields (version, api, domains)
  - Unique action IDs
  - Unique (method, path) route declarations
  - Mandatory endpoint properties (method, path, status)
  - Deprecation metadata compliance (deprecated_since, replacement)
  - KPI calculation ownership (backend-owned business metrics)
  - Parameter path-variable consistency ({id} in path must have parameter declaration)
  - Consumer mapping validation

Exit codes:
  0: Validation passed (no errors)
  1: Validation failed (errors detected)
"""

import sys
import os
import argparse
import re
from pathlib import Path
from typing import Dict, Any, List, Tuple

try:
    import yaml
except ImportError:
    print("[FATAL] PyYAML is not installed. Run 'pip install pyyaml' first.", file=sys.stderr)
    sys.exit(1)

VALID_METHODS = {"GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS"}
VALID_STATUSES = {"active", "deprecated", "removed", "draft"}
VALID_ACTION_TYPES = {
    "dashboard", "kpi", "chart", "table", "drilldown",
    "action", "crud", "export", "realtime", "search",
    "summary", "aggregation"
}
VALID_PARAM_TYPES = {
    "string", "integer", "number", "boolean", "date", "datetime", "array", "object"
}
VALID_PARAM_IN = {"query", "path", "header", "body"}


class ApiValidator:
    def __init__(self, file_path: str, strict: bool = False):
        self.file_path = Path(file_path).resolve()
        self.strict = strict
        self.errors: List[str] = []
        self.warnings: List[str] = []
        self.action_ids: Dict[str, str] = {}  # id -> location
        self.endpoints: Dict[Tuple[str, str], str] = {}  # (method, path) -> location
        self.total_actions = 0
        self.total_dashboards = 0
        self.total_domains = 0

    def add_error(self, message: str, location: str = ""):
        prefix = f"[{location}] " if location else ""
        self.errors.append(f"{prefix}{message}")

    def add_warning(self, message: str, location: str = ""):
        prefix = f"[{location}] " if location else ""
        self.warnings.append(f"{prefix}{message}")

    def run(self) -> bool:
        if not self.file_path.exists():
            self.add_error(f"File not found: {self.file_path}")
            return False

        try:
            with open(self.file_path, "r", encoding="utf-8") as f:
                data = yaml.safe_load(f)
        except Exception as e:
            self.add_error(f"YAML Parsing Error: {e}")
            return False

        if not isinstance(data, dict):
            self.add_error("Root element must be a YAML mapping/dictionary.")
            return False

        self._validate_root(data)
        return len(self.errors) == 0

    def _validate_root(self, data: Dict[str, Any]):
        # Required top-level fields
        for field in ["version", "api", "domains"]:
            if field not in data:
                self.add_error(f"Missing required top-level field: '{field}'")

        # Validate api metadata
        api_meta = data.get("api")
        if isinstance(api_meta, dict):
            if "base_url" not in api_meta:
                self.add_warning("Field 'api.base_url' missing. Recommended: e.g. '/api'")
            if "version" not in api_meta:
                self.add_warning("Field 'api.version' missing. Recommended: e.g. 'v1'")
        elif api_meta is not None:
            self.add_error("Field 'api' must be a dictionary.")

        # Validate domains
        domains = data.get("domains")
        if not isinstance(domains, dict):
            self.add_error("Field 'domains' must be a dictionary of domain definitions.")
            return

        self.total_domains = len(domains)
        for domain_name, domain_data in domains.items():
            self._validate_domain(domain_name, domain_data)

    def _validate_domain(self, domain_name: str, domain_data: Any):
        loc = f"domain:{domain_name}"
        if not isinstance(domain_data, dict):
            self.add_error("Domain data must be a dictionary.", loc)
            return

        # Dashboards
        dashboards = domain_data.get("dashboards", {})
        if isinstance(dashboards, dict):
            self.total_dashboards += len(dashboards)
            for db_name, db_data in dashboards.items():
                self._validate_dashboard(domain_name, db_name, db_data)
        elif dashboards is not None:
            self.add_error("Field 'dashboards' must be a dictionary.", loc)

        # Domain actions
        actions = domain_data.get("actions", {})
        if isinstance(actions, dict):
            for action_name, action_data in actions.items():
                self._validate_action(
                    action_data,
                    location=f"{loc}.actions.{action_name}"
                )
        elif actions is not None:
            self.add_error("Field 'actions' must be a dictionary.", loc)

    def _validate_dashboard(self, domain_name: str, db_name: str, db_data: Any):
        loc = f"domain:{domain_name}.dashboards.{db_name}"
        if not isinstance(db_data, dict):
            self.add_error("Dashboard data must be a dictionary.", loc)
            return

        actions = db_data.get("actions", {})
        if isinstance(actions, dict):
            for action_name, action_data in actions.items():
                self._validate_action(
                    action_data,
                    location=f"{loc}.actions.{action_name}"
                )
        elif actions is not None:
            self.add_error("Dashboard 'actions' must be a dictionary.", loc)

    def _validate_action(self, action_data: Any, location: str):
        self.total_actions += 1
        if not isinstance(action_data, dict):
            self.add_error("Action definition must be a dictionary.", location)
            return

        # 1. Check ID
        action_id = action_data.get("id")
        if not action_id or not isinstance(action_id, str):
            self.add_error("Action missing required string field 'id'.", location)
        else:
            if action_id in self.action_ids:
                self.add_error(
                    f"Duplicate action id '{action_id}'. Already defined in {self.action_ids[action_id]}.",
                    location
                )
            else:
                self.action_ids[action_id] = location

        # 2. Check status
        status = action_data.get("status")
        if not status:
            self.add_error("Missing required field 'status'.", location)
        elif status not in VALID_STATUSES:
            self.add_error(
                f"Invalid status '{status}'. Must be one of: {', '.join(sorted(VALID_STATUSES))}",
                location
            )
        elif status == "deprecated":
            # Check deprecation metadata
            if not action_data.get("deprecated_since"):
                self.add_error("Deprecated action missing required 'deprecated_since' field.", location)
            if not action_data.get("replacement"):
                self.add_warning("Deprecated action should specify a 'replacement' action or endpoint.", location)

        # 3. Check action type
        action_type = action_data.get("type")
        if action_type and action_type not in VALID_ACTION_TYPES:
            self.add_warning(
                f"Unknown action type '{action_type}'. Standard types: {', '.join(sorted(VALID_ACTION_TYPES))}",
                location
            )

        # 4. Check API endpoint definition
        api_block = action_data.get("api")
        if not isinstance(api_block, dict):
            self.add_error("Action missing required 'api' dictionary block.", location)
            return

        method = api_block.get("method")
        path = api_block.get("path")

        if not method or not isinstance(method, str):
            self.add_error("Missing or invalid 'api.method'.", location)
        else:
            method_upper = method.upper()
            if method_upper not in VALID_METHODS:
                self.add_error(
                    f"Invalid HTTP method '{method}'. Must be one of: {', '.join(sorted(VALID_METHODS))}",
                    location
                )
            else:
                method = method_upper

        if not path or not isinstance(path, str):
            self.add_error("Missing or invalid 'api.path'.", location)
        elif not path.startswith("/"):
            self.add_error(f"API path '{path}' must begin with a forward slash ('/').", location)
        else:
            # Check duplicate method + path
            if method and status != "removed":
                key = (method, path)
                if key in self.endpoints:
                    self.add_warning(
                        f"Duplicate route '{method} {path}'. Already mapped in {self.endpoints[key]}.",
                        location
                    )
                else:
                    self.endpoints[key] = location

        # 5. Parameters & Path Variables
        parameters = action_data.get("parameters", [])
        param_names = set()
        if isinstance(parameters, list):
            for p in parameters:
                if isinstance(p, dict):
                    p_name = p.get("name")
                    p_type = p.get("type")
                    p_in = p.get("in", "query")

                    if not p_name:
                        self.add_error("Parameter missing 'name'.", location)
                    else:
                        param_names.add(p_name)

                    if p_type and p_type not in VALID_PARAM_TYPES:
                        self.add_warning(
                            f"Non-standard parameter type '{p_type}' for '{p_name}'.",
                            location
                        )
                    if p_in and p_in not in VALID_PARAM_IN:
                        self.add_error(
                            f"Invalid parameter 'in' value '{p_in}' for '{p_name}'. Must be one of: {', '.join(sorted(VALID_PARAM_IN))}",
                            location
                        )
        elif parameters is not None:
            self.add_error("Field 'parameters' must be a list.", location)

        # Check path variables like {id} or {orderId}
        if path and isinstance(path, str):
            path_vars = re.findall(r"\{([a-zA-Z0-9_]+)\}", path)
            for var in path_vars:
                if var not in param_names:
                    self.add_warning(
                        f"Path variable '{{{var}}}' in '{path}' is not declared in parameters list.",
                        location
                    )

        # 6. KPI Rules
        if action_type == "kpi" or "kpi" in action_data:
            kpi_block = action_data.get("kpi")
            if not isinstance(kpi_block, dict):
                self.add_error("KPI action must include a 'kpi' dictionary block.", location)
            else:
                if not kpi_block.get("name"):
                    self.add_error("KPI block missing required 'name' field.", location)
                calc_owner = kpi_block.get("calculation_owner", "backend")
                if calc_owner != "backend":
                    self.add_warning(
                        f"KPI calculation_owner is '{calc_owner}'. Rule: Business KPI calculations must live on backend to ensure consistency.",
                        location
                    )

        # 7. Visualization Rules
        if action_type == "chart" or "visualization" in action_data:
            viz = action_data.get("visualization")
            if not isinstance(viz, dict):
                self.add_warning("Chart action should define a 'visualization' dictionary block.", location)
            elif not viz.get("type"):
                self.add_warning("Visualization block should specify 'type' (e.g. line, bar, pie).", location)

        # 8. Consumers
        consumers = action_data.get("consumers")
        if consumers is not None:
            if not isinstance(consumers, list):
                self.add_error("Field 'consumers' must be a list of consumer names.", location)
            elif len(consumers) == 0 and status == "active":
                self.add_warning("Active action has an empty 'consumers' list.", location)


def print_banner():
    print("=" * 70)
    print(" API GOVERNANCE VALIDATOR (validate-api-mapping.py)")
    print("=" * 70)


def main():
    parser = argparse.ArgumentParser(description="Validate api-mapping.yaml contracts")
    parser.add_argument(
        "--file", "-f",
        default="api-mapping.yaml",
        help="Path to api-mapping.yaml (default: api-mapping.yaml in current dir)"
    )
    parser.add_argument(
        "--strict",
        action="store_true",
        help="Treat warnings as errors and exit with code 1"
    )
    parser.add_argument(
        "--quiet", "-q",
        action="store_true",
        help="Suppress informational output and print only errors"
    )

    args = parser.parse_args()

    target_path = Path(args.file)
    # If not found directly, look in common locations
    if not target_path.exists():
        candidates = [
            Path("templates/api-mapping.yaml"),
            Path("../templates/api-mapping.yaml"),
            Path("api-mapping.yaml"),
        ]
        for c in candidates:
            if c.exists():
                target_path = c
                break

    if not args.quiet:
        print_banner()
        print(f"Target File : {target_path.resolve()}")
        print(f"Strict Mode : {'Enabled' if args.strict else 'Disabled'}")
        print("-" * 70)

    validator = ApiValidator(str(target_path), strict=args.strict)
    success = validator.run()

    # Report results
    if not args.quiet:
        print(f"Domains Scanned   : {validator.total_domains}")
        print(f"Dashboards Scanned: {validator.total_dashboards}")
        print(f"Actions Scanned   : {validator.total_actions}")
        print("-" * 70)

    if validator.warnings:
        print(f"\n[WARNINGS] Found {len(validator.warnings)} warning(s):")
        for w in validator.warnings:
            print(f"  * {w}")

    if validator.errors:
        print(f"\n[ERRORS] Found {len(validator.errors)} validation error(s):")
        for e in validator.errors:
            print(f"  ! {e}")
        print("\nRESULT: FAILED (Errors must be resolved before proceeding)")
        sys.exit(1)

    if args.strict and validator.warnings:
        print("\nRESULT: FAILED (--strict mode enabled and warnings were found)")
        sys.exit(1)

    if not args.quiet:
        print("\nRESULT: PASSED (All API contracts, mappings, and rules are valid)")
    sys.exit(0)


if __name__ == "__main__":
    main()
