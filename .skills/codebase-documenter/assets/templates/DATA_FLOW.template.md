# End-to-End Data Flow Architecture

> **Status**: Verified against active codebase data pathways.  
> **Source of Truth Rule**: Describe actual discovered responsibilities for each layer; never prescribe where validation or formatting "should" live.  
> **Last Audited**: [YYYY-MM-DD]

---

## 1. Discovered Data Flow Hierarchy

The data pipeline moves through discovered architectural layers in this codebase:

```
┌────────────────────────────────────────────────────────┐
│ 1. User Interface (Screen / Widget / Component)        │
│    - Renders visual state & captures user events       │
└──────────────────────────┬─────────────────────────────┘
                           │ Dispatches action / event
                           ▼
┌────────────────────────────────────────────────────────┐
│ 2. State Management (Store / ViewModel / Bloc)         │
│    - Discovered Responsibilities: [List actual roles,  │
│      e.g. emits states, triggers repository calls]     │
└──────────────────────────┬─────────────────────────────┘
                           │ Invokes domain operation
                           ▼
┌────────────────────────────────────────────────────────┐
│ 3. Repository / Domain Service Layer                   │
│    - Discovered Responsibilities: [List actual roles,  │
│      e.g. local cache management, network dispatch]    │
└──────────────────────────┬─────────────────────────────┘
                           │ Calls API client
                           ▼
┌────────────────────────────────────────────────────────┐
│ 4. Network / API Client                                │
│    - Contract Reference: api-mapping.yaml              │
│    - Attaches authorization headers & tokens           │
└──────────────────────────┬─────────────────────────────┘
                           │ HTTP / WebSocket request
                           ▼
┌────────────────────────────────────────────────────────┐
│ 5. Backend Controller & Business Logic                 │
│    - Discovered Responsibilities: [List actual roles,  │
│      e.g. auth check, request validation, DB query]    │
└──────────────────────────┬─────────────────────────────┘
                           │ Reads / writes data
                           ▼
┌────────────────────────────────────────────────────────┐
│ 6. Persistence / Database Layer                        │
│    - Discovered tables, transactions & migrations      │
└──────────────────────────┘
```

---

## 2. Exemplar Feature Flow: `[Feature Name]`

### Step 1: User Action & UI Trigger
- **Screen**: [`src/screens/[Screen].dart`](file:///c:/project/src/screens/Screen.dart) → `WidgetName`
- **Trigger**: User presses `[Button Name]` or triggers event.

### Step 2: State Transformation
- **State Handler**: [`src/viewmodels/[ViewModel].dart`](file:///c:/project/src/viewmodels/ViewModel.dart) → `ViewModel.method()`
- **State Transition**: `Idle` → `Loading` (UI reflects loading feedback).

### Step 3: Domain & Repository Orchestration
- **Repository**: [`src/repositories/[Repository].dart`](file:///c:/project/src/repositories/Repository.dart) → `Repository.method()`
- **Observed Behavior**: [Describe discovered caching or validation logic].

### Step 4: Network Transport
- **Authoritative Contract**: [`api-mapping.yaml#[action-id]`](file:///c:/project/api-mapping.yaml)
- **API Client**: [`src/api/[ApiClient].dart`](file:///c:/project/src/api/ApiClient.dart) → `ApiClient.method()`
- **Route**: `[METHOD] /api/v1/[path]`

### Step 5: Backend & Persistence
- **Route Handler**: [`server/controllers/[Controller].ts`](file:///c:/project/server/controllers/Controller.ts) → `Controller.method()`
- **Database Table**: `[table_name]` backed by [`migrations/[migration].sql`](file:///c:/project/migrations/migration.sql)

### Step 6: Response & State Resolution
- **Success Pathway**: State updates to `Success`, caching payload and refreshing UI.
- **Error Pathway**: Network failure or HTTP 4xx/5xx triggers `Error` state with user-facing message and retry action.
