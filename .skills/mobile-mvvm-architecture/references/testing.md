# Mobile MVVM Testing Guide

This guide details how to unit test each layer of the MVVM architecture in isolation using mocks.

---

## 1. Testing Hierarchy

```
┌─────────────────┐       ┌─────────────────┐
│ ViewModel Tests │ ────► │ Mock Repository │
└─────────────────┘       └─────────────────┘

┌──────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│ Repository Tests │ ────► │ Mock API Client │   and   │   Mock Cache    │
└──────────────────┘       └─────────────────┘       └─────────────────┘

┌──────────────────┐       ┌────────────────────────┐
│ API Client Tests │ ────► │ Mock HTTP Client/Server│
└──────────────────┘       └────────────────────────┘

┌─────────────────┐       ┌────────────────┐
│ UI/Widget Tests │ ────► │ Mock ViewModel │
└─────────────────┘       └────────────────┘
```

---

## 2. ViewModel Unit Testing

ViewModels should be tested with 100% mocked repositories. Never hit live network or disk storage during ViewModel tests.

```dart
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';

class MockSalesRepository extends Mock implements SalesRepository {}

void main() {
  late MockSalesRepository mockRepo;
  late SalesDashboardViewModel viewModel;

  setUp(() {
    mockRepo = MockSalesRepository();
    viewModel = SalesDashboardViewModel(repository: mockRepo);
  });

  test('loadDashboard emits [loading, success] when repository succeeds', () async {
    // Arrange
    final fakeDashboard = SalesDashboard(totalRevenue: 100000, ordersCount: 42);
    when(() => mockRepo.getDashboard(
      from: any(named: 'from'),
      to: any(named: 'to'),
    )).thenAnswer((_) async => fakeDashboard);

    // Act
    await viewModel.loadDashboard();

    // Assert
    expect(viewModel.state.status, ScreenStatus.success);
    expect(viewModel.state.data?.totalRevenue, 100000);
    verify(() => mockRepo.getDashboard(from: any(named: 'from'), to: any(named: 'to'))).called(1);
  });

  test('loadDashboard emits [loading, error] when repository throws', () async {
    // Arrange
    when(() => mockRepo.getDashboard(
      from: any(named: 'from'),
      to: any(named: 'to'),
    )).thenThrow(const NetworkException("Connection timed out"));

    // Act
    await viewModel.loadDashboard();

    // Assert
    expect(viewModel.state.status, ScreenStatus.error);
    expect(viewModel.state.errorMessage, contains("Connection timed out"));
  });
}
```

---

## 3. Repository Testing
Verify:
1. API client is invoked with correct arguments.
2. DTO is correctly transformed into the domain entity.
3. Cache is populated on successful response.
4. If API fails and cache exists, fallback data is returned if configured.

---

## 4. API Client Testing
Verify:
1. Correct HTTP method (`GET`, `POST`, etc.) matches `api-mapping.yaml`.
2. Path and query parameters are correctly formatted.
3. Headers and authentication tokens are attached.
4. HTTP 401/403/500 errors are parsed and wrapped in typed exceptions.
