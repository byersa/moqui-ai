# FOP Dependency Fix Walkthrough

## Changes Made
- Modified `runtime/component/moqui-fop/build.gradle` to exclude `org.apache.xmlgraphics:fop` from the `barcode4j-fop-ext` dependency.
- **Manual Cleanup**: Removed `fop-0.95.jar` from `runtime/component/moqui-fop/lib/`. This file persisted from previous builds and caused the runtime conflict.

## Verification
### Automated Checks
1. Ran `./gradlew runtime:component:moqui-fop:dependencies` to confirm `fop:0.95` is excluded from the graph.
2. Verified `runtime/component/moqui-fop/lib/` no longer contains `fop-0.95.jar`.

### Manual Verification
The user must restart their Moqui instance. The `NoSuchMethodError` should be resolved as the classloader will now only find the modern `FopFactory` from `fop-core-2.9.jar`.
