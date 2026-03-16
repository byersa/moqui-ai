# Demo Data Date Refresh Standard

## Purpose

Demo data with hardcoded dates becomes stale over time. This standard defines conventions for keeping time-sensitive demo data (meetings, deadlines, recent orders) current.

## Rules

### Data Type Convention
- Use `{project}-demo` type (not generic `demo`) for data files that contain relative date expressions
- The `{project}-demo` type ensures these files are processed by the project's date refresh service

### Template Naming
- Demo data template files use the same naming as regular data files
- Files with `@rel:` or `@epoch:` expressions are templates that require processing at load time

### Date Expression Syntax

**`@rel:` expressions** — dates relative to today:
```
@rel:0d          → today
@rel:-7d         → 7 days ago
@rel:+30d        → 30 days in the future
@rel:-2m         → 2 months ago
@rel:+1y         → 1 year from now
```

**`@epoch:` expressions** — fixed reference points:
```
@epoch:start     → project-defined start date
```

### When to Use
- **Use `@rel:` dates** for: Recent orders, upcoming meetings, active tasks, recent invoices
- **Use static dates** for: Historical reference data, seed data, configuration

## Implementation

Each project implements its own date refresh service. See your project's overlay documentation (`runtime/component/{main-component}/.agent-os/`) for:
- The specific refresh service name
- Available date expression formats
- Load-time processing details

## Framework Guide Reference

See `runtime/component/moqui-agent-os/framework-guide.md`:
- Section: **"### Demo Data Date Refresh"**
- Section: **"## Test Data Management and Data Types"**
