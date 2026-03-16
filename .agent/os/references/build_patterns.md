# Moqui Build Patterns

**Standards Reference**: For declarative conventions, see:
- `standards/global/build.md` - Directory structure, Gradle settings
- `standards/global/tech-stack.md` - Framework versions and dependencies

---

## Project Structure

### Single Component
```
component-name/
├── build.gradle
├── gradle.properties
├── src/
│   ├── main/
│   │   ├── groovy/
│   │   └── resources/
│   └── test/
│       ├── groovy/
│       └── resources/
├── entity/
├── service/
├── screen/
├── data/
├── template/
├── lib/
└── conf/
```

### Multi-Module Pattern
```
project-root/
├── build.gradle
├── settings.gradle
├── gradle.properties
├── runtime/
│   └── component/
│       ├── main-component/
│       │   └── build.gradle
│       ├── shared-library/
│       │   └── build.gradle
│       └── integration-tests/
│           └── build.gradle
└── framework/ -> [symlink to moqui-framework]
```

---

## Basic Component build.gradle

```gradle
apply plugin: 'groovy'

version = '1.0.0'

repositories {
    flatDir name: 'localLib', dirs: projectDir.absolutePath + '/lib'
    mavenCentral()
}

dependencies {
    compile project(':framework')

    // Component-specific dependencies
    // compile 'org.example:library:1.0.0'

    testCompile 'junit:junit:4.12'
    testCompile 'org.spockframework:spock-core:1.3-groovy-2.5'
}

jar {
    destinationDir = file(projectDir.absolutePath + '/lib')
    archiveName = 'component.jar'
}

test {
    useJUnit()

    systemProperty 'moqui.runtime', '../runtime'
    systemProperty 'moqui.conf', 'conf/MoquiDevConf.xml'

    testLogging {
        events "passed", "skipped", "failed"
        exceptionFormat "full"
    }
}
```

---

## Dependency Management

### Version Management
```gradle
// gradle.properties
moquiVersion=3.0.0
spockVersion=2.1-groovy-3.0
httpClientVersion=4.5.13

// build.gradle
ext {
    moquiVersion = project.property('moquiVersion')
    spockVersion = project.property('spockVersion')
}

dependencies {
    compile project(':framework')
    compile "org.apache.httpcomponents:httpclient:${httpClientVersion}"
    testCompile "org.spockframework:spock-core:${spockVersion}"
}
```

### Repository Configuration
```gradle
repositories {
    // Local flat directory for component libs
    flatDir name: 'localLib', dirs: projectDir.absolutePath + '/lib'

    // Maven Central
    mavenCentral()

    // Custom repository with credentials
    maven {
        name 'CompanyRepository'
        url 'https://repository.company.com/maven2'
        credentials {
            username = project.findProperty('repoUsername')
            password = project.findProperty('repoPassword')
        }
    }
}
```

---

## CRITICAL: Test Execution Command

**ALWAYS run tests with `cleanDb` and `load` commands from the root project directory.**

```bash
# Standard pattern - always use this format for ANY component
./gradlew cleanDb load runtime:component:{component-name}:test -Ptypes=seed,seed-initial,{l10n}-install,{project}-test,{project}-demo,{component}-test,{component-name}-test
```

**Note:** The `{component-name}-test` type is always included by convention. If it doesn't exist for a component, it is safely ignored without error.

**Why This is Critical:**
- `cleanDb` ensures a clean database state for each test run
- `load` with `-Ptypes=` loads the specific seed and test data needed
- Running tests without this pattern causes unpredictable failures from stale data
- **NEVER** run just `./gradlew :runtime:component:{name}:test` without cleanDb and load

**Common Data Types:**
- `seed,seed-initial` - Framework core seed data (always required first)
- `{l10n}-install` - Project-specific localization/installation data (replaces `install` for customized projects)
- `{project}-test` - Common test user accounts and configuration
- `{project}-demo` - Demo data for development testing
- `{component}-test` - Component-specific test data
- `{component-name}-test` - Component-specific test fixtures (included by convention)

📄 **Full Reference**: See `testing-guide.md` for complete data type documentation.

---

## Testing Configuration

### Test Categories
```gradle
test {
    useJUnit {
        includeCategories 'org.moqui.test.UnitTest'
        excludeCategories 'org.moqui.test.SlowTest'
    }

    systemProperties = [
        'moqui.runtime': '../runtime',
        'moqui.conf': 'conf/MoquiTestConf.xml',
        'file.encoding': 'UTF-8'
    ]

    testLogging {
        events "passed", "skipped", "failed", "standardOut", "standardError"
        exceptionFormat "full"
    }

    // Parallel test execution
    maxParallelForks = Runtime.runtime.availableProcessors().intdiv(2) ?: 1

    // JVM configuration
    minHeapSize = "256m"
    maxHeapSize = "2g"
    jvmArgs = ['-XX:+UseG1GC', '-XX:MaxGCPauseMillis=200']
}
```

### Integration Test Task
```gradle
task integrationTest(type: Test) {
    description = 'Run integration tests'
    group = 'verification'

    testClassesDirs = sourceSets.integrationTest.output.classesDirs
    classpath = sourceSets.integrationTest.runtimeClasspath

    systemProperties = [
        'moqui.runtime': '../runtime',
        'moqui.conf': 'conf/MoquiIntegrationTestConf.xml',
        'integration.test.mode': 'true'
    ]

    shouldRunAfter test
}
```

---

## Data Loading Tasks

```gradle
task loadSeedData {
    group = 'data'
    description = 'Load seed data for component'

    doLast {
        javaexec {
            mainClass = 'MoquiStart'
            classpath = sourceSets.main.runtimeClasspath
            args = ['load_data', 'types=seed', 'component=component-name']
            systemProperty 'moqui.conf', "conf/MoquiDevConf.xml"
            systemProperty 'moqui.runtime', '../runtime'
        }
    }
}

task loadDemoData {
    group = 'data'
    description = 'Load demo data for component'
    dependsOn loadSeedData

    doLast {
        javaexec {
            mainClass = 'MoquiStart'
            classpath = sourceSets.main.runtimeClasspath
            args = ['load_data', 'types=demo', 'component=component-name']
            systemProperty 'moqui.conf', "conf/MoquiDevConf.xml"
            systemProperty 'moqui.runtime', '../runtime'
        }
    }
}
```

---

## Build Performance Optimization

### gradle.properties Settings
```properties
# Enable build cache
org.gradle.caching=true

# Enable parallel builds
org.gradle.parallel=true

# Configure daemon
org.gradle.daemon=true
org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m

# Enable configuration cache (Gradle 6.6+)
org.gradle.unsafe.configuration-cache=true
```

---

## Packaging Patterns

### Component JAR
```gradle
jar {
    from sourceSets.main.output
    include 'entity/**'
    include 'service/**'
    include 'screen/**'

    destinationDir = file('lib')
    archiveName = "${project.name}.jar"

    manifest {
        attributes(
            'Implementation-Title': project.name,
            'Implementation-Version': version,
            'Built-By': System.getProperty('user.name'),
            'Built-Date': new Date(),
            'Built-JDK': System.getProperty('java.version')
        )
    }
}
```

---

## Security Best Practices

### Credential Management
```gradle
// Never hardcode credentials - use environment variables
task deployToProduction {
    doFirst {
        def username = System.getenv('DEPLOY_USERNAME')
        def password = System.getenv('DEPLOY_PASSWORD')

        if (!username || !password) {
            throw new GradleException(
                'Deployment credentials not found in environment variables'
            )
        }
    }
}
```

### Dependency Version Enforcement
```gradle
configurations.all {
    resolutionStrategy {
        eachDependency { DependencyResolveDetails details ->
            // Enforce specific versions for security
            if (details.requested.group == 'log4j') {
                details.useVersion '2.17.1'
                details.because 'Security vulnerability CVE-2021-44228'
            }
        }
    }
}
```

---

## Quality Checklist

**Build Configuration:**
- [ ] Build is reproducible across environments
- [ ] Dependencies pinned to specific versions
- [ ] Build time optimized (caching, parallel)
- [ ] Error messages are clear and actionable

**Testing:**
- [ ] All tests pass consistently
- [ ] Test coverage configured
- [ ] Integration tests separated from unit tests

**Deployment:**
- [ ] Staging deployment works
- [ ] Production rollback procedure tested
- [ ] Health checks configured
- [ ] Credentials not hardcoded

**Documentation:**
- [ ] Build tasks documented
- [ ] Required properties listed
- [ ] CI/CD integration documented