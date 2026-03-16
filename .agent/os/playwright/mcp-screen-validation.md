# Playwright MCP Screen Validation Guide

> Framework-level guide for validating Moqui screens using Playwright MCP
> Created: 2025-12-27
> Status: Active

## Overview

This guide provides patterns for using Playwright MCP to validate Moqui Framework screens during development. Screen validation helps identify:

- Missing backend services before they cause production issues
- Role-based access control correctness
- Navigation and workflow completeness
- UI component rendering issues

## When to Use Screen Validation

**Use Playwright MCP validation:**
- After implementing new screens
- After adding new transitions/actions to screens
- When refactoring existing screens
- Before demo or release milestones
- To verify role-based access control

**Validation vs E2E Testing:**
| Aspect | Screen Validation | E2E Testing |
|--------|------------------|-------------|
| Purpose | Verify screens work | Verify workflows work |
| Scope | Individual screens | Complete user journeys |
| Speed | Quick exploration | Automated regression |
| Tooling | Playwright MCP (interactive) | Playwright scripts |

## Standard Validation Workflow

### Phase 1: Application Access

```
1. Navigate to http://localhost:8080
2. Capture login screen snapshot
3. Identify available quick-login users
4. Login with appropriate test user
5. Verify main dashboard loads
```

**Key Tools:**
- `browser_navigate` - Navigate to URL
- `browser_snapshot` - Capture accessibility tree (preferred over screenshots)
- `browser_click` - Click elements by ref

### Phase 2: Navigation Validation

```
1. Take snapshot of main navigation
2. Navigate to target module
3. Verify menu structure
4. Document available menu options
5. Test navigation to sub-screens
```

**Pattern for organization-based apps:**
```
If module is not visible:
1. Click organization selector (edit_location button)
2. Select appropriate organization/tenant
3. Verify module appears in app list
4. Navigate to module
```

### Phase 3: Screen Component Testing

For each screen:

```
1. Navigate to screen
2. Take snapshot to capture all components
3. Identify action buttons and forms
4. Test each action:
   - Click button/submit form
   - Observe response (success or error)
   - Document service calls that fail
5. Test with different user roles
```

### Phase 4: Role-Based Access Validation

```
1. Logout (navigate to /Login/logout)
2. Login as different user role
3. Navigate to same screens
4. Document differences:
   - Visible/hidden buttons
   - Editable/read-only fields
   - Available actions
5. Verify access control is correct
```

## Common Screen Components

### Moqui Screen Elements

| Component | Snapshot Representation | Testing Approach |
|-----------|------------------------|------------------|
| Form fields | `textbox`, `combobox` | Fill and submit |
| Buttons | `button` with label | Click and observe |
| Links | `link` with URL | Click or verify href |
| Tabs | `tab` with selected state | Click to switch |
| Dialogs | `dialog` element | Test open/close/submit |
| Tables | `row`, `columnheader` | Check data display |
| Cards | `generic` with headings | Expand/collapse |

### Error Detection

**Service Not Found (500):**
```
alert:
  generic:
    - img: warning
    - generic: "{'message':'Service not found with name X.Y.Z','errorName':'Internal Server Error','error':500}"
```

**Permission Denied (403):**
```
alert:
  generic:
    - img: warning
    - generic: "View not permitted for path [X, Y, Z]"
```

## Validation Checklist Template

Use this checklist for each screen validation:

```markdown
## Screen: [Screen Name]
**Path:** /qapps/Module/Screen
**Tested by:** [User Role]

### Components Present
- [ ] Header/title displays correctly
- [ ] Navigation breadcrumb works
- [ ] All form fields render
- [ ] All buttons are clickable
- [ ] Required data loads

### Actions Tested
| Action | Button/Link | Result | Service |
|--------|-------------|--------|---------|
| Create | "Add Item" | [ ] Works / [ ] Error | service.name |
| Update | "Save" | [ ] Works / [ ] Error | service.name |
| Delete | "Remove" | [ ] Works / [ ] Error | service.name |
| Export | "Export" | [ ] Works / [ ] Error | service.name |

### Role-Based Access
| Role | Can View | Can Edit | Can Delete | Notes |
|------|----------|----------|------------|-------|
| Admin | Yes | Yes | Yes | Full access |
| Secretary | Yes | Yes | No | Edit only |
| Director | Yes | No | No | Read only |

### Issues Found
1. [Issue description with service name if applicable]
2. [Issue description]

### Missing Services
- [ ] `service.path.ServiceName.action#Entity`
```

## Example: Agenda Screen Validation

### Test Users Identified

| User | Role | Primary Access |
|------|------|----------------|
| Martín Burgos | Secretary | Agenda management, full editing |
| Fernando Torres | Director | Topic proposals, read-only |
| Sofía Cifuentes | Admin | Organization administration |

### Screen: Agenda de Instancia de Participación

**Path:** `/qapps/Governance/Engagement/EngagementAgenda?engagementId=XXX`

**Components Validated:**
- Contenido de la Agenda (title, content, notes)
- Gestión de Ítems de la Agenda
- Resumen de Duración
- Materias Disponibles
- Documentos de Apoyo

**Actions Tested:**

| Action | Status | Service |
|--------|--------|---------|
| Cargar Plantilla | Works (no templates) | N/A |
| Exportar Agenda | 500 Error | `GovernanceEngagementServices.export#EngagementAgenda` |
| Agregar Ítem | 500 Error | `GovernanceEngagementServices.add#AgendaItem` |
| Guardar Contenido | 500 Error | `GovernanceEngagementServices.update#EngagementAgenda` |
| Adjuntar Documento | Not tested | `GovernanceEngagementServices.attach#AgendaDocument` |

**Role Differences:**
- Secretary: Full editing, phase transitions, agenda management
- Director: Read-only with topic proposal form

## Development Integration

### Recommended Development Flow

```
1. Design screens (XML)
2. Define services (XML interface)
3. Run screen validation with Playwright MCP
4. Identify missing service implementations
5. Implement services
6. Re-validate screens
7. Create E2E tests for critical workflows
```

### Adding Validation to Sprint Workflow

**Definition of Done for Screens:**
- [ ] Screen renders without errors
- [ ] All actions tested via Playwright MCP
- [ ] All services implemented and working
- [ ] Role-based access verified
- [ ] Localization verified (Spanish)

## Quick Reference Commands

### Starting Validation Session

```
Navigate to http://localhost:8080
Login with test user via quick-login button
```

### Switching Organizations

```
Click organization selector (edit_location button)
Select target organization from list
```

### Testing Screen Actions

```
Navigate to screen
Take snapshot
Click action button
Check for error alerts
Document results
```

### Switching Users

```
Navigate to /Login/logout
Confirm logout dialog
Click different user's login button
```

## Troubleshooting

### Element Outside Viewport

If click fails with "element is outside of the viewport":
1. Use `browser_press_key` with "Home" to scroll to top
2. Take new snapshot
3. Retry click with new ref

### Dialog Handling

If action opens confirm dialog:
```
browser_handle_dialog(accept=true)
```

### Waiting for Load

If page is still loading:
```
browser_wait_for(time=2)  # Wait 2 seconds
browser_snapshot()  # Take fresh snapshot
```

## Related Documentation

- **MCP Setup:** `playwright-mcp-setup.md`
- **E2E Infrastructure:** `playwright-e2e-infrastructure.md`
- **Testing Guide:** `testing-guide.md`
- **Screen Patterns:** `moqui-screen-patterns.md`

---

*This guide captures patterns from screen validation sessions. Update as new patterns emerge.*
