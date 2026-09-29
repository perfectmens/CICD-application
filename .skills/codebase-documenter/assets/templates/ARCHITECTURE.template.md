# Architecture Overview

> **Status**: Verified against active codebase structure and entry points.  
> **Source of Truth Rule**: Document ACTUAL architecture as discovered in the code, never assumed or speculative architecture. Current architecture ≠ Ideal architecture.  
> **Last Audited**: [YYYY-MM-DD]

---

## 1. System Design

### 1.1 High-Level Architecture

```
┌────────────────────────────────────────────────────────┐
│ 1. Client / Presentation Layer                         │
│    [Discovered framework and active UI components]     │
└──────────────────────────┬─────────────────────────────┘
                           │ Network / IPC
                           ▼
┌────────────────────────────────────────────────────────┐
│ 2. Backend / Service Layer                             │
│    [Discovered routing, controllers, business services]│
└──────────────────────────┬─────────────────────────────┘
                           │ Storage Protocol
                           ▼
┌────────────────────────────────────────────────────────┐
│ 3. Persistence / Database Layer                        │
│    [Discovered database engine and migration tool]     │
└────────────────────────────────────────────────────────┘
```

### 1.2 Technology Stack

> **Evidence Rule**: Only document rationales backed by an ADR, commit, issue, or configuration comment. If unknown, state `[Rationale: UNKNOWN - No ADR/Commit found]`.

| Layer | Technology | Verified In | Architectural Rationale |
| :--- | :--- | :--- | :--- |
| **Frontend** | [e.g., Flutter / React] | [`pubspec.yaml`](file:///c:/project/pubspec.yaml) | [Cite ADR/commit, or "Rationale: UNKNOWN"] |
| **Backend** | [e.g., Node.js / Python] | [`package.json`](file:///c:/project/package.json) | [Cite ADR/commit, or "Rationale: UNKNOWN"] |
| **Database** | [e.g., PostgreSQL] | [`docker-compose.yml`](file:///c:/project/docker-compose.yml) | [Cite ADR/commit, or "Rationale: UNKNOWN"] |

---

## 2. Verified Directory Structure

```
project-root/
├── [Discovered Directory 1]/    # Verified purpose based on active code
├── [Discovered Directory 2]/    # Verified purpose based on active code
└── [Entry Point File]           # Discovered application initialization
```

### Module Boundaries & Responsibilities

- **`[module_1]`**: [Describe actual responsibilities discovered in code]
  - **Primary Entry**: [`path/to/entry.ts`](file:///c:/project/path/to/entry.ts) → `SymbolName`
- **`[module_2]`**: [Describe actual responsibilities discovered in code]
  - **Primary Entry**: [`path/to/module.ts`](file:///c:/project/path/to/module.ts) → `SymbolName`

---

## 3. Verified Data Flow Overview

> **Note**: For comprehensive per-feature data flows, see [`docs/DATA_FLOW.md`](file:///c:/project/docs/DATA_FLOW.md).

```
[EXAMPLE — NOT VERIFIED: Concrete feature flow]
User Action → View Component → ViewModel / Store → Repository → ApiClient → Backend Router → Database
```

---

## 4. Architectural Decision Records (ADRs)

Document only decisions that have verifiable records in the codebase (commit history, ADR folder, PR descriptions):

### ADR-[Number]: [Decision Title]
- **Status**: [VERIFIED in commit `<HASH>` / ADR file `docs/adr/001.md`]
- **Context**: [Verified problem statement from record]
- **Decision**: [What was chosen]
- **Documented Trade-offs**: [Pros and cons noted in original record]

---

## 5. Engineering Conventions (Non-Factual Guidelines)

> **Important**: This section contains design guidelines and team conventions. It is kept strictly distinct from the factual architecture description above.

- [Team convention on state management patterns]
- [Team convention on folder structuring]
