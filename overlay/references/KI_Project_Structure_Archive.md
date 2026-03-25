# KI: Project Structure - Archive Components

## Context
When developing complex Moqui components like `aitree`, there is often a need for "scratchpad" or experimental sub-components that should not be part of the official build or version control.

## Strategy
We have established an `archive-components/` directory within the component root.

### Details:
- **Location**: `runtime/archive-components/`
- **Purpose**: For extemporaneous, experimental, or legacy components.
- **Git Policy**: Added to project root `.gitignore` to prevent cluttering the repository.
- **Onboarding**: New developers are informed via this KI that this is a safe place for local experiments.

## Implementation
1. Created `runtime/archive-components/`
2. Added `runtime/archive-components/` to the project root `.gitignore`.
