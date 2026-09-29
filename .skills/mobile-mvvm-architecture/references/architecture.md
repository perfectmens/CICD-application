# Mobile MVVM Architecture Reference

This reference document details the theoretical and practical application of Model-View-ViewModel (MVVM) in modern mobile software engineering.

---

## 1. The Architectural Hierarchy

```
┌─────────────────────────────────────────────────────────────┐
│                         VIEW LAYER                          │
│  - Widgets / Screens / Components                           │
│  - Observes ViewModel state (Reactive / Stream / Listener)  │
│  - Forwards user gestures (taps, text input, refresh)       │
└──────────────────────────────┬──────────────────────────────┘
                               │ Observes & Dispatches
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                      VIEWMODEL LAYER                        │
│  - Screen-specific state holder                             │
│  - Presentation logic & formatters                          │
│  - Coordinates one or more repositories                     │
│  - Exposes immutable state (Loading, Success, Error, Empty) │
└──────────────────────────────┬──────────────────────────────┘
                               │ Invokes abstract methods
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                      REPOSITORY LAYER                       │
│  - Unified domain data access boundary                      │
│  - Coordinates Cache (memory / DB) & Remote Network API     │
│  - Transforms wire DTOs to Clean Domain Entities            │
│  - Implements offline-first or cache-first policies         │
└──────────────────────────────┬──────────────────────────────┘
                               │ Calls HTTP methods
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                      API CLIENT LAYER                       │
│  - Encapsulates HTTP client (Dio / Http / Axios / Retrofit) │
│  - Base URL, endpoints, headers, auth interceptors          │
│  - Pure JSON serialization / deserialization                │
└──────────────────────────────┬──────────────────────────────┘
                               │ Over the wire (HTTPS)
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                         BACKEND API                         │
│  - Authoritative data & calculations (api-mapping.yaml)     │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Dependency Inversion & Injection

Every layer should depend on **abstractions (interfaces/abstract classes)** rather than concrete implementations. This enables:
- Seamless mock injection during unit testing.
- Swapping network clients or local database storage engines without touching the UI.
- Strict compilation-time enforcement of layer boundaries.

```dart
// Abstract contract
abstract class OrdersRepository {
  Future<List<Order>> getOrders({int page = 1});
}

// ViewModel depends on the interface
class OrdersViewModel extends ChangeNotifier {
  final OrdersRepository _repository;
  OrdersViewModel({required OrdersRepository repository}) : _repository = repository;
}

// Concrete implementation in Data layer
class OrdersRepositoryImpl implements OrdersRepository {
  final OrdersApiClient _apiClient;
  final OrdersLocalCache _cache;
  OrdersRepositoryImpl(this._apiClient, this._cache);
  ...
}
```

---

## 3. Framework Adaptations

### Flutter (Dart)
- **State Management**: `ChangeNotifier`, `StateNotifier`, `Bloc/Cubit`, or `AsyncNotifier`.
- **View**: `StatelessWidget` with `Consumer` / `ListenableBuilder`.
- **Networking**: `Dio` or `http` inside `ApiClient` only.

### React Native / Expo (TypeScript)
- **State Management**: Custom hook `useViewModel()` or Zustand store.
- **View**: Functional React Component.
- **Networking**: Custom `ApiClient` wrapping `fetch` or `axios`.

### Kotlin (Android Native)
- **ViewModel**: `androidx.lifecycle.ViewModel` with `StateFlow`.
- **View**: Jetpack Compose Composable function.
- **Networking**: `Retrofit` / `Ktor`.

### Swift (iOS Native)
- **ViewModel**: `ObservableObject` / `@Observable` class.
- **View**: SwiftUI `View`.
- **Networking**: `URLSession` inside dedicated `ApiClient`.
