# Feature Specification: [Feature Name]

> **Feature Area**: [e.g., Authentication / Order Management / Analytics]  
> **Status**: Verified in codebase  
> **Authoritative Contract**: [`api-mapping.yaml#[action-id]`](file:///c:/project/api-mapping.yaml)  
> **Last Audited**: [YYYY-MM-DD]

---

## 1. Overview & Business Value
[Concise description of the feature's capability based on verified implementation.]

---

## 2. Vertical Slice Layer Mapping

| Layer | Component | File Path & Symbol | Discovered Responsibility |
| :--- | :--- | :--- | :--- |
| **UI** | Screen / Widget | [`src/views/[View].dart`](file:///c:/project/src/views/View.dart) → `ViewWidget` | Renders UI & captures interaction |
| **State** | ViewModel / Store | [`src/viewmodels/[ViewModel].dart`](file:///c:/project/src/viewmodels/ViewModel.dart) → `ViewModelClass` | Manages state & emits transitions |
| **Domain** | Repository | [`src/repositories/[Repo].dart`](file:///c:/project/src/repositories/Repo.dart) | Caching & domain rules |
| **Network** | ApiClient | [`src/api/[Client].dart`](file:///c:/project/src/api/Client.dart) → `ApiClient.method()` | Network dispatch |
| **Backend** | Route Handler | [`server/routes/[Route].ts`](file:///c:/project/server/routes/Route.ts) → `routeHandler` | Validation & DB query |
| **Database** | Migration / Table | [`migrations/[001].sql`](file:///c:/project/migrations/001.sql) → Table `table_name` | Persistent schema |

---

## 3. Unified State Handling Matrix

Every feature must verify and document all relevant canonical states:

| Canonical State | UI Representation | Trigger / Condition | Recovery / Transition |
| :--- | :--- | :--- | :--- |
| **`Idle`** | Initial / resting view | Screen mount | User dispatches action |
| **`Loading`** | Progress spinner / skeleton | Async operation in progress | Resolves to Success or Error |
| **`Success`** | Populated UI with data | Operation succeeded | User interaction |
| **`Empty`** | "No records found" view | Query returns 0 records | Prompt user to create item |
| **`Error`** | Error card / snackbar | Exception or HTTP 4xx/5xx | "Retry" action |
| **`Disabled`** | Grayed / unclickable button | Form invalid or missing pre-req | Complete required fields |
| **`Offline`** | Offline banner / cached data | Network unreachable | Reconnect or sync later |
| **`Edge`** | Limit warnings / boundary state | Character limit, last page | Paginate or adjust input |
| **`[Custom State]`**| [If applicable] | [Specific condition in code] | [Transition] |

---

## 4. API Contract & Payload Mapping

- **Action ID**: `[action_id]` (Verified in `api-mapping.yaml`)
- **HTTP Endpoint**: `[METHOD] /api/v1/[path]`
- **Request Body**:
```json
// [EXAMPLE — NOT VERIFIED]
{
  "field_name": "<VALUE>"
}
```
- **Response Payload**:
```json
// [EXAMPLE — NOT VERIFIED]
{
  "id": "<ID>",
  "status": "<STATUS>"
}
```

---

## 5. Verification Commands
```bash
# Unit & integration verification
[Insert verified test command, e.g. npm test or flutter test]
```
