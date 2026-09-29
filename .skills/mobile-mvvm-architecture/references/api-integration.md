# API Integration with api-mapping.yaml

This guide explains how mobile MVVM features consume endpoints defined in `api-mapping.yaml`.

---

## 1. The Single Source of Truth
Never invent mobile API paths or guess parameter names. Every network request made by the mobile application must mirror the corresponding entry in `api-mapping.yaml`.

```yaml
# In api-mapping.yaml:
sales_dashboard:
  actions:
    revenue_kpi:
      id: sales.kpi.revenue
      type: kpi
      api:
        method: GET
        path: /api/v1/dashboard/sales/kpis/revenue
      parameters:
        - name: period
          type: string
          in: query
          required: false
          default: "mtd"
      response:
        type: RevenueKpiResponse
```

---

## 2. Layer Implementation Steps

### Step 1: Wire DTO (Data Transfer Object)
Model the raw JSON payload exactly as the backend returns it:
```dart
class RevenueKpiResponseDto {
  final String period;
  final double netRevenue;
  final double growthRate;
  final String currency;

  RevenueKpiResponseDto({
    required this.period,
    required this.netRevenue,
    required this.growthRate,
    required this.currency,
  });

  factory RevenueKpiResponseDto.fromJson(Map<String, dynamic> json) {
    return RevenueKpiResponseDto(
      period: json['period'] as String? ?? 'mtd',
      netRevenue: (json['net_revenue'] as num).toDouble(),
      growthRate: (json['growth_rate'] as num).toDouble(),
      currency: json['currency'] as String? ?? 'USD',
    );
  }

  RevenueKpi toDomain() {
    return RevenueKpi(
      period: period,
      amount: netRevenue,
      percentageChange: growthRate,
      currencyCode: currency,
    );
  }
}
```

### Step 2: API Client Method
Call the specific endpoint using the path and query parameters from `api-mapping.yaml`:
```dart
class SalesApiClient {
  final HttpClient _client;
  SalesApiClient(this._client);

  Future<RevenueKpiResponseDto> fetchRevenueKpi({String period = 'mtd'}) async {
    final response = await _client.get(
      '/api/v1/dashboard/sales/kpis/revenue',
      queryParameters: {'period': period},
    );
    return RevenueKpiResponseDto.fromJson(response.data);
  }
}
```

### Step 3: Repository Method
Invoke the API Client and map the DTO to a Domain Entity:
```dart
class SalesRepositoryImpl implements SalesRepository {
  final SalesApiClient _apiClient;
  SalesRepositoryImpl(this._apiClient);

  @override
  Future<RevenueKpi> getRevenueKpi({String period = 'mtd'}) async {
    final dto = await _apiClient.fetchRevenueKpi(period: period);
    return dto.toDomain();
  }
}
```

---

## 3. Network Error Abstraction
Do not let low-level HTTP errors (e.g. `SocketException`, `DioException`, HTTP 502) leak into the ViewModel. Convert them in the API Client or Repository into typed AppExceptions:

```dart
sealed class AppException implements Exception {
  final String message;
  const AppException(this.message);
}

class NetworkException extends AppException {
  const NetworkException([super.message = "No internet connection"]);
}

class UnauthorizedException extends AppException {
  const UnauthorizedException([super.message = "Session expired. Please log in again."]);
}

class ServerException extends AppException {
  final int statusCode;
  const ServerException(this.statusCode, [super.message = "Internal server error"]);
}
```
