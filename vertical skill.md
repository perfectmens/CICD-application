---
name: vertical-slice-development
description: Enforce disciplined, end-to-end vertical slice software engineering. Guides the implementation of complete, working feature slices across all layers (UI, state, logic, API, database) while strictly adhering to code inspection, truth-in-codebase verification, minimal viable changes, exhaustive state handling, non-regression validation, and proof before completion. Use this skill whenever implementing new features, modifying application behavior, refactoring functionality, building full-stack slices, or whenever the user asks for vertical slicing, disciplined development, inspect-before-implementing, or verified software engineering.
---

# Vertical Slice Development & Disciplined Engineering

This skill guides you through implementing software features using strict **Vertical Slicing** and **Disciplined Engineering** principles. Instead of building isolated horizontal layers (e.g., creating only database models or only UI components without integration), you build complete, working feature slices end-to-end, grounded entirely in the actual codebase, with continuous verification at every step.

---

## The 15 Core Tenets

```
+--------------------------------------------------------------------------+
| Discovery & Grounding                                                    |
|  2. Inspect before implementing       5. Do not guess; verify            |
|  3. Codebase is source of truth       6. Never invent APIs or schemas    |
|  4. Reuse before creating                                                |
+--------------------------------------------------------------------------+
| Scoping & Vertical Slicing                                               |
|  1. Do vertical slicing development   8. Do not refactor unrelated code  |
|  7. Make smallest viable change       9. Implement features end-to-end   |
| 10. Keep app runnable after changes                                      |
+--------------------------------------------------------------------------+
| State Completeness                                                       |
| 11. Handle all important states (Success, Loading, Empty, Error, etc.)   |
+--------------------------------------------------------------------------+
| Verification & Quality Assurance                                         |
| 12. Validate after every change       14. Check for regressions          |
| 13. Test behavior, not just compile   15. Never claim without proof      |
+--------------------------------------------------------------------------+
```

---

## Phase 1: Discovery & Grounding (Inspection Before Action)

Never begin coding based on assumptions. Ground all plans in direct evidence from the project.

### 2. Inspect Before Implementing
Before modifying or creating any code:
- **Trace the relevant data flow**: Read the files handling similar features. Follow data from user interaction, through state management and network calls, down to the database or storage layer.
- **Understand architectural boundaries**: Identify how modules communicate (e.g., controllers, services, repositories, state stores).
- **Inspect configurations & dependencies**: Review `package.json`, `pubspec.yaml`, `requirements.txt`, or environment configurations to understand existing libraries and tools.

### 3. Treat the Existing Codebase as the Source of Truth
- Prioritize patterns, conventions, and idioms actively used in the project over generic internet tutorials or personal preferences.
- If the project uses a specific state management approach (e.g., Zustand, Riverpod, Redux), file structure, or naming convention, adhere strictly to it.

### 4. Reuse Before Creating
Before writing a new utility, component, helper, or model:
- Search the workspace using search tools (e.g., `grep_search`, `list_dir`).
- Check for existing UI components (buttons, modals, form inputs, loaders).
- Check for shared utilities (date formatters, API clients, validation helpers, error handlers).
- Reuse or extend existing abstractions instead of creating duplicates.

### 5. Do Not Guess; Verify
- Whenever a question arises regarding a function signature, return type, path, or prop name, locate and read the declaration.
- Do not assume parameters or file paths; check them in the files directly.

### 6. Never Invent APIs, Schemas, Dependencies, or Conventions
- **APIs**: Confirm endpoint paths, HTTP methods, query parameters, headers, and payload structures against existing API files, route definitions, or OpenAPI/Swagger specs.
- **Schemas**: Verify database columns, migrations, ORM models, and type definitions before querying or mutating data.
- **Dependencies**: Never import third-party packages that are not already present in the project's dependency manifest unless explicitly instructed by the user.

---

## Phase 2: Scoping & Execution (Vertical Slicing)

Build narrow, fully functional vertical slices rather than wide, incomplete horizontal layers.

### 1. Do Vertical Slicing Development
- A vertical slice delivers a functional capability across all required layers:
  ```
  [User Interface] --> [State/Presenter] --> [Domain Logic] --> [Data/API/Storage]
  ```
- Do not spend an entire iteration building only database tables, or only API stubs, or only mock screens without connection. Deliver a narrow slice that works from UI to data.

### 7. Make the Smallest Viable Change
- Keep edits focused strictly on satisfying the immediate requirement.
- Resist the urge to rewrite adjacent functions, reformat unaffected files, or add speculative "future-proofing".
- Smaller, focused diffs are easier to review, test, and debug.

### 8. Do Not Refactor Unrelated Code
- If you notice poorly written, legacy, or suboptimal code that is outside the direct path of your feature, leave it untouched.
- Only refactor code if it is strictly necessary to enable the requested slice.

### 9. Implement Features End-to-End
- Connect every link in the chain:
  1. Trigger: User action or event (tap, click, keyboard shortcut, schedule).
  2. State/Feedback: UI updates to reflect pending/loading status.
  3. Processing: Business rules and input validation.
  4. Persistence/Network: API call or storage read/write.
  5. Resolution: Render success state, update cache/store, or display user-friendly error.

### 10. Keep the Application Runnable After Each Meaningful Change
- Do not leave the workspace in a broken or unbuildable state across steps.
- If a multi-step edit is needed, make incremental changes that leave the project compiling and operational at each checkpoint.

---

## Phase 3: State Completeness (The 6-State Matrix)

A feature is not complete if it only handles the happy path.

### 11. Handle All Important States
Every vertical slice must explicitly account for the standard state matrix:

| State | UI / System Behavior | Verification |
|---|---|---|
| **1. Success / Normal** | Complete data displayed, user actions enabled | Happy path test |
| **2. Loading / Pending** | Spinner, skeleton screen, or disabled trigger with loading indicator | Slow network / async delay test |
| **3. Empty** | Meaningful empty message and call to action when no records exist | Zero-record query test |
| **4. Error** | Human-readable error message, retry mechanism, non-crashing UI | Malformed input / network error test |
| **5. Disabled** | Inactive state when prerequisites (e.g. valid form input, permissions) are unmet | Incomplete form / unauthorized test |
| **6. Edge / Boundary** | Handling long strings, null values, special characters, max limits, timeouts | Extreme input / boundary test |

---

## Phase 4: Verification & Quality Assurance (Evidence-Based Completion)

Never report work as complete without concrete proof.

### 12. Validate After Every Meaningful Change
- Run the quickest and most relevant validation tools immediately after edits:
  - Type checks (e.g., `tsc --noEmit`, `dart analyze`, `mypy`)
  - Linter (e.g., `eslint`, `flake8`)
  - Project build / test runners (e.g., `npm test`, `pytest`, `flutter test`)

### 13. Test Behavior, Not Just Compilation
- A green build does not mean the feature functions as intended.
- Execute unit tests, integration tests, or manual behavioral verification to confirm that business logic produces the expected output.
- Verify error branches and edge cases, not just the happy path.

### 14. Check for Regressions
- Run existing test suites that cover related modules.
- Ensure that your changes have not broken existing API contracts, database queries, or UI layouts.

### 15. Do Not Claim Completion Without Verification
- State what was verified, how it was verified, and the exact command or method used.
- If any part of the implementation could not be automatically validated (e.g., requiring external hardware, third-party credentials, or manual visual inspection), **explicitly disclose it** to the user.

---

## Vertical Slicing Checklist

Use this quick checklist during implementation:

```markdown
### Pre-Implementation (Grounding)
- [ ] Inspected relevant existing code, schemas, and routes.
- [ ] Confirmed existing patterns, libraries, and utilities to reuse.
- [ ] Verified API signatures, database schemas, and data contracts.

### Implementation (Vertical Slicing)
- [ ] Built complete end-to-end slice (UI -> Logic -> Data).
- [ ] Made smallest viable change; avoided unrelated refactoring.
- [ ] Handled all 6 states: Success, Loading, Empty, Error, Disabled, Edge.
- [ ] Kept codebase compiling and runnable after each change.

### Verification (Proof-of-Work)
- [ ] Ran linter / type checker / compiler with zero errors.
- [ ] Executed automated behavioral tests (unit, integration, or E2E).
- [ ] Verified regressions against surrounding features.
- [ ] Formulated transparent summary detailing verified vs unverified items.
```
