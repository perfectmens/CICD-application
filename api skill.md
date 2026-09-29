---
name: api-governance
description: >
  Design, implement, modify, validate, and maintain the application's API layer.
  Every frontend/mobile action and backend capability must be represented in the central API mapping.
  The API mapping YAML is the single source of truth for API contracts, actions, dashboards, KPIs, visualizations,
  authentication, parameters, responses, and implementation status. Use this skill whenever designing new endpoints,
  adding features that call APIs, modifying backend endpoints, changing frontend/mobile API calls, building dashboards/KPIs,
  or validating API consistency.
---

# API Governance Skill

## Core Principle
`api-mapping.yaml` is the **SINGLE SOURCE OF TRUTH** for application APIs.
- Every application action that communicates with the backend **MUST** have an entry in `api-mapping.yaml`.
- Every API change **MUST** update `api-mapping.yaml` in the same change.
- Never implement an API without updating the mapping.
- Never modify the API mapping without verifying the implementation.

---

# 1. Before Making Any Change
Before implementing any feature:
1. Read `api-mapping.yaml` (typically located in the project root or configured documentation path).
2. Search for an existing API/action that provides the required capability.
3. Reuse an existing API when appropriate.
4. Determine whether the requested feature requires:
   - A new API
   - Modification of an existing API
   - Removal/deprecation of an API
   - No API change
5. Update the mapping as part of the implementation. Do **NOT** blindly create a new endpoint.

---

# 2. API-First Workflow
For every feature or modification, adhere strictly to this sequence:

```
USER REQUEST / REQUIREMENT
         │
         ▼
┌─────────────────┐
│ Read API Mapping│ ◄────── Read api-mapping.yaml
└────────┬────────┘
         ▼
   Existing API?
       /   \
     YES   NO
     /       \
    ▼         ▼
Reuse or   Design New
Modify     API Contract
    │         │
    └────┬────┘
         ▼
┌─────────────────────────┐
│ UPDATE api-mapping.yaml │ ◄── Document first (contract frozen)
└────────┬────────────────┘
         ▼
┌─────────────────────────┐
│ IMPLEMENT BACKEND       │ ◄── Implement endpoint & controllers
└────────┬────────────────┘
         ▼
┌─────────────────────────┐
│ IMPLEMENT APP / CLIENT  │ ◄── Connect mobile / frontend client
└────────┬────────────────┘
         ▼
┌─────────────────────────┐
│ VALIDATE CONTRACT       │ ◄── Run scripts/validate-api-mapping.py
└────────┬────────────────┘
         ▼
    DONE / COMMIT
```

> **Non-Negotiable Rule:** No API-related implementation is considered complete until `api-mapping.yaml` has been updated and validated.

---

# 3. Application Actions
Treat user-visible actions as first-class API capabilities. Every action triggering network communication must be bound to a mapped API. Examples:
- Login / Logout / Refresh Token
- Create / Update / Cancel Order
- Update User Profile
- Load Dashboard / Summary
- Load KPI Card
- Filter Dashboard / Apply Date Range
- Drill down into KPI / Metric Breakdown
- Load Chart / Trend Line
- Export Report (CSV, PDF, Excel)
- Search / Autocomplete
- Refresh Data / Pull-to-refresh
- Receive Real-time WebSocket Event

Each action entry in the mapping must identify the exact API method, path, and consumer.

---

# 4. Dashboard and Analytics APIs
Dashboards and analytical views are dedicated API consumers. Categorize API endpoints by purpose:
- **KPI**: Single-value or comparison business metrics.
- **Chart**: Time-series or grouped datasets intended for visualization.
- **Table**: Paginated, sortable, and filterable record sets.
- **Drill-down**: Contextual granular data behind a KPI or chart slice.
- **Summary**: High-level aggregated figures across dimensions.
- **Aggregation**: Grouped totals by category, region, or time period.
- **Export**: Batch file generation and download.
- **Real-time**: Streaming updates via WebSockets or Server-Sent Events.
- **Filter / Search**: Dimension lookups and query endpoints.

### UI vs. Backend Separation of Concerns
- **Never put visualization rendering logic in the API.**
- The **backend** provides structured data and authoritative business calculations.
- The **frontend / mobile client** renders:
  - KPI cards
  - Line / bar / pie charts
  - Tables and grids
  - Gauges and progress indicators
  - Other client visual widgets

---

# 5. KPI Rules
Business KPI calculations **MUST** live on the backend.

```yaml
revenue_kpi:
  id: sales.kpi.revenue
  type: kpi
  api:
    method: GET
    path: /api/v1/dashboard/sales/kpis/revenue
  kpi:
    name: revenue_growth
    calculation_owner: backend
    unit: percentage
```

- **Do not duplicate business calculations in the client application.**
- The same KPI calculation must produce the identical result for:
  - Mobile apps
  - Web applications
  - Admin dashboards
  - Automated reports
  - External API consumers

---

# 6. API Naming and Conventions
Follow clean, RESTful resource-oriented naming:
- `GET /api/v1/dashboard/sales`
- `GET /api/v1/dashboard/sales/kpis`
- `GET /api/v1/dashboard/sales/revenue-trend`
- `GET /api/v1/dashboard/sales/orders`
- `GET /api/v1/dashboard/sales/drilldown`
- `POST /api/v1/orders`
- `GET /api/v1/orders/{id}`
- `PUT /api/v1/orders/{id}`
- `DELETE /api/v1/orders/{id}`

Always use explicit versioning (e.g., `/api/v1`). Do not introduce inconsistent or ad-hoc endpoint structures.

---

# 7. Before Creating a New Endpoint
Ask these questions before declaring a new route:
1. Does an existing endpoint already provide this data or capability?
2. Can an existing endpoint be extended safely (e.g., optional query param, field expansion)?
3. Is the new endpoint a genuinely different resource or operation?
4. Will the endpoint be reusable by other clients (web, mobile, integrations)?
5. Does it belong to an existing domain or require a new domain?
6. Does it require different authorization levels or role checks?
7. Does it require different caching or performance characteristics?

**Default to reuse over endpoint proliferation.**

---

# 8. API Contract Specifications
Every API mapping entry in `api-mapping.yaml` should define, where applicable:
- **`id`**: Unique dot-notated identifier (e.g., `sales.dashboard.load`, `orders.create`).
- **`domain`**: Functional area (`sales`, `auth`, `inventory`, `billing`).
- **`action`**: User or system action name.
- **`endpoint` / `path`**: URL path with standard parameter syntax (`/api/v1/...`).
- **`method`**: HTTP verb (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`).
- **`purpose`**: Plain-text description of business purpose.
- **`auth`**: Authentication requirements (`required: true/false`, `roles: [...]`).
- **`parameters`**: List of query, path, and header parameters (`name`, `type`, `required`, `description`).
- **`request`**: Request body schema / model type.
- **`response`**: Response model type, status codes, and payload schema.
- **`errors`**: Expected error codes and error payload structures.
- **`pagination` / `filtering` / `sorting`**: Supported paging and query capabilities.
- **`caching`**: Caching strategy, TTL, and cache keys.
- **`realtime`**: WebSocket event or SSE channel if applicable.
- **`consumers`**: List of client components bound to this API (e.g., `mobile.sales_dashboard.revenue_card`).
- **`backend`**: Service/controller/handler implementation reference.
- **`status`**: `active`, `deprecated`, `removed`, or `draft`.
- **`version`**: Contract revision number.

---

# 9. Modification Rules
When modifying an existing API:
1. Locate the entry in `api-mapping.yaml`.
2. Inspect `consumers` to understand every dependent frontend, mobile, or backend component.
3. Determine whether the change is backward-compatible (non-breaking):
   - Adding optional fields: usually safe.
   - Renaming fields, altering types, or removing fields: breaking change.
4. Update `api-mapping.yaml` first.
5. Update backend implementation.
6. Update all registered consumers in frontend and mobile codebases.
7. Validate request/response contracts and test end-to-end.
8. Record version or deprecation details if required.
9. **Never silently change an existing API contract.**

---

# 10. New Feature Rule
If a new user requirement or feature introduces any backend interaction:
1. **STOP** before writing backend or frontend implementation.
2. Formulate and record the API mapping entry in `api-mapping.yaml`.
3. Implement the backend route, controller, and tests.
4. Implement mobile / web frontend client callers and state bindings.
5. Run the validation script to verify consistency.

---

# 11. Deprecated and Removed APIs
Never silently delete or drop an API from the documentation:
1. Set `status: deprecated` or `status: removed`.
2. Include mandatory deprecation metadata:
   - `deprecated_since`: Version or date when deprecated.
   - `replacement`: Replacement endpoint or action ID.
   - `removal_target`: Planned version or sunset date.
3. Keep the deprecated entry in `api-mapping.yaml` until the sunset date is reached and all consumers have migrated.

---

# 12. Validation Checklist
Before completing any API-related task, verify:
- [ ] Every API implementation in code has an entry in `api-mapping.yaml`.
- [ ] Every active mapped API has a concrete backend implementation.
- [ ] HTTP methods match between code and mapping.
- [ ] Endpoint paths and parameters match implementation exactly.
- [ ] Request parameters and body match code expectations.
- [ ] Response structures match backend response models.
- [ ] Authentication and role requirements match security middleware.
- [ ] Dashboard and KPI consumers are mapped to the active UI screens.
- [ ] Deprecated APIs have replacement pointers and sunset targets.
- [ ] No duplicate IDs or duplicate `(method, path)` combinations exist.

### Automated Validation Tool
Execute the bundled validator script:
```bash
python scripts/validate-api-mapping.py --file path/to/api-mapping.yaml
```
If validation errors are reported, resolve them before declaring the task complete.

---

# 13. Source of Truth Hierarchy
When contract discrepancies occur, use this resolution hierarchy:
1. `api-mapping.yaml`
2. Backend API implementation
3. Frontend / mobile API client
4. External documentation

> **Reconciliation Mandate:** If the active implementation differs from `api-mapping.yaml`, **DO NOT** silently pick one. Report the exact discrepancy, confirm business intent, and reconcile both the YAML mapping and the code.

---

# 14. Final Response Format
When completing an API-related task, always provide a concise governance summary:

```markdown
### API Governance Summary
- **APIs Added**:
  - `GET /api/v1/dashboard/sales` (`sales.dashboard.load`)
  - `GET /api/v1/dashboard/sales/kpis` (`sales.kpi.summary`)
- **APIs Modified**:
  - `GET /api/v1/orders` (Added optional query parameter `status`)
- **APIs Deprecated/Removed**:
  - None
- **Mapping Status**: `api-mapping.yaml` updated
- **Validation**: Passed (0 errors, 0 warnings)
- **Affected Consumers**:
  - Mobile Sales Dashboard (`mobile.sales_dashboard`)
  - Sales KPI Screen (`mobile.sales_kpi_card`)
```

---

# Bundled Skill Resources
- **Schema**: [`references/api-mapping-schema.yaml`](file:///references/api-mapping-schema.yaml) — Formal YAML schema specifying all valid keys, nested structures, and validation rules.
- **Template**: [`templates/api-mapping.yaml`](file:///templates/api-mapping.yaml) — Production-ready template with sales dashboard, KPIs, charts, orders CRUD, auth, and consumers.
- **Validator**: [`scripts/validate-api-mapping.py`](file:///scripts/validate-api-mapping.py) — Automated Python CLI tool to lint and validate `api-mapping.yaml`.
