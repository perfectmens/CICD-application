# Troubleshooting & Verified Solutions Guide

> **Core Rule**: Document ONLY real, observed failures (`OBSERVED`) and verified solutions (`VERIFIED`). Never document hypothetical or speculative errors.  
> **Last Audited**: [YYYY-MM-DD]

---

## 1. Verified Issue Registry

| Error Signature | Subsystem | Evidence Level | Verified Quick Fix |
| :--- | :--- | :--- | :--- |
| `PSSecurityException: UnauthorizedAccess` | Windows PowerShell | OBSERVED | Run via `npx.cmd` or bypass policy |
| `RenderFlex overflowed by X pixels` | Flutter UI | OBSERVED | Wrap column in `SingleChildScrollView` |

---

## 2. Issue Details & Root-Cause Remediation

### 2.1 Issue: `[Exact Error Message Signature]`

- **Subsystem**: [Build / Database / Network / Runtime]
- **Evidence Level**: `OBSERVED in [Environment / OS]`
- **Encountered When**: [Exact command or user action that triggered error]
- **Root Cause**: [Verified technical reason why the failure occurred]

#### Diagnostic Steps:
1. Run diagnostic command:
   ```bash
   [Diagnostic command, e.g. docker ps]
   ```

#### Verified Solution:
```bash
[Exact terminal command or code edit that resolved the issue]
```

#### Prevention:
- [Pre-commit hook, CI check, or lint rule that prevents recurrence]

---

## 3. Discrepancy Reporting

If a documented troubleshooting resolution fails during a future run:
```markdown
### ⚠️ Troubleshooting Resolution Failure
- **Documented Solution**: `docs/TROUBLESHOOTING.md`
- **Command Executed**: `...`
- **Unexpected Result**: `...`
- **Active Environment**: Windows / macOS / Linux, Runtime versions
```
