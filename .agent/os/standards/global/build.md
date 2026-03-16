# Build and Project Structure

### Component Directory Structure

```
component-name/
├── build.gradle
├── gradle.properties
├── component.xml
├── src/
│   ├── main/
│   │   └── groovy/
│   └── test/
│       └── groovy/
├── entity/
├── service/
├── screen/
├── data/
├── template/
├── lib/
└── conf/
```

### Multi-Module Project Structure

```
project-root/
├── build.gradle
├── settings.gradle
├── gradle.properties
├── runtime/
│   └── component/
│       ├── main-component/
│       │   └── build.gradle
│       └── shared-library/
│           └── build.gradle
└── framework/ -> [symlink to moqui-framework]
```

### Component Dependencies

**In `component.xml`:**
```xml
<component name="my-component" version="1.0.0">
    <depends-on name="moqui-framework"/>
    <depends-on name="mantle-usl"/>
    <depends-on name="other-component"/>
</component>
```

### Gradle Properties

**`gradle.properties`:**
```properties
# Build cache
org.gradle.caching=true

# Parallel builds
org.gradle.parallel=true

# Daemon
org.gradle.daemon=true
org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m

# Configuration cache (Gradle 6.6+)
org.gradle.unsafe.configuration-cache=true
```

### Basic Component build.gradle

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

### Version Management

**In `gradle.properties`:**
```properties
moquiVersion=3.0.0
spockVersion=2.1-groovy-3.0
```

**In `build.gradle`:**
```gradle
dependencies {
    testCompile "org.spockframework:spock-core:${spockVersion}"
}
```

### Test Configuration

```gradle
test {
    useJUnit()

    systemProperties = [
        'moqui.runtime': '../runtime',
        'moqui.conf': 'conf/MoquiTestConf.xml',
        'file.encoding': 'UTF-8'
    ]

    // Parallel execution
    maxParallelForks = Runtime.runtime.availableProcessors().intdiv(2) ?: 1

    // JVM settings
    minHeapSize = "256m"
    maxHeapSize = "2g"
    jvmArgs = ['-XX:+UseG1GC', '-XX:MaxGCPauseMillis=200']
}
```

### Data Loading Tasks

```gradle
task loadSeedData {
    group = 'data'
    description = 'Load seed data for component'

    doLast {
        javaexec {
            mainClass = 'MoquiStart'
            classpath = sourceSets.main.runtimeClasspath
            args = ['load_data', 'types=seed', 'component=component-name']
            systemProperty 'moqui.conf', 'conf/MoquiDevConf.xml'
        }
    }
}
```

### Credential Security

**Never hardcode credentials:**
```gradle
task deploy {
    doFirst {
        def username = System.getenv('DEPLOY_USERNAME')
        def password = System.getenv('DEPLOY_PASSWORD')

        if (!username || !password) {
            throw new GradleException('Credentials not found')
        }
    }
}
```

### JAR Packaging

```gradle
jar {
    from sourceSets.main.output
    include 'entity/**'
    include 'service/**'
    include 'screen/**'

    manifest {
        attributes(
            'Implementation-Title': project.name,
            'Implementation-Version': version,
            'Built-Date': new Date()
        )
    }
}
```

### Common Gradle Commands

| Command | Purpose |
|---------|---------|
| `./gradlew build` | Compile and test |
| `./gradlew test` | Run tests only |
| `./gradlew load` | Load all data |
| `./gradlew run` | Start Moqui |
| `./gradlew cleanAll` | Clean all artifacts |

### Build Best Practices

- Pin dependency versions
- Enable build cache
- Use parallel builds
- Configure appropriate JVM memory
- Separate test configurations
- Never hardcode credentials
