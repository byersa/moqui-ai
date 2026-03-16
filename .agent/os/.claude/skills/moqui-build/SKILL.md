---
name: moqui-build
description: |
  Moqui Framework build and deployment patterns including Gradle configuration, component structure, dependency management, and CI/CD integration.

  Use this skill when:
  - Configuring build.gradle for Moqui components
  - Setting up component directory structure
  - Managing dependencies and version control
  - Creating data loading tasks
  - Configuring test execution
  - Setting up CI/CD pipelines
---

# Moqui Build Patterns

## References

| Reference | Description |
|-----------|-------------|
| `../../references/build_patterns.md` | Gradle tasks, component structure, dependency management, CI/CD |

### Deep Reference (framework-guide.md)

For detailed patterns, search these sections in `runtime/component/moqui-agent-os/framework-guide.md`:
- **"## Build Script Integration Patterns"** - Framework build tasks, test configuration
- **"### Framework Build Tasks"** - Common Gradle tasks, data loading
- **"### Component Test Configuration"** - Test source sets, Spock configuration
- **"## Standard Component Structure"** - Core directories, source code organization
- **"## Common Development Commands"** - Building, running, data management commands

## Quick Reference

### Component build.gradle
```gradle
apply plugin: 'groovy'
version = '1.0.0'

repositories {
    flatDir name: 'localLib', dirs: projectDir.absolutePath + '/lib'
    mavenCentral()
}

dependencies {
    compile project(':framework')
    testCompile 'org.spockframework:spock-core:2.1-groovy-3.0'
}

test {
    systemProperty 'moqui.runtime', '../runtime'
    systemProperty 'moqui.conf', 'conf/MoquiDevConf.xml'
}
```

## Key Principles

1. **Component Structure**: entity/, service/, screen/, data/, template/ directories
2. **Dependencies**: Pin versions in gradle.properties for reproducibility
3. **Test Isolation**: Configure separate test source sets for unit vs integration tests
4. **No Hardcoded Credentials**: Use environment variables for deployment credentials