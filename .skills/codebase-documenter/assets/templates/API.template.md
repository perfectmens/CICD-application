# API Documentation

> **Authoritative Contract**: [`api-mapping.yaml`](file:///c:/project/api-mapping.yaml)  
> **Source of Truth Rule**: All endpoints documented below are derived directly from `api-mapping.yaml`. Never document an unmapped or unverified endpoint.  
> **Last Audited**: [YYYY-MM-DD]

---

## 1. Overview & Contract Mapping

- **Base URL**: `<API_BASE_URL>` (Configured via `API_URL` environment variable)
- **API Version**: `<API_VERSION>`
- **Authoritative Contract File**: [`api-mapping.yaml`](file:///c:/project/api-mapping.yaml)

---

## 2. Authentication Standard

Authentication requirements are defined per-action in `api-mapping.yaml`.

- **Scheme**: Bearer Token / API Key
- **Header**: `Authorization: Bearer <ACCESS_TOKEN>`

### Obtaining an Access Token:
```bash
# [EXAMPLE — NOT VERIFIED]
curl -X POST <API_BASE_URL>/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "<USER_EMAIL>", "password": "<PASSWORD>"}'
```

---

## 3. Standard Response & Error Format

All endpoints follow the response envelope defined in the contract:

### Success Response Envelope:
```json
{
  "success": true,
  "data": {}
}
```

### Error Response Envelope:
```json
{
  "success": false,
  "error": {
    "code": "<ERROR_CODE>",
    "message": "<ERROR_MESSAGE>"
  }
}
```

---

## 4. Endpoints & Action Mappings

### 4.1 Action: `[action_id_from_api_mapping]`

- **Mapping Reference**: [`api-mapping.yaml#[action-id]`](file:///c:/project/api-mapping.yaml)
- **HTTP Route**: `[METHOD] /api/v1/[path]`
- **Auth Required**: `Yes / No`
- **Frontend / Client Consumer**: [`path/to/client.dart`](file:///c:/project/path/to/client.dart) → `ApiClient.methodName()`
- **Backend Route Handler**: [`path/to/controller.ts`](file:///c:/project/path/to/controller.ts) → `Controller.handlerName()`

#### Request Parameters & Body:
```json
// [EXAMPLE — NOT VERIFIED]
{
  "example_field": "<VALUE>"
}
```

#### Expected Responses:
- **HTTP 200 OK**:
```json
// [EXAMPLE — NOT VERIFIED]
{
  "success": true,
  "data": {
    "id": "<ID>",
    "status": "<STATUS>"
  }
}
```
- **HTTP 400 Bad Request**: Validation failure
- **HTTP 401 Unauthorized**: Missing or expired `<ACCESS_TOKEN>`
- **HTTP 404 Not Found**: Resource does not exist
