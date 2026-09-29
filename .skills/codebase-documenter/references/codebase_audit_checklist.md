# Codebase Documentation Audit Checklist & Scorecard

Use this checklist to perform objective, evidence-based audits. Subjective ratings are prohibited; every category is evaluated strictly as **PASS** or **FAIL**.

---

## 1. Objective Audit Scorecard

```
================================================================================
                    DOCUMENTATION AUDIT SCORECARD
================================================================================
  [ ] ACCURACY       All documented claims match verified reality.
  [ ] COMPLETENESS   Zero undocumented APIs, migrations, env vars, or screens.
  [ ] TRACEABILITY   Claims cite File + Symbol/Class/Method (not brittle lines).
  [ ] FRESHNESS      Zero stale docs trailing recent code commits (change-driven).
  [ ] LINKS          Zero dead, broken, or unresolved local file links.
  [ ] COMMANDS       All commands syntax-checked and tested for target OS.
  [ ] API SYNC       Exact 1:1 match with authoritative api-mapping.yaml.
  [ ] DB SYNC        Exact 1:1 match with physical migration scripts.
  [ ] SECRETS        Zero exposed credentials, tokens, or private keys.
================================================================================
  OVERALL STATUS: [PASS / FAIL]
================================================================================
```

---

## 2. Detailed Audit Criteria

### 2.1 Accuracy & Evidence Levels
- [ ] Every factual claim is classified as **`VERIFIED`**, **`OBSERVED`**, or **`DERIVED`**.
- [ ] No unconfirmed assumptions are presented as fact (any assumption is explicitly labeled **`[INFERRED — UNCONFIRMED]`**).
- [ ] Unknown rationales are explicitly labeled **`[Rationale: UNKNOWN - No ADR/Commit found]`** instead of fabricated explanations.
- [ ] All code/payload examples that were not verified in runtime tests are labeled **`[EXAMPLE — NOT VERIFIED]`**.
- [ ] No realistic-looking fake URLs, ports, or IDs exist (e.g. `https://api.example.com/v1`); only standardized tokens like `<API_URL>` or `<PORT>`.

### 2.2 Completeness (Zero Undocumented Public Surfaces)
- [ ] **APIs**: Every route in backend controllers is documented in `api-mapping.yaml` and API documentation.
- [ ] **Database**: Every table and column in `migrations/` is documented in `DATABASE.md`.
- [ ] **Configuration**: Every environment variable accessed in source code is documented in `.env.example` and `ONBOARDING.md`.
- [ ] **Screens / Features**: Every primary route/screen is represented in feature documentation.
- [ ] **State Coverage**: Every documented feature includes all canonical states (`Idle`, `Loading`, `Success`, `Empty`, `Error`, `Disabled`, `Offline`, `Edge`).

### 2.3 Traceability & Citations
- [ ] Citations follow the resilient standard: `File Path` + `Symbol / Class / Method`.
- [ ] Links use valid markdown syntax (`file:///...` or relative paths).
- [ ] API documentation points directly to `api-mapping.yaml`.
- [ ] Database documentation points directly to migration files.

### 2.4 Freshness & Change-Driven Sync
- [ ] Audit matches git commit history: all documentation modified alongside or after the latest code changes touching those modules.
- [ ] Impact Matrix was verified: when APIs changed, all dependent feature docs and onboarding docs were updated.

### 2.5 Secret Redaction
- [ ] Zero real or fake passwords (no `password123`, no hardcoded test passwords).
- [ ] Zero JWT strings, API keys, private keys, or credentials in any documentation file.
- [ ] Connection strings are fully parameterized with `<USER>`, `<PASSWORD>`, `<HOST>`, `<DB>`.

### 2.6 Cross-Document Consistency
- [ ] `README.md` and `docs/ARCHITECTURE.md` agree on tech stack, folder tree, and project scope.
- [ ] `docs/ARCHITECTURE.md` and `docs/DATA_FLOW.md` agree on module responsibilities.
- [ ] `docs/features/` actions correspond 1-to-1 with `api-mapping.yaml`.
- [ ] `docs/ONBOARDING.md` scripts match commands in `package.json` / `pubspec.yaml`.

### 2.7 Pruning & Deletion
- [ ] Zero dead documentation referring to deleted features, removed APIs, or obsolete packages.
- [ ] Deleted components have been removed from `docs/DOCS_INDEX.md`.
