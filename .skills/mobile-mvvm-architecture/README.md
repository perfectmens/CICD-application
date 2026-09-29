# Mobile MVVM Architecture Skill (`mobile-mvvm-architecture`)

A project-level and global agent skill enforcing strict Model-View-ViewModel (MVVM) architecture across mobile applications, ensuring clean separation of concerns, testability, and seamless integration with `api-mapping.yaml` and the `api-governance` skill.

---

## Directory Structure

```
mobile-mvvm-architecture/
├── SKILL.md                          # Primary agent instructions and 21 governance sections
├── references/
│   ├── architecture.md               # Detailed layer responsibilities and inversion of control
│   ├── api-integration.md            # Linking with api-mapping.yaml and DTO mapping
│   ├── state-management.md           # 5 asynchronous screen states & filter flows
│   └── testing.md                    # Mocking strategies for isolated unit testing
├── templates/
│   ├── viewmodel.template            # Production ViewModel boilerplate
│   ├── repository.template           # Repository interface, caching & DTO conversion
│   ├── model.template                # Wire DTO vs Domain Entity separation
│   └── api-client.template           # Dedicated HTTP service client
├── scripts/
│   └── validate-mvvm.py              # Automated static analyzer detecting layer violations
└── README.md                         # Documentation
```

---

## The Non-Negotiable Flow

```
View  ──►  ViewModel  ──►  Repository  ──►  API Client  ──►  Backend API
```

1. **Views** only render UI and dispatch user gestures.
2. **ViewModels** own screen state and coordinate data fetching through Repositories.
3. **Repositories** provide a single data boundary, manage caching, and convert DTOs to Domain Entities.
4. **API Clients** exclusively handle HTTP communication, headers, and serialization.
5. **api-mapping.yaml** remains the single source of truth for all backend contracts.

---

## Running the Linter

Run the included validator against your mobile source directory:
```bash
python scripts/validate-mvvm.py --dir ./lib
```

To enforce strict zero-warning mode in CI/CD pipelines:
```bash
python scripts/validate-mvvm.py --dir ./lib --strict
```
