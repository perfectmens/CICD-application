---
name: mobile-mvvm-architecture
description: >
  Build and maintain mobile applications using MVVM architecture with strict separation between Views, ViewModels, Repositories, API clients, Models, and backend API contracts. Use this skill whenever creating, modifying, refactoring, or reviewing mobile screens, application state, API integrations, dashboard/KPI features, navigation, data models, or tests.
---

# Mobile MVVM Architecture

## Purpose
This project mandates MVVM (Model-View-ViewModel) as the standard mobile application architecture.
The architecture enforces strict separation between:
- **View**: UI rendering, user interaction, lifecycle events.
- **ViewModel**: Screen state ownership, presentation logic, coordinating data calls.
- **Repository**: Single data-access boundary, cache coordination, DTO-to-Domain mapping.
- **API Client**: Pure HTTP communication, serialization, headers, auth interceptors.
- **Model**: Domain entities (business concepts) vs. API DTOs (wire schemas).
- **Backend API Contracts**: Authoritative definitions in `api-mapping.yaml`.

The mobile application communicates with backend services exclusively through the centralized API layer defined by the project's `api-mapping.yaml`.

---

# 1. Non-Negotiable Architecture
Adhere strictly to this unidirectional dependency flow:

```
View
  │
  ▼
ViewModel
  │
  ▼
Repository
  │
  ▼
API Client
  │
  ▼
Backend API (defined in api-mapping.yaml)
```

- **Models** are shared between the appropriate layers (Domain Models between Repository and ViewModel/View; DTOs between API Client and Repository).
- **Never reverse this dependency direction.**
- The **View** must **NOT** directly call APIs or Repositories.
- The **ViewModel** must **NOT** contain raw HTTP implementation details or API URLs.
- The **Repository** must **NOT** contain UI logic or UI state.
- The **API Client** must **NOT** contain business logic, screen logic, or UI state.

---

# 2. View
The **View** is responsible for:
- Rendering UI components and layout.
- Handling raw user gestures (taps, scrolls, inputs) and forwarding them to the ViewModel.
- Triggering navigation via abstract navigation handlers.
- Observing and displaying loading, error, empty, refreshing, and data states provided by the ViewModel.

### The View must NOT:
- Make direct HTTP/WebSocket network requests.
- Construct API URLs or query strings.
- Parse raw API responses.
- Perform business or KPI calculations.
- Access databases or storage engines directly.
- Contain complex business rules.

```
❌ Bad:
View ────► HTTP / Database ────► Backend

✅ Correct:
View ────► ViewModel ────► Repository ────► API Client ────► Backend
```

---

# 3. ViewModel
The **ViewModel** owns screen state.

### Typical Screen State:
- `loading`: Async operation underway.
- `data`: Typed domain model/entity list.
- `error`: User-friendly error message or typed error object.
- `empty`: Valid response containing 0 items.
- `refreshing`: Pull-to-refresh active while existing data remains visible.
- `filters`: Selected date range, category, search query.
- `pagination`: Current page, cursor, hasMore indicator.
- `selectedItem`: Currently highlighted or active record.

### Responsibilities:
- Exposing immutable or observable screen state to the View.
- Calling Repository methods in response to user actions.
- Applying presentation-level transformations (e.g. date formatting, localized label display).
- Managing active filters and pagination states.
- Coordinating multiple repository calls if a screen combines multiple domains.

### The ViewModel must NOT:
- Construct raw HTTP requests or import HTTP client packages (`dio`, `http`, `axios`, etc.).
- Know backend route strings, headers, or query-string encoding.
- Access database or persistent storage drivers directly.
- Import UI framework rendering widgets (`Widget`, `BuildContext`, `JSX`, etc.).

---

# 4. Repository
Repositories provide a clean, unified data-access boundary.

```dart
abstract class SalesRepository {
  Future<SalesDashboard> getDashboard({required DateTime from, required DateTime to, String? region});
  Future<RevenueKpi> getRevenueKpi({String period = 'mtd'});
  Future<List<RevenueTrendPoint>> getRevenueTrend({String interval = 'day'});
  Future<List<Order>> getOrders({int page = 1, String? status});
  Future<SalesDrilldown> getRevenueDrilldown({required DateTime date, int page = 1, int limit = 20});
}
```

The ViewModel communicates with Repositories rather than directly with API Clients.

### Repository Responsibilities:
- Selecting appropriate API client endpoints.
- Coordinating multiple data sources (e.g., in-memory cache, local SQLite/Hive/Room, remote API).
- Mapping wire DTOs (`SalesDashboardResponseDto`) to clean domain models (`SalesDashboard`).
- Implementing local/remote fallback strategies.
- Managing retry policies, exponential backoff, and offline cache synchronization.

---

# 5. API Client
The **API Client** owns network and HTTP communication.

### Responsibilities:
- Base URL and environment management.
- HTTP method configuration (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`).
- Request and response headers.
- Authentication token attachment via interceptors.
- Serialization (request body DTO to JSON) and deserialization (JSON to response DTO).
- Timeout configurations and network error abstraction (SocketException, timeout, 4xx/5xx).
- Request tracing IDs and logging.
- API versioning alignment with `api-mapping.yaml`.

### The API Client must NOT contain:
- Screen logic or widget trees.
- UI state variables.
- KPI business calculations.
- Navigation handling.

---

# 6. API Mapping Integration
`api-mapping.yaml` is the **API source of truth**. Every API consumed by the mobile application **MUST** exist in `api-mapping.yaml`.

Before implementing any mobile API call:
1. Read `api-mapping.yaml`.
2. Find the existing API contract and action ID.
3. Reuse the existing endpoint if appropriate.
4. If missing, update `api-mapping.yaml` **before** writing mobile code.
5. Implement the API client method with matching path, method, and parameters.
6. Implement the repository method and DTO-to-Domain mapping.
7. Connect the ViewModel method and screen state.
8. Connect the View UI widgets and user interactions.
9. Validate the contract.

> **Never create an undocumented or unmapped API call.**

---

# 7. Feature Structure
Organize features following a predictable, modular convention:

```
features/
└── sales_dashboard/
    ├── views/
    │   ├── sales_dashboard_screen.dart
    │   └── widgets/
    │       ├── revenue_kpi_card.dart
    │       └── revenue_trend_chart.dart
    ├── viewmodels/
    │   ├── sales_dashboard_viewmodel.dart
    │   └── sales_dashboard_state.dart
    ├── repositories/
    │   ├── sales_repository.dart
    │   └── sales_repository_impl.dart
    ├── models/
    │   ├── sales_dashboard.dart         # Domain Entity
    │   └── dto/
    │       ├── sales_dashboard_dto.dart # API DTO (fromJson/toJson)
    │       └── revenue_kpi_dto.dart
    └── services/
        ├── sales_api_service.dart
        └── sales_api_service_impl.dart
```

Adapt naming to framework conventions (e.g. Flutter, React Native, Swift, Kotlin) without adding unnecessary abstraction layers.

---

# 8. Dashboard Architecture
Dashboard screens are treated as normal MVVM features.

```
Sales Dashboard View
         │
         ▼
Sales Dashboard ViewModel
         │
         ▼
Sales Repository
         │
         ▼
Sales API Client
         │
         ▼
Backend API (/api/v1/dashboard/sales/...)
```

The ViewModel can coordinate multiple APIs through a single repository:
```
SalesDashboardViewModel
    └── SalesRepository
         ├── getRevenueKpi()
         ├── getOrdersKpi()
         ├── getRevenueTrend()
         ├── getOrdersByCategory()
         └── getRevenueDrilldown()
```

Do not create a separate ViewModel for every chart card unless the widget has completely independent lifecycle or background polling requirements.

---

# 9. KPI Rules
Business KPIs belong to the backend.

```
Examples:
- Net Revenue
- Profit Margin
- Customer Acquisition Cost (CAC)
- Retention Rate
- YoY / MoM Growth
```

The mobile ViewModel may format the value (e.g. currency symbol, thousand separators, color thresholds), but **must not independently calculate authoritative business metrics**.

```
✅ Correct:
Backend API ──► Revenue = $1,250,000, Growth = +12.4% ──► Repository ──► ViewModel ──► View

❌ Anti-Pattern:
Mobile App downloads 5,000 raw transactions and calculates revenue growth locally.
```

---

# 10. Filters & Search State
Dashboard filters live in ViewModel state.

```
Filter Change Flow:
User changes filter in View
         │
         ▼
ViewModel receives intent (e.g. changeDateRange(from, to))
         │
         ▼
ViewModel updates filter state & sets status to Loading
         │
         ▼
ViewModel calls Repository.getDashboard(from: from, to: to)
         │
         ▼
Repository queries API Client with updated query params
         │
         ▼
API returns fresh dataset
         │
         ▼
ViewModel updates data state to Success
         │
         ▼
View automatically re-renders with fresh data
```

---

# 11. Loading and Error States
Every asynchronous screen operation must explicitly account for:
- **Initial Loading**: Skeleton loader or spinner during first screen render.
- **Success**: Data loaded and populated.
- **Empty**: Server returned 0 items; show contextual empty illustration and action button.
- **Error**: Network failure, timeout, or server error; show retry button and clear message.
- **Refreshing**: Pull-to-refresh active while existing list/data remains visible.

Do not leave loading/error handling inside individual nested widgets unless there is a specific UI reason.

---

# 12. API Response Mapping
Keep wire DTOs separate from UI domain entities when responsibilities differ:

```
API Wire Response (JSON)
         │
         ▼
DTO Model (e.g. SalesDashboardResponseDto.fromJson)
         │
         ▼
Repository mapping (.toDomain())
         │
         ▼
Clean Domain Entity (e.g. SalesDashboard)
         │
         ▼
ViewModel State
         │
         ▼
View UI Presentation
```

Do not expose raw HTTP response maps or JSON objects throughout the application widgets.

---

# 13. Authentication
Authentication belongs strictly below the ViewModel:
- Token storage and refresh lifecycle belong in the network infrastructure (e.g., HTTP Interceptors).
- ViewModels simply invoke `authRepository.login(credentials)` or `authRepository.logout()`.
- Views **must never** manually attach `Authorization: Bearer <token>` headers.

---

# 14. Navigation
Views or ViewModels may request navigation through the application's router or navigation abstraction.
- **Never** put navigation implementation or UI context references into Repositories or API Clients.

---

# 15. Caching
Caching belongs in the Repository/data layer:
```
ViewModel
   │
   ▼
Repository
   ├── In-Memory Cache / Local Database
   └── API Client ──► Remote Backend
```
The ViewModel should not care whether data came from memory, disk, or network unless that distinction is an explicit UI requirement (e.g. "Offline Mode" banner).

---

# 16. Real-Time APIs (WebSockets / SSE)
For live streaming updates:
- Connection lifecycle and socket reconnects are owned by the Realtime Service/Client.
- The Repository subscribes to the stream and maps raw events to Domain events.
- The ViewModel listens to the repository stream and updates its screen state.
- The View displays real-time updates reactively.

---

# 17. Testing Strategy
Each layer must be independently testable in isolation:

| Layer Under Test | Mock Dependency | Verification Focus |
| :--- | :--- | :--- |
| **ViewModel** | Mock Repository | State transitions (`loading` → `success` / `error`), filter handling |
| **Repository** | Mock API Client, Mock Cache | DTO-to-Domain mapping, cache hits/misses, error fallback |
| **API Client** | Mock HTTP Server / Adapter | URL paths, query parameters, auth headers, serialization |
| **View (Widget)** | Mock ViewModel / Fake State | UI element presence, loading spinner, error text, user tap dispatch |

---

# 18. Creating a New Feature Checklist
1. Identify user action and screen requirements.
2. Check `api-mapping.yaml` for existing matching APIs; add entry if missing.
3. Define Models (Domain Entity and API DTO).
4. Create API Client service method and wire tests.
5. Create Repository interface and implementation with DTO-to-Domain mapping.
6. Create ViewModel with explicit state class (loading, success, error, empty).
7. Create View and bind UI widgets to ViewModel state.
8. Connect navigation and filters.
9. Verify all 5 async states (Initial Loading, Success, Empty, Error, Refreshing).
10. Run `python scripts/validate-mvvm.py` to ensure no architectural violations.

---

# 19. Modifying Existing Features Checklist
1. Locate the View, ViewModel, Repository, API Client, and corresponding entry in `api-mapping.yaml`.
2. Trace consumer dependencies across the feature.
3. Make the minimal necessary change respecting layer boundaries.
4. Update `api-mapping.yaml` if the API contract changes.
5. Update unit and widget tests.
6. Run `scripts/validate-mvvm.py` to confirm zero architectural violations.

---

# 20. Architecture Violations Catalog
Flag and immediately refactor any of the following anti-patterns:
- ❌ **View → API Client**: View directly triggering network requests.
- ❌ **View → Repository**: View bypassing the ViewModel to fetch data.
- ❌ **View → Database**: View directly accessing local storage/SQLite.
- ❌ **ViewModel → HTTP**: ViewModel importing `http`/`dio` or building URLs.
- ❌ **Repository → UI**: Repository importing UI widgets or holding `BuildContext`.
- ❌ **API Client → ViewModel**: API client dispatching UI state.
- ❌ **API Client → Navigation**: API client triggering screen navigation directly.
- ❌ **UI → Business KPI Calculation**: Client recalculating authoritative metrics.

---

# 21. Definition of Done
A mobile feature is complete only when:
- [ ] MVVM unidirectional dependency flow is respected (`View` → `ViewModel` → `Repository` → `API Client` → `Backend`).
- [ ] View contains zero direct API calls or network code.
- [ ] ViewModel owns all screen and filter state.
- [ ] Repository owns data access, caching, and DTO mapping.
- [ ] API Client owns HTTP communication and headers.
- [ ] All 5 async states (Loading, Success, Empty, Error, Refreshing) are handled.
- [ ] Unit tests are present for ViewModel and Repository.
- [ ] All consumed APIs exist in `api-mapping.yaml`.
- [ ] `validate-mvvm.py` passes with 0 violations.

### Standard Governance Summary:
```markdown
### Mobile MVVM Architecture Summary
- **Feature**: Sales Dashboard Overview
- **View**: `SalesDashboardScreen` (displays KPI cards, revenue chart, order list)
- **ViewModel**: `SalesDashboardViewModel` (manages `SalesDashboardState`, filters)
- **Repository**: `SalesRepository` (DTO to Domain mapping, in-memory cache)
- **APIs Consumed**:
  - `GET /api/v1/dashboard/sales` (`sales.dashboard.load`)
  - `GET /api/v1/dashboard/sales/kpis/revenue` (`sales.kpi.revenue`)
- **api-mapping.yaml**: Verified & synchronized
- **Architecture Validation**: Passed (0 violations)
- **Tests**: ViewModel unit tests passing
```

---

# Relationship to `api-governance` Skill

```
                     AI AGENT
                        │
         ┌──────────────┴──────────────┐
         ▼                             ▼
   API GOVERNANCE                 MOBILE MVVM
         │                             │
         ▼                             ▼
  api-mapping.yaml                    View
  API contracts                        │
  API lifecycle                        ▼
  API validation                   ViewModel
                                       │
                                       ▼
                                  Repository
                                       │
                                       ▼
                                   API Client
                                       │
                                       ▼
                                api-mapping.yaml
                                       │
                                       ▼
                                  Backend API
```

- **`api-governance` Skill**: "What APIs exist, what they do, and how their contracts are maintained."
- **`mobile-mvvm-architecture` Skill**: "How the mobile application consumes those APIs and structures its internal code."

---

# Bundled Skill Resources
- **References**:
  - [`references/architecture.md`](file:///references/architecture.md): Deep-dive into layers, dependency injection, and frameworks.
  - [`references/api-integration.md`](file:///references/api-integration.md): Contract integration with `api-mapping.yaml`, DTO conversion, and error handling.
  - [`references/state-management.md`](file:///references/state-management.md): Screen state modeling, filter flows, and pagination.
  - [`references/testing.md`](file:///references/testing.md): Comprehensive testing patterns with mocks.
- **Templates**:
  - [`templates/viewmodel.template`](file:///templates/viewmodel.template): Clean ViewModel with state handling.
  - [`templates/repository.template`](file:///templates/repository.template): Repository with caching and DTO mapping.
  - [`templates/model.template`](file:///templates/model.template): DTO vs Domain Entity pattern.
  - [`templates/api-client.template`](file:///templates/api-client.template): Centralized HTTP client service.
- **Validator**:
  - [`scripts/validate-mvvm.py`](file:///scripts/validate-mvvm.py): Static analyzer detecting architectural boundary violations.
