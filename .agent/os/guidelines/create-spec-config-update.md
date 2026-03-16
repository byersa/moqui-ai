# Create-Spec Configuration Update

## Overview

The `/create-spec` command has been updated to automatically detect project structure and store specifications in the appropriate location based on the project type.

## Changes Made

### 1. Project Detection Logic

The command now includes automatic project detection in **Step 5: Spec Folder Creation**:

- **Moqui Component Projects**: If `runtime/component/` directory is detected, specs are stored in:
  ```
  runtime/component/{main-component}/.agent-os/specs/YYYY-MM-DD-spec-name/
  ```

- **Standard Projects**: If no Moqui component structure is found, specs use the original location:
  ```
  .agent-os/specs/YYYY-MM-DD-spec-name/
  ```

### 2. Auto-Detection Algorithm

The system will:
1. Check if `runtime/component/` directory exists in current or parent directories
2. If found, identify the main component (usually the one with most entities/services)  
3. Default to first component found if unable to determine main component
4. Use component-specific `.agent-os/specs/` directory for spec storage

### 3. Updated Configuration Files

**Files Modified:**
- `runtime/component/moqui-agent-os/instructions/core/create-spec.md`
- `/Users/jhp/.agent-os/instructions/create-spec.md`

**Changes Include:**
- Dynamic project detection logic in Step 5
- Updated file path references throughout all steps
- Template placeholders using `{detected-spec-path}` and `{relative-spec-path}`
- Consistent cross-referencing between all spec files

### 4. Prohibited Location

**NEVER** create specs in `runtime/component/moqui-agent-os/specs/`. The shared `agent-os` component is project-neutral and must not contain feature specifications, shaping documents, or implementation plans for any specific project. If specs are found there, use the `prompts/relocate-product-files.md` prompt to move them.

### 5. Benefits

- **Project-Appropriate Storage**: Specs stored in component directories for Moqui projects
- **Framework Separation**: Framework-level and project-specific content properly separated
- **Automatic Detection**: No manual configuration needed - detects project structure automatically
- **Backward Compatibility**: Standard projects continue to use `.agent-os/specs/` as before

## Testing

For this acme-erp project:
- **Detected Structure**: Moqui component project
- **Main Component**: `acme-erp`
- **Spec Location**: `runtime/component/acme-erp/.agent-os/specs/`

## Verification

The configuration correctly identifies this as a Moqui component project and will store future specs in:
```
/Users/jhp/Devel/moqui/acme-erp/runtime/component/acme-erp/.agent-os/specs/YYYY-MM-DD-spec-name/
```

This ensures project-specific specifications remain within the component directory while maintaining proper separation from framework-level configuration.