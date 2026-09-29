# Traceability, Evidence & Validation Guide

This guide establishes the mandatory operational protocols for maintaining **code-to-documentation traceability**, classifying **evidence levels**, enforcing **secret redaction**, detecting **documentation drift**, and resolving **discrepancies**.

---

## 1. Domain-Specific Source of Truth & Precedence Matrix

Different domains have different authorities. When two sources disagree, apply this strict precedence order:

```
┌─────────────────────────────────┬─────────────────────────────────┬─────────────────────────────────┐
│ Conflict                        │ Winning Authority               │ Required Action                 │
├─────────────────────────────────┼─────────────────────────────────┼─────────────────────────────────┤
│ api-mapping.yaml vs Controller  │ api-mapping.yaml                │ STOP & REPORT DISCREPANCY       │
│                                 │ (Authoritative API Contract)    │ Do NOT silently overwrite code. │
├─────────────────────────────────┼─────────────────────────────────┼─────────────────────────────────┤
│ migrations/ vs ORM Model        │ migrations/                     │ Migration reflects physical DB. │
│                                 │ (Physical Schema Authority)     │ Flag missing migration as bug.  │
├─────────────────────────────────┼─────────────────────────────────┼─────────────────────────────────┤
│ Manifest vs Source Imports      │ Package Manifest                │ Manifest reflects valid env.    │
│                                 │ (pubspec.yaml, package.json)    │ Undeclared import is defect.    │
├─────────────────────────────────┼─────────────────────────────────┼─────────────────────────────────┤
│ Documentation vs Code / Schema  │ Code / Schema                   │ Documentation NEVER wins over   │
│                                 │ (Implementation Reality)        │ working implementation reality. │
└─────────────────────────────────┴─────────────────────────────────┴─────────────────────────────────┘
```

---

## 2. Evidence Classification Protocol

Every documented claim must be grounded in an explicit evidence level:

| Level | Definition | Citation Requirement | Allowed in Factual Docs? |
| :--- | :--- | :--- | :--- |
| **`VERIFIED`** | Confirmed directly from code, config, schema, test, or contract. | Cite exact `File Path` + `Symbol / Method`. | ✅ Yes (Standard) |
| **`OBSERVED`** | Seen during actual runtime execution, interactive test, or logs. | Note execution context (e.g. `[Observed in test run]`). | ✅ Yes |
| **`DERIVED`** | Logically deduced from multiple verified sources (e.g. call tree). | Note derivation path. | ✅ Yes |
| **`INFERRED`** | Educated hypothesis or likely convention. | MUST be explicitly labeled `[INFERRED — UNCONFIRMED]`. | ⚠️ Only with clear label |
| **`UNKNOWN`** | Insufficient evidence in codebase / no ADR / no commit rationale. | Mark as `[UNKNOWN - No ADR/Commit found]`. | ❌ Never invent rationale |

### Strict Non-Fabrication Rules:
1. **Never turn `UNKNOWN` into a plausible story**: If there is no commit message, ADR, or comment explaining why a library was chosen, state `[Rationale: UNKNOWN]`. Do not fabricate architectural wisdom.
2. **Never use realistic placeholders**: Do not use real-looking mock domains (`api.example.com`), ports, or user IDs. Use generic tokens: `<API_URL>`, `<PORT>`, `<USER_ID>`.
3. **Explicit Example Labels**: All mock requests, payloads, or outputs must be labeled:  
   `[EXAMPLE — NOT VERIFIED]`.

---

## 3. Secret-Redaction Rules

Before writing any documentation or code comment, filter out all sensitive data:

| Sensitive Category | Prohibited Pattern in Docs | Mandatory Replacement |
| :--- | :--- | :--- |
| Passwords & Hashes | `password123`, `admin`, sha256 hashes | `<PASSWORD>`, `<HASH>` |
| Access Tokens & JWTs | `eyJhbGciOi...`, Bearer tokens | `<ACCESS_TOKEN>`, `<JWT_TOKEN>` |
| API Keys & Secrets | `AIzaSy...`, `sk_live_...` | `<API_KEY>`, `<CLIENT_SECRET>` |
| Connection Strings | `postgres://user:pass@host/db` | `postgres://<USER>:<PASSWORD>@<HOST>/<DB>` |
| Private Keys | `-----BEGIN RSA PRIVATE KEY-----` | `<PRIVATE_KEY_REDACTED>` |

---

## 4. Resilient Citation Standard (Symbol-Based)

To prevent documentation from becoming stale on every minor line shift:
- **Cite File + Symbol / Method**:
  ```markdown
  - **Controller**: [`lib/controllers/auth_controller.dart`](file:///c:/project/lib/controllers/auth_controller.dart) → `AuthController.authenticate()`
  - **ViewModel**: [`lib/viewmodels/auth_viewmodel.dart`](file:///c:/project/lib/viewmodels/auth_viewmodel.dart) → `AuthViewModel.login()`
  - **API Contract**: [`api-mapping.yaml#auth-login`](file:///c:/project/api-mapping.yaml)
  - **Database Table**: [`migrations/001_users.sql`](file:///c:/project/migrations/001_users.sql) → `users`
  ```
- Line ranges are **optional** and should only be used when targeting immutable git commit hashes or specific code spans.

---

## 5. Change-Driven Freshness & Impact Detection

Documentation drift occurs when code changes and dependent documentation is not updated.

### Automated Impact Detection Flow:
When a pull request or code change is analyzed:
1. Extract list of modified files (`git diff --name-only`).
2. Map modified files to documentation targets using the **Documentation Impact Matrix**:

```
Code Change                                Affected Documentation
────────────────────────────────────────────────────────────────────────
routes/*, controllers/*, api-client/*   → api-mapping.yaml, docs/api/*,
                                           docs/features/*, docs/DATA_FLOW.md
migrations/*, models/*                  → docs/DATABASE.md, docs/features/*
.env*, config/*                         → docs/ONBOARDING.md, README.md
package.json, pubspec.yaml              → docs/ONBOARDING.md, README.md
screens/*, views/*, widgets/*           → docs/features/*, UI component docs
```

3. Update only affected sections (Minimal Diff Rule).

---

## 6. Cross-Document Consistency Matrix

Execute cross-verification across documents:

```
┌───────────────────────────┬───────────────────────────┬────────────────────────────────────────────┐
│ Document A                │ Document B                │ Verification Condition                     │
├───────────────────────────┼───────────────────────────┼────────────────────────────────────────────┤
│ README.md                 │ docs/ARCHITECTURE.md      │ Tech stack and directory structure match.  │
├───────────────────────────┼───────────────────────────┼────────────────────────────────────────────┤
│ docs/ARCHITECTURE.md      │ docs/DATA_FLOW.md         │ Component boundaries and flows align.      │
├───────────────────────────┼───────────────────────────┼────────────────────────────────────────────┤
│ docs/api/                 │ api-mapping.yaml          │ Endpoints, methods, and status match 1:1.  │
├───────────────────────────┼───────────────────────────┼────────────────────────────────────────────┤
│ docs/DATABASE.md          │ migrations/               │ Tables, columns, and constraints match DB. │
├───────────────────────────┼───────────────────────────┼────────────────────────────────────────────┤
│ docs/features/*.md        │ api-mapping.yaml          │ Every API call references a valid mapped ID│
├───────────────────────────┼───────────────────────────┼────────────────────────────────────────────┤
│ docs/ONBOARDING.md        │ package.json/pubspec.yaml │ All scripts and commands exist and run.    │
└───────────────────────────┴───────────────────────────┴────────────────────────────────────────────┘
```

---

## 7. Documentation Deletion & Pruning Protocol

When code or features are deleted, documentation must be pruned to avoid dead knowledge accumulation:
1. **Identify**: Locate all documents mentioning the deleted component using `grep_search`.
2. **Remove / Archive**:
   - If an entire feature is deleted: Delete its feature document (`docs/features/<feature>.md`) or move it to `docs/archive/<feature>.md` with a deprecation notice.
   - If an API is removed: Mark as deprecated or remove from `api-mapping.yaml` and API docs.
3. **Index Cleanup**: Remove the deleted document from `docs/DOCS_INDEX.md`.
4. **Link Audit**: Verify that no other documents contain dead links to the removed content.

---

## 8. Discrepancy Reporting Template

If documentation conflicts with code or schema, log a discrepancy before making changes:

```markdown
### ⚠️ Documentation Discrepancy Report

- **Topic**: [e.g., User Authentication Route]
- **Documented In**: `docs/api/auth.md` (Claims: `POST /api/v1/auth/login`)
- **Authoritative Contract**: `api-mapping.yaml#auth-login` (Specifies: `POST /api/v2/auth/login`)
- **Code Implementation**: `server/routes/auth.ts` → `loginHandler` (Implements: `POST /api/v2/auth/login`)
- **Nature of Discrepancy**: Documentation is lagging behind the authoritative API contract and code.
- **Resolution**: Update `docs/api/auth.md` to reflect `POST /api/v2/auth/login`.
```
