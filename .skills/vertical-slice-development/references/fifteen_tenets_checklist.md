# The 15 Tenets of Disciplined Engineering - Quick Reference

| # | Tenet | Action Rule | Anti-Pattern to Avoid |
|---|---|---|---|
| **1** | **Do vertical slicing development** | Implement complete, working end-to-end features spanning UI, state, logic, and data. | Building horizontal layers in isolation (e.g., writing 5 models with no UI or wiring). |
| **2** | **Inspect before implementing** | Search and read existing code, dependencies, and architectural flow before writing code. | Coding immediately based on prompt assumptions. |
| **3** | **Treat existing codebase as source of truth** | Follow project-specific patterns, conventions, and state solutions. | Forcing external templates or conflicting idioms into an established project. |
| **4** | **Reuse before creating** | Search for existing UI components, utility functions, helpers, and types. | Duplicating functions or styling that already exist in shared modules. |
| **5** | **Do not guess; verify** | Locate and read actual function signatures, routes, props, and config files. | Guessing parameter names or types without checking definitions. |
| **6** | **Never invent APIs or schemas** | Confirm endpoint paths, HTTP verbs, payload keys, and database columns against source definitions. | Inventing non-existent endpoints or DB fields. |
| **7** | **Make the smallest viable change** | Write only the code required to fulfill the request. | Gratuitous rewriting or premature over-engineering. |
| **8** | **Do not refactor unrelated code** | Restrict changes to the feature's direct path. | Opportunistic refactoring that pollutes git diffs. |
| **9** | **Implement features end-to-end** | Connect triggers, loading states, validation, storage/API, and feedback. | Partial features that end at a `console.log` or stub. |
| **10** | **Keep app runnable after each change** | Ensure the build and test suite remain functional at every checkpoint. | Leaving broken intermediate commits or uncompilable states. |
| **11** | **Handle all important states** | Implement Success, Loading, Empty, Error, Disabled, and Boundary/Edge states. | Only implementing the happy path. |
| **12** | **Validate after every meaningful change** | Run linters, typecheckers, and test suites frequently. | Waiting until the end of a huge batch of edits to run checks. |
| **13** | **Test behavior, not just compilation** | Assert expected functional behavior and error paths, not merely lack of crash. | Assuming that zero compiler errors means zero logical bugs. |
| **14** | **Check for regressions** | Test neighboring features and run existing regression test suites. | Breaking adjacent functionality unnoticed. |
| **15** | **Do not claim completion without verification** | Provide verifiable test results and explicitly disclose any unverified parts. | Declaring "Done!" without executing or validating code. |
