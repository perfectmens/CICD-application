---
name: codebase-documenter
description: Complete codebase discovery, documentation, traceability, and knowledge governance engine. Enforces domain-specific sources of truth (api-mapping.yaml for APIs, migrations for schemas, active code for architecture), source-precedence matrix, 5 evidence levels (VERIFIED, OBSERVED, DERIVED, INFERRED, UNKNOWN), secret redaction, change-driven freshness, cross-document consistency, documentation deletion/pruning, and objective PASS/FAIL audit scorecards. Use this skill whenever discovering, documenting, auditing, or maintaining codebase documentation to ensure zero hallucination and total synchronization with code reality.
---

# Codebase Documentation & Knowledge Governance

This skill empowers AI agents and engineers to **discover, accurately document, rigorously trace, validate, prune, and govern** documentation for an existing codebase.

It operates as an **active Knowledge Governance Engine**. It treats documentation as a verified representation of real, running code, strictly bound to domain-specific authorities, protected by evidence classification, and synchronized through change-driven triggers.

---

## 1. Core Foundational Principles

### 1.1 Source of Truth Depends on Domain

The blanket statement *"Codebase is the source of truth"* is too broad. Different domains have explicit authoritative sources:

```
┌─────────────────────────┬──────────────────────────────────┬────────────────────────────────────────┐
│ Domain                  │ Authoritative Source of Truth    │ Rule                                   │
├─────────────────────────┼──────────────────────────────────┼────────────────────────────────────────┤
│ API Contracts           │ api-mapping.yaml                 │ Authoritative for endpoints, params,   │
│                         │                                  │ actions, responses, and status         │
├─────────────────────────┼──────────────────────────────────┼────────────────────────────────────────┤
│ Database Schemas        │ migrations/ and schema files     │ Authoritative for tables, columns,     │
│                         │                                  │ types, indexes, and constraints        │
├─────────────────────────┼──────────────────────────────────┼────────────────────────────────────────┤
│ System Architecture     │ Verified code implementation     │ Authoritative for actual boundaries,   │
│                         │                                  │ layers, and communication patterns     │
├─────────────────────────┼──────────────────────────────────┼────────────────────────────────────────┤
│ External Dependencies   │ Package manifests                │ Authoritative for libraries, tools,    │
│                         │ (pubspec.yaml, package.json,etc) │ and runtime versions                   │
├─────────────────────────┼──────────────────────────────────┼────────────────────────────────────────┤
│ System Behavior         │ Active code + passing tests      │ Authoritative for operational logic    │
│                         │                                  │ and runtime edge cases                 │
├─────────────────────────┼──────────────────────────────────┼────────────────────────────────────────┤
│ Existing Documentation  │ NEVER authoritative over code    │ Must be verified or updated; cannot    │
│                         │ or contracts                     │ override implementation reality        │
└─────────────────────────┴──────────────────────────────────┴────────────────────────────────────────┘
```

### 1.2 Source-Precedence Matrix (Conflict Resolution)

When two artifacts disagree, apply this strict precedence order:

1. **API Contract vs. Backend Route**:
   - `api-mapping.yaml` defines the agreed contract.
   - If backend code deviates: **STOP & REPORT DISCREPANCY**.
   - Do not silently change documentation to match a defective route, and do not silently edit code without contract approval.
2. **Database Migration vs. ORM/Model Code**:
   - `migrations/` represents physical database reality.
   - If an ORM model declares a field that no migration creates: the migration wins. Document the missing migration as a defect.
3. **Package Manifest vs. Source Imports**:
   - Manifest (`package.json`, `pubspec.yaml`) wins for valid dependencies. Undeclared imports are defects.
4. **Documentation vs. Code/Schema/Contract**:
   - Implementation and authoritative contracts ALWAYS win over documentation.
   - Documentation NEVER overrides reality.

---

## 2. The Documentation Evidence Rule

Every documented statement must belong to an explicit evidence category. The agent must **NEVER** present `INFERRED` or `UNKNOWN` information as `VERIFIED`.

```
================================================================================
                         DOCUMENTATION EVIDENCE LEVELS
================================================================================
  VERIFIED  Directly confirmed from code, config, schema, passing tests,
            command execution, or authoritative contract (e.g. api-mapping.yaml).
            -> Document as fact with citation.

  OBSERVED  Seen during actual runtime execution, interactive testing, or logs.
            -> Document with observation context: "[Observed in build v1.2]".

  DERIVED   Logically derived from verified sources (e.g., call graphs).
            -> Document with derivation path.

  INFERRED  Educated hypothesis or likely convention.
            -> MUST be explicitly labeled: "[INFERRED — UNCONFIRMED]".
            -> NEVER document as established fact.

  UNKNOWN   Insufficient evidence in the codebase.
            -> Mark explicitly as "[UNKNOWN]" or "[No ADR/Commit found]".
            -> NEVER invent explanations, fake URLs, fake rationales, or commands.
================================================================================
```

### Prohibited Behaviors:
- **UNKNOWN → Fabricated Explanation**: If `Why We Chose This` is not in an ADR, issue, or commit message, write `[Rationale: UNKNOWN - No ADR/Commit found]`. Do NOT invent architectural philosophy.
- **UNKNOWN → Fake Placeholders**: Never use realistic-looking fake URLs (e.g. `https://api.example.com/v1`) or fake tokens. Use obvious syntax: `<API_BASE_URL>`, `<AUTH_TOKEN>`, `<PORT>`.
- **Unverified Examples**: Every example that was not directly executed must be explicitly marked:  
  `[EXAMPLE — NOT VERIFIED]`.

---

## 3. Secret-Redaction Rule (Zero Credentials in Docs)

The documentation agent must **NEVER** write real or realistic credentials into documentation files:
- ❌ API Keys, Private Keys, Certificates
- ❌ JWTs, Bearer Tokens, Refresh Tokens
- ❌ Passwords, Hashes, Salt strings (e.g., do NOT use `password123`)
- ❌ Database Connection Strings with embedded credentials
- ❌ Production URLs containing secret query parameters

**Mandatory Replacement**: Always use sanitized placeholders:
```markdown
Authorization: Bearer <ACCESS_TOKEN>
DATABASE_URL=postgres://<DB_USER>:<DB_PASSWORD>@<DB_HOST>:<DB_PORT>/<DB_NAME>
```

---

## 4. Citation Standards: Resilient Traceability

**Do NOT require line numbers for every claim.** Line numbers change on almost every commit and create brittle documentation.

### Resilient Citation Standard:
Cite **`File Path` + `Symbol / Class / Method`** with optional line ranges:
- ✅ [`src/auth/AuthRepository.ts`](file:///c:/project/src/auth/AuthRepository.ts) → `AuthRepository.login()`
- ✅ [`lib/screens/login_screen.dart`](file:///c:/project/lib/screens/login_screen.dart) → `_LoginScreenState.submitForm()`
- ✅ [`api-mapping.yaml#auth-login`](file:///c:/project/api-mapping.yaml)
- ✅ [`migrations/004_create_users.sql`](file:///c:/project/migrations/004_create_users.sql) → Table `users`
- ❌ `src/auth/AuthRepository.ts:L42-L85` (brittle unless referencing an immutable release tag).

---

## 5. Architectural Separation: Actual vs. Recommended

- **Actual Architecture**: Describe ONLY what currently exists in the codebase.
- **Prohibition**: Current architecture ≠ Ideal architecture. The documentation agent must NEVER silently "improve" or document aspirational architecture as if it is implemented.
- **Engineering Conventions**: Keep architectural recommendations (e.g. "reusable component used in 2+ places") strictly segregated into separate guideline documents, never mixed into factual architectural descriptions.

---

## 6. Descriptive Data-Flow Discovery

Data-flow documentation must be **descriptive, not prescriptive**:
- Do not assume that State Management performs validation or that Repositories format payloads.
- **Discover where responsibilities actually reside** in the specific codebase (e.g., MVVM, BLoC, Redux, Clean Architecture) and document the observed pathway.

### Canonical State Vocabulary
Unify state terminology across features while allowing feature-specific custom states:
- **`Idle`**: Resting state before trigger.
- **`Loading`**: Async operation or network request in progress.
- **`Success`**: Data retrieved/operation succeeded and rendered.
- **`Empty`**: Valid query returned zero records.
- **`Error`**: Failure state with user-facing message and retry option.
- **`Disabled`**: Action cannot be taken because prerequisites are not met.
- **`Offline`**: Device disconnected; serving cached data or queuing actions.
- **`Edge`**: Boundary conditions (e.g., character limits reached, pagination end).
- **Custom States**: Allowed when explicitly named in code (e.g., `AwaitingVerification`, `Expired`).

---

## 7. Change-Driven Freshness & Impact Detection

Documentation freshness is **change-driven, not time-driven**. A document does not become stale because 90 days passed; it becomes stale the moment its backing code changes.

### Documentation Impact Matrix

When code changes, immediately identify and update all impacted documents:

```
┌──────────────────────────────┬────────────────────────────────────────────────────────┐
│ Change Event                 │ Impacted Documentation Files to Update                 │
├──────────────────────────────┼────────────────────────────────────────────────────────┤
│ API added / changed / cut    │ api-mapping.yaml, docs/api/, docs/features/,           │
│                              │ docs/DATA_FLOW.md, README.md, test examples            │
├──────────────────────────────┼────────────────────────────────────────────────────────┤
│ DB Migration / Model change  │ docs/DATABASE.md, docs/features/, ERD diagrams         │
├──────────────────────────────┼────────────────────────────────────────────────────────┤
│ Environment Variable added   │ docs/ONBOARDING.md, .env.example, README.md            │
├──────────────────────────────┼────────────────────────────────────────────────────────┤
│ Architecture / Layer change  │ docs/ARCHITECTURE.md, docs/DATA_FLOW.md                │
├──────────────────────────────┼────────────────────────────────────────────────────────┤
│ CLI Script / Build change    │ docs/ONBOARDING.md, README.md, CI docs                 │
├──────────────────────────────┼────────────────────────────────────────────────────────┤
│ Feature removed / refactored │ docs/features/<feature>.md (DELETE or ARCHIVE),        │
│                              │ docs/DOCS_INDEX.md, docs/ARCHITECTURE.md               │
└──────────────────────────────┴────────────────────────────────────────────────────────┘
```

---

## 8. Documentation Completeness & Deletion Rules

### 8.1 Completeness Check (Detect Undocumented Surfaces)
Audit the repository for:
- 🔍 **Undocumented APIs**: Routes in backend controllers missing from `api-mapping.yaml` or API docs.
- 🔍 **Undocumented Migrations**: Tables in migrations missing from `DATABASE.md`.
- 🔍 **Undocumented Environment Variables**: Variables accessed via `process.env` or `Platform.environment` missing from `.env.example` and `ONBOARDING.md`.
- 🔍 **Undocumented Features / Screens**: Screens in router definitions missing from feature documentation.

### 8.2 Deletion & Pruning Protocol (No Dead Documentation)
When code, features, or APIs are deleted:
```
Implementation Code Removed
            ↓
Identify Affected Documentation via Impact Matrix
            ↓
Remove Obsolete Sections (or move to docs/archive/ if history is needed)
            ↓
Update docs/DOCS_INDEX.md
            ↓
Verify Cross-Document Consistency (No dangling references)
```

---

## 9. Cross-Document Consistency Validation

Before finalizing any documentation set, verify cross-document alignment:
- `README.md` ↔ `docs/ARCHITECTURE.md`: Tech stack, project purpose, and folder trees must match.
- `docs/ARCHITECTURE.md` ↔ `docs/DATA_FLOW.md`: Component responsibilities and layers must match.
- `docs/api/` ↔ `api-mapping.yaml`: Endpoints, methods, payloads, and action IDs must match 1-to-1.
- `docs/DATABASE.md` ↔ `migrations/`: Tables, foreign keys, and column types must match migrations.
- `docs/features/` ↔ `api-mapping.yaml`: Network actions cited in features must exist in the API mapping.
- `docs/ONBOARDING.md` ↔ Project Manifests: Setup commands and scripts must exist in `package.json` / `pubspec.yaml`.

---

## 10. Objective Documentation Audit Scorecard

Every audit produces an objective **PASS / FAIL** scorecard. Subjective quality scores are prohibited.

```
================================================================================
                    DOCUMENTATION AUDIT SCORECARD
================================================================================
  [PASS / FAIL]  ACCURACY       All documented claims match verified reality.
  [PASS / FAIL]  COMPLETENESS   Zero undocumented APIs, migrations, env vars, or screens.
  [PASS / FAIL]  TRACEABILITY   Claims cite File + Symbol/Class/Method.
  [PASS / FAIL]  FRESHNESS      Zero stale docs trailing recent code commits.
  [PASS / FAIL]  LINKS          Zero dead, broken, or unresolved local file links.
  [PASS / FAIL]  COMMANDS       All commands syntax-checked and tested for target OS.
  [PASS / FAIL]  API SYNC       Exact 1:1 match with authoritative api-mapping.yaml.
  [PASS / FAIL]  DB SYNC        Exact 1:1 match with physical migration scripts.
  [PASS / FAIL]  SECRETS        Zero exposed credentials, tokens, or private keys.
================================================================================
  OVERALL STATUS: [PASS / FAIL]
  FAILURES DETECTED: [List exact file and defect, or "None"]
================================================================================
```

---

## 11. Assets, Guidelines & Templates

- **Audit Checklist**: [`references/codebase_audit_checklist.md`](references/codebase_audit_checklist.md)
- **Traceability & Validation Guide**: [`references/traceability_and_validation_guide.md`](references/traceability_and_validation_guide.md)
- **Documentation Guidelines**: [`references/documentation_guidelines.md`](references/documentation_guidelines.md)
- **Visual Aids Guide**: [`references/visual_aids_guide.md`](references/visual_aids_guide.md)
- **Templates**:
  - Architecture: [`assets/templates/ARCHITECTURE.template.md`](assets/templates/ARCHITECTURE.template.md)
  - API: [`assets/templates/API.template.md`](assets/templates/API.template.md)
  - Data Flow: [`assets/templates/DATA_FLOW.template.md`](assets/templates/DATA_FLOW.template.md)
  - Feature: [`assets/templates/FEATURE.template.md`](assets/templates/FEATURE.template.md)
  - Database: [`assets/templates/DATABASE.template.md`](assets/templates/DATABASE.template.md)
  - Onboarding: [`assets/templates/ONBOARDING.template.md`](assets/templates/ONBOARDING.template.md)
  - Troubleshooting: [`assets/templates/TROUBLESHOOTING.template.md`](assets/templates/TROUBLESHOOTING.template.md)
  - Documentation Index: [`assets/templates/DOCS_INDEX.template.md`](assets/templates/DOCS_INDEX.template.md)
  - README: [`assets/templates/README.template.md`](assets/templates/README.template.md)
  - Code Comments: [`assets/templates/CODE_COMMENTS.template.md`](assets/templates/CODE_COMMENTS.template.md)
