# Mobile State Management Guide

This document details the standard state management patterns for mobile ViewModels under the MVVM architecture.

---

## 1. The 5 Core Asynchronous Screen States

Every screen that loads data asynchronously must handle these 5 states:

```
                  ┌──────────────┐
                  │   Initial    │
                  └──────┬───────┘
                         │ loadData()
                         ▼
                  ┌──────────────┐
                  │   Loading    │ (Skeleton / Spinner)
                  └──────┬───────┘
                         │
          ┌──────────────┼──────────────┐
          ▼              ▼              ▼
   ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
   │   Success   │ │    Empty    │ │    Error    │
   │ (Data list) │ │  (0 items)  │ │ (Retry UI)  │
   └──────┬──────┘ └─────────────┘ └─────────────┘
          │
          │ pullToRefresh()
          ▼
   ┌─────────────┐
   │ Refreshing  │ (Keep existing data visible + top spinner)
   └─────────────┘
```

---

## 2. State Modeling Pattern

Use immutable state classes or sealed classes to represent screen state cleanly:

```dart
enum ScreenStatus { initial, loading, success, empty, error, refreshing }

class DashboardState<T> {
  final ScreenStatus status;
  final T? data;
  final String? errorMessage;
  final DateTime fromDate;
  final DateTime toDate;
  final String? selectedRegion;

  const DashboardState({
    this.status = ScreenStatus.initial,
    this.data,
    this.errorMessage,
    required this.fromDate,
    required this.toDate,
    this.selectedRegion,
  });

  bool get isLoading => status == ScreenStatus.loading;
  bool get isRefreshing => status == ScreenStatus.refreshing;
  bool get isSuccess => status == ScreenStatus.success;
  bool get isEmpty => status == ScreenStatus.empty;
  bool get hasError => status == ScreenStatus.error;

  DashboardState<T> copyWith({
    ScreenStatus? status,
    T? data,
    String? errorMessage,
    DateTime? fromDate,
    DateTime? toDate,
    String? selectedRegion,
  }) {
    return DashboardState<T>(
      status: status ?? this.status,
      data: data ?? this.data,
      errorMessage: errorMessage ?? this.errorMessage,
      fromDate: fromDate ?? this.fromDate,
      toDate: toDate ?? this.toDate,
      selectedRegion: selectedRegion ?? this.selectedRegion,
    );
  }
}
```

---

## 3. Filter and Pagination Lifecycle

1. **User interaction**: User picks a date range or filter tag in the View.
2. **Action dispatch**: View calls `viewModel.setDateRange(from, to)`.
3. **Optimistic/Loading state**: ViewModel emits state with new filters and sets `status = ScreenStatus.loading`.
4. **Data fetch**: ViewModel calls `repository.getDashboard(from: from, to: to)`.
5. **State emission**:
   - If records returned > 0: `status = ScreenStatus.success`, `data = result`.
   - If records returned == 0: `status = ScreenStatus.empty`, `data = null`.
   - If error thrown: `status = ScreenStatus.error`, `errorMessage = e.message`.
