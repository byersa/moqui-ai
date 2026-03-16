# Security Review Checklist

Analytical checklist for systematic security review of Moqui Framework components. Organized by 8 security dimensions with severity levels and framework behavior notes.

**Purpose**: This is a *review* checklist — use it to find gaps. For remediation patterns, see `security.md`. For EntityFilter implementation details, see `entity-filters.md`.

## Severity Guide

| Level | Meaning | Action |
|-------|---------|--------|
| **CRITICAL** | Data exposure, authorization bypass, or privilege escalation | Must fix before deployment |
| **HIGH** | Missing security layer that could be exploited with additional knowledge | Fix in current iteration |
| **MEDIUM** | Inconsistency or gap that degrades defense-in-depth | Fix before next release |
| **LOW** | Best practice deviation with minimal direct risk | Track for improvement |

---

## Dimension 1: Row-Level Security (EntityFilters) — CRITICAL

EntityFilters enforce multi-tenant and organization-scoped data isolation. Gaps here mean users see data belonging to other organizations.

| # | Check | Severity | Where to Look |
|---|-------|----------|----------------|
| 1.1 | Every new entity storing org-scoped data has an EntityFilter definition | CRITICAL | `data/` files for `EntityFilterSet`/`EntityFilter` records |
| 1.2 | Filter uses fail-safe pattern: `filterOrgIds ? filterOrgIds : ['-NO-MATCH-']` | CRITICAL | `filterMap` attribute in EntityFilter definitions |
| 1.3 | Filter context variables are populated before entity queries | CRITICAL | `setup#FilterContext` calls in services and screen `always-actions` |
| 1.4 | View-entity alias names match the filtered field name exactly | CRITICAL | `<alias>` elements in view-entity definitions |
| 1.5 | View-entity member entities with EntityFilters are not trimmed by unused aliases | HIGH | Check if filtered member's alias appears in `select-field`, `econdition`, or `order-by` |
| 1.6 | `UserGroupEntityFilterSet` records exist for all relevant user groups | HIGH | Security seed data files |
| 1.7 | `applyCachedFinds="true"` set on EntityFilterSets that apply to cached entities | MEDIUM | EntityFilterSet definitions |

**Framework behavior**: EntityFilters are applied automatically by `ArtifactExecutionFacadeImpl` when a user's group has a `UserGroupEntityFilterSet` record. For view-entities, the framework builds an `entityAliasUsedSet` from SELECT/WHERE/ORDER BY — member entities not in this set are trimmed and their filters skipped silently.

**Cross-reference**: See `entity-filters.md` for fail-safe pattern details and view-entity alias matching rules.

---

## Dimension 2: Artifact Authorization — CRITICAL

ArtifactAuthz controls which user groups can access screens, services, and entities. The framework automatically hides menu items and links when users lack authorization.

| # | Check | Severity | Where to Look |
|---|-------|----------|----------------|
| 2.1 | New screens have `ArtifactGroup` + `ArtifactGroupMember` records | CRITICAL | Security seed data files |
| 2.2 | `ArtifactAuthz` records grant appropriate access per user group | CRITICAL | Security seed data files |
| 2.3 | `inheritAuthz="Y"` set on `ArtifactGroupMember` for parent screens | HIGH | `ArtifactGroupMember` records |
| 2.4 | No deprecated `userGroupId` on `SubscreensItem` records | HIGH | Screen mounting data files |
| 2.5 | REST API endpoints have corresponding `ArtifactGroup`/`ArtifactAuthz` records | CRITICAL | REST resource definitions + security data |
| 2.6 | New services that should be restricted have `ArtifactAuthz` records | HIGH | Service definitions + security data |
| 2.7 | One `SubscreensItem` per screen (not duplicated per user group) | MEDIUM | Screen mounting data |

**Framework behavior**: `ScreenUrlInfo.isPermitted()` checks ArtifactAuthz for the target screen. If the user lacks permission, the link/button is automatically hidden from the UI — no code needed. This is the preferred approach over manual permission checks.

**Cross-reference**: See `security.md` § "Screen Access Control (SubscreensItem + ArtifactAuthz)" for the full pattern.

---

## Dimension 3: Service Authentication — HIGH

Controls whether services require a logged-in user and what authorization level applies.

| # | Check | Severity | Where to Look |
|---|-------|----------|----------------|
| 3.1 | Services default to `authenticate="true"` unless explicitly public | HIGH | `<service>` definitions |
| 3.2 | `authenticate="anonymous-all"` is justified (public endpoint, system job, filter setup) | HIGH | Service definitions with anonymous auth |
| 3.3 | `disable-authz="true"` on `service-call` is justified and documented | HIGH | Service implementations calling other services |
| 3.4 | System/internal services use `authenticate="anonymous-all"` only when called from trusted contexts | MEDIUM | Service call chains |

**Framework behavior**: `authenticate="true"` (default) requires an active user session. `anonymous-all` bypasses both authentication and authorization checks. `disable-authz="true"` on a `service-call` bypasses artifact authorization for that specific call only.

---

## Dimension 4: Permission Consistency — MEDIUM

`ec.user.hasPermission()` checks do NOT auto-hide UI elements — the button remains visible but fails at the service level, creating a poor user experience.

| # | Check | Severity | Where to Look |
|---|-------|----------|----------------|
| 4.1 | Every `hasPermission()` call has a corresponding ArtifactAuthz that hides the UI element | MEDIUM | Service actions using `hasPermission()` + screen transitions |
| 4.2 | `UserPermission` records exist for all permission IDs used in code | MEDIUM | Security seed data |
| 4.3 | `UserGroupPermission` records link permissions to appropriate user groups | MEDIUM | Security seed data |
| 4.4 | Permission naming follows a consistent convention (e.g., `ENTITY_ACTION`) | LOW | Permission definitions |

**Framework behavior**: `ec.user.hasPermission('PERM_ID')` checks `UserGroupPermission` for the user's groups. Unlike ArtifactAuthz, this does NOT affect UI visibility — the screen element remains visible and the user sees an error only when they click. Prefer ArtifactAuthz for access control; reserve `hasPermission()` for fine-grained business logic branching.

---

## Dimension 5: REST API Security — CRITICAL

REST endpoints are attack surface entry points requiring explicit security configuration.

| # | Check | Severity | Where to Look |
|---|-------|----------|----------------|
| 5.1 | `require-authentication="true"` on all non-public resources | CRITICAL | `*.rest.xml` files |
| 5.2 | Filter context is set up before entity queries in REST service implementations | CRITICAL | Services called by REST methods |
| 5.3 | POST/PUT/DELETE endpoints are not accessible without authentication | CRITICAL | REST resource method definitions |
| 5.4 | `require-authentication="anonymous-all"` is justified (webhook, public API) | HIGH | REST resource definitions |
| 5.5 | REST services validate and sanitize all input parameters | HIGH | Service `in-parameters` |
| 5.6 | API keys or tokens are used for machine-to-machine authentication | MEDIUM | REST authentication setup |

**Framework behavior**: `require-authentication` on a REST resource only controls whether the framework requires a session/key. It does NOT bypass CSRF checks for POST requests — CSRF is bypassed only when the request is authenticated via Basic Auth or API key (which sets `moqui.request.authenticated=true`). Screen-based POST requests still go through CSRF validation.

**Cross-reference**: See `entity-filters.md` § "REST API Filter Context" for the mandatory filter setup pattern.

---

## Dimension 6: Input Validation — MEDIUM

Prevents injection attacks and ensures data integrity at system boundaries.

| # | Check | Severity | Where to Look |
|---|-------|----------|----------------|
| 6.1 | Service parameters have appropriate `type` attributes | MEDIUM | Service `in-parameters` |
| 6.2 | Text parameters that should not contain HTML use `allow-html="none"` | MEDIUM | Service `in-parameters` |
| 6.3 | Parameters accepting HTML use `allow-html="safe"` (not `allow-html="any"`) | HIGH | Service `in-parameters` |
| 6.4 | No raw SQL constructed with user input (string concatenation) | CRITICAL | Groovy scripts, service implementations |
| 6.5 | Entity-find conditions use parameterized values (framework handles escaping) | LOW | Entity-find XML — usually safe by default |
| 6.6 | Regular expressions used for format validation where appropriate | LOW | `<matches>` validators on parameters |

**Framework behavior**: The Moqui entity facade automatically parameterizes all entity-find conditions, preventing SQL injection. Risk exists only in raw SQL via `entity-find-sql` or Groovy `sql.execute()` calls.

---

## Dimension 7: Audit & Sensitive Data — MEDIUM/HIGH

Ensures traceability of changes and protection of personally identifiable information (PII).

| # | Check | Severity | Where to Look |
|---|-------|----------|----------------|
| 7.1 | Sensitive entity fields have `enable-audit-log="true"` | MEDIUM | Entity definitions — status fields, amounts, key dates |
| 7.2 | PII fields use `encrypt="true"` | HIGH | Entity definitions — SSN, credit card, tax ID fields |
| 7.3 | Audit log is not disabled on entities that track financial or legal data | HIGH | Entity definitions — check for `enable-audit-log="false"` |
| 7.4 | Sensitive data is not logged in plain text | MEDIUM | Service implementations — `ec.logger` calls |
| 7.5 | API responses do not expose sensitive internal fields | MEDIUM | REST service `out-parameters`, auto-parameters usage |

**Framework behavior**: `enable-audit-log="true"` on a field causes the framework to write to `moqui.entity.EntityAuditLog` on every update. `encrypt="true"` uses the framework's encryption key to encrypt the field value at rest in the database.

---

## Dimension 8: Screen Security — HIGH

Ensures screens enforce authorization and don't leak sensitive data through URLs or UI.

| # | Check | Severity | Where to Look |
|---|-------|----------|----------------|
| 8.1 | Transitions that modify data use `<service-call>` (not inline actions) | HIGH | Screen transition definitions |
| 8.2 | Screen hierarchy has filter context setup in root `always-actions` | HIGH | Root/app screen definitions |
| 8.3 | Sensitive data is not passed in URL query parameters | MEDIUM | Screen transitions, link URLs |
| 8.4 | Form submissions use POST for data-modifying operations | MEDIUM | Form definitions and transitions |
| 8.5 | `container-dialog` content is not pre-loaded with sensitive data before user action | LOW | Screen sections inside container-dialog |
| 8.6 | Confirmation dialogs protect destructive operations (delete, cancel, void) | MEDIUM | Screen transitions for destructive actions |

**Framework behavior**: Screen transitions inherit the authorization context of their parent screen via `inheritAuthz="Y"`. The framework checks ArtifactAuthz before rendering transition links — unauthorized transitions are hidden automatically.

---

## Report Template

Use this structure when reporting security review findings:

```markdown
# Security Review Report

**Date**: YYYY-MM-DD
**Scope**: [description of reviewed changes/components]
**Reviewer**: [name/tool]

## Summary

| Severity | Count |
|----------|-------|
| CRITICAL | N |
| HIGH | N |
| MEDIUM | N |
| LOW | N |

## Findings

### [SEVERITY] [Dimension #.#] — [Brief title]

- **Location**: `path/to/file:line`
- **Dimension**: [Dimension name]
- **Issue**: [What's wrong]
- **Risk**: [What could happen]
- **Fix**: [Specific remediation]
- **Standard**: [Reference to security.md / entity-filters.md section]

## Dimension Status

| Dimension | Status | Notes |
|-----------|--------|-------|
| 1. Row-Level Security | PASS/FAIL/N/A | |
| 2. Artifact Authorization | PASS/FAIL/N/A | |
| 3. Service Authentication | PASS/FAIL/N/A | |
| 4. Permission Consistency | PASS/FAIL/N/A | |
| 5. REST API Security | PASS/FAIL/N/A | |
| 6. Input Validation | PASS/FAIL/N/A | |
| 7. Audit & Sensitive Data | PASS/FAIL/N/A | |
| 8. Screen Security | PASS/FAIL/N/A | |

## Recommendations

1. [Prioritized list of actions]
```
