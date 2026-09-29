# API Governance Agent Skill (`api-governance`)

A project-level and global agent skill enforcing that **`api-mapping.yaml` is the single source of truth** for all application API contracts, backend implementations, dashboards, KPIs, and client consumers (mobile & web).

---

## Directory Structure

```
api-governance/
├── SKILL.md                          # Primary agent instructions and API-first workflow
├── references/
│   └── api-mapping-schema.yaml       # Formal YAML specification of the mapping format
├── templates/
│   └── api-mapping.yaml              # Production-grade starter registry
├── scripts/
│   └── validate-api-mapping.py       # Automated linting & validation CLI script
└── README.md                         # This documentation
```

---

## The Core Invariant

```
       ┌────────────────────────────────────────────────────────┐
       │               api-mapping.yaml                         │
       │            (SINGLE SOURCE OF TRUTH)                    │
       └──────────────────────────┬─────────────────────────────┘
                                  │
         ┌────────────────────────┼─────────────────────────┐
         ▼                        ▼                         ▼
┌──────────────────┐    ┌──────────────────┐      ┌──────────────────┐
│ Backend Services │    │  Mobile Clients  │      │   Web Dashboard  │
│  & Controllers   │    │  (Flutter/React) │      │  (Next.js/React) │
└──────────────────┘    └──────────────────┘      └──────────────────┘
```

1. **Every action communicating with the backend MUST be mapped in `api-mapping.yaml`.**
2. **Every API change MUST update `api-mapping.yaml` in the same commit.**
3. **Dashboards and KPIs are first-class consumers.**
4. **Business KPI calculations MUST be owned by the backend.**

---

## Quickstart

### 1. Copy the Starter Template
In your project root:
```bash
cp templates/api-mapping.yaml ./api-mapping.yaml
```

### 2. Validate Your Mapping
Run the bundled Python validator against your mapping:
```bash
python scripts/validate-api-mapping.py --file ./api-mapping.yaml
```

### 3. Strict Mode in CI/CD
In CI/CD pipelines or pre-commit hooks, pass `--strict` to treat warnings as blocking errors:
```bash
python scripts/validate-api-mapping.py --file ./api-mapping.yaml --strict
```

---

## Validation Checks
The validator checks:
- [x] Valid YAML syntax and root structure (`version`, `api`, `domains`).
- [x] Global uniqueness of all Action IDs (prevents conflicting registrations).
- [x] Global uniqueness of route signatures (`METHOD /path`).
- [x] Standard HTTP verbs (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`).
- [x] Status lifecycle (`active`, `deprecated`, `removed`, `draft`).
- [x] Mandatory deprecation records (`deprecated_since`, `replacement`).
- [x] Backend calculation ownership for business KPIs.
- [x] Path variables (e.g. `{id}`) have matching parameter definitions.
- [x] Consumer bindings for all active endpoints.
