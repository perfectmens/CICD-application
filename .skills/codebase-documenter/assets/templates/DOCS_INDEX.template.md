# Project Documentation Master Index

> **Knowledge Governance**: Single navigable entry point for all project documentation.  
> **Freshness Policy**: Change-driven (documents must be audited whenever backing source code changes).  
> **Last Audited**: [YYYY-MM-DD]

---

## 1. Documentation Structure & Map

```
docs/
├── ARCHITECTURE.md          # System boundaries, actual module structure
├── DATA_FLOW.md             # End-to-end data pipelines (UI -> State -> Repo -> API -> DB)
├── ONBOARDING.md            # Developer setup, prerequisites, 5-minute quickstart
├── DATABASE.md              # Database schemas, migrations, indexes, ERDs
├── TROUBLESHOOTING.md       # Real verified errors and root cause fixes
├── features/                # Vertical slice documentation per feature
│   ├── auth.md              # Authentication, session tokens, permissions
│   └── dashboard.md         # Dashboard, KPI calculations, real-time sync
└── api/
    └── api-mapping.yaml     # Authoritative contract for all application APIs
```

---

## 2. Master Documentation Registry & Verification Status

| Category | Document | Backing Code Authority | Sync Status | Last Verified Commit |
| :--- | :--- | :--- | :--- | :--- |
| **System Overview** | [`README.md`](file:///c:/project/README.md) | `package.json`, root configs | ✅ In Sync | `<COMMIT_HASH>` |
| **Architecture** | [`docs/ARCHITECTURE.md`](file:///c:/project/docs/ARCHITECTURE.md) | `lib/`, `src/`, `server/` | ✅ In Sync | `<COMMIT_HASH>` |
| **Data Pipelines** | [`docs/DATA_FLOW.md`](file:///c:/project/docs/DATA_FLOW.md) | ViewModels, Controllers | ✅ In Sync | `<COMMIT_HASH>` |
| **API Contracts** | [`api-mapping.yaml`](file:///c:/project/api-mapping.yaml) | Backend routes, API client | ✅ In Sync | `<COMMIT_HASH>` |
| **Database** | [`docs/DATABASE.md`](file:///c:/project/docs/DATABASE.md) | `migrations/` | ✅ In Sync | `<COMMIT_HASH>` |
| **Setup & Dev** | [`docs/ONBOARDING.md`](file:///c:/project/docs/ONBOARDING.md) | Build scripts, Dockerfiles | ✅ In Sync | `<COMMIT_HASH>` |
| **Troubleshooting** | [`docs/TROUBLESHOOTING.md`](file:///c:/project/docs/TROUBLESHOOTING.md) | Verified observed errors | ✅ In Sync | `<COMMIT_HASH>` |

---

## 3. Governance & Pruning Rules

1. **Change-Driven Triggers**: When a commit modifies code backing any row above, mark that document as `⚠️ DRIFT CANDIDATE` until re-verified.
2. **Pruning Protocol**: When a feature or API is deleted, immediately remove or archive its documentation and update this registry.
3. **No Dangling Links**: Ensure every link in this table resolves to an existing file.
