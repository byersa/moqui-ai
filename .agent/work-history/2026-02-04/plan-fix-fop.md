# FOP Dependency Fix

## Goal Description
Fix `java.lang.NoSuchMethodError` during FOP initialization caused by a dependency conflict.
`net.sf.barcode4j:barcode4j-fop-ext:2.1` transitively pulls in an ancient version of FOP (`org.apache.xmlgraphics:fop:0.95`), which conflicts with the modern FOP 2.9 libraries explicitly used by Moqui.

## Proposed Changes
### moqui-fop
#### [MODIFY] [build.gradle](file:///home/byersa/IdeaProjects/huddle-ai-project/runtime/component/moqui-fop/build.gradle)
- Update the `barcode4j-fop-ext` dependency block to exclude the legacy `fop` artifact.

## Verification Plan
### Automated Tests
- Run `./gradlew runtime:component:moqui-fop:dependencies` and verify `org.apache.xmlgraphics:fop:0.95` is gone.
- Since the user reported this happening on startup, we ideally would run a test that initializes FOP.
- I will verify the build is clean. The user will need to confirm runtime fix as I cannot easily restart their external instance, but I can assume if the dependency is gone, the classloader ambiguity is resolved.
