# Tech Stack

This standard documents the Moqui Framework tech stack for both version 3.x and 4.x. Projects should specify which version they use in their project-specific `.agent-os/product/tech-stack.md`.

---

## Version Comparison Summary

| Component | Moqui 3.x | Moqui 4.x |
|-----------|-----------|-----------|
| **Java** | Java 11+ | Java 25 (recommended) |
| **Groovy** | Groovy 3.x | Groovy 5.x |
| **Gradle** | Gradle 7.x | Gradle 9.2+ |
| **Jetty** | Jetty 10.x | Jetty 12.1 |
| **Servlet API** | javax.servlet (EE 8) | jakarta.servlet (EE 11) |
| **Transaction API** | javax.transaction | jakarta.transaction |
| **OpenSearch** | 2.6.0 | 3.4.0 |
| **PostgreSQL** | 14.x+ | 18.1 |
| **MySQL** | 8.x | 9.5 |
| **Spock** | Spock 2.1 | Spock 2.4+ |

---

## Moqui Framework 3.x (Current Stable)

### Framework & Runtime
- **Application Framework**: Moqui Framework 3.x
- **Language/Runtime**: Groovy 3.x on JVM (Java 11+)
- **Build Tool**: Gradle 7.x
- **Package Manager**: Gradle dependencies, Maven Central
- **Namespace**: `javax.*` packages

### Backend
- **Service Layer**: Moqui XML DSL Services with Groovy implementations
- **ORM**: Moqui Entity Engine (entity-find, entity-one, auto-services)
- **API Style**: REST (*.rest.xml) and XML-SOAP (remote-xml-soap)
- **Authentication**: UserLoginKey, Basic Auth, OAuth integration
- **Transaction Manager**: Bitronix (javax.transaction)

### Frontend
- **UI Framework**: Moqui Screen XML with FreeMarker templates
- **CSS Framework**: Bootstrap 4.x (via vuet theme)
- **JavaScript**: Vue.js 2.x (via vuet theme)
- **UI Components**: Moqui form widgets, container components

### Database & Storage
- **Development DB**: H2 (embedded)
- **Production DB**: PostgreSQL 14+, MySQL 8.x, Oracle
- **Search Engine**: OpenSearch 2.6.0 / Elasticsearch 7.10.2
- **Caching**: Moqui built-in caching (Hazelcast for clustering)

### Testing & Quality
- **Unit/Integration Testing**: Spock Framework 2.1 (Groovy)
- **E2E Testing**: Playwright (optional)
- **Code Quality**: Moqui XML validation, Groovy compilation
- **JUnit**: JUnit 5 (Jupiter) via JUnit Platform

### Deployment & Infrastructure
- **Application Server**: Embedded Jetty 10.x (via MoquiStart)
- **Containerization**: Docker (optional)
- **CI/CD**: GitHub Actions, GitLab CI, Jenkins
- **Monitoring**: Log4j2, custom monitoring endpoints

---

## Moqui Framework 4.x (Next Generation)

### Breaking Changes from 3.x

**Java 25 Recommended (Java 21 minimum)**
- All custom code must be validated against Java 25 (or Java 21 minimum)
- Removed deprecated `finalize()` methods
- Uses `Thread.threadId()` instead of `Thread.getId()` (supports virtual threads)

**Groovy 5 Upgrade**
- Stricter `@CompileStatic` behavior
- Groovysh removed - terminal interface rewritten
- Some inner class field access patterns may need adjustment

**javax → jakarta Migration**
- All imports must change from `javax.*` to `jakarta.*`
- Affects: servlet, transaction, mail, websocket, activation, xml.bind
- Custom code using these packages requires import updates

**Bitronix Transaction Manager**
- New fork: https://github.com/moqui/bitronix (v4.0.0-BETA1)
- Legacy Bitronix artifacts no longer supported
- `javax.transaction.*` → `jakarta.transaction.*`

**Gradle 9.2**
- Stricter validation and immutability rules
- `exec {}` blocks replaced with Groovy `execute()` usage
- Property assignments require `=` (e.g., `maxParallelForks = 1`)

### Framework & Runtime
- **Application Framework**: Moqui Framework 4.x
- **Language/Runtime**: Groovy 5.x on JVM (Java 25 recommended, Java 21 minimum)
- **Build Tool**: Gradle 9.2+
- **Package Manager**: Gradle dependencies, Maven Central, JitPack
- **Namespace**: `jakarta.*` packages

### Backend
- **Service Layer**: Moqui XML DSL Services with Groovy implementations
- **ORM**: Moqui Entity Engine (entity-find, entity-one, auto-services)
- **API Style**: REST (*.rest.xml) and XML-SOAP (remote-xml-soap)
- **Authentication**: UserLoginKey, Basic Auth, OAuth integration
- **Transaction Manager**: Bitronix fork (jakarta.transaction)
- **Servlet API**: Jakarta Servlet 6.1 (EE 11)

### Frontend
- **UI Framework**: Moqui Screen XML with FreeMarker templates
- **CSS Framework**: Bootstrap 4.x (via vuet theme)
- **JavaScript**: Vue.js 2.x (via vuet theme)
- **UI Components**: Moqui form widgets, container components

### Database & Storage
- **Development DB**: H2 (embedded)
- **Production DB**: PostgreSQL 18.1, MySQL 9.5, Oracle
- **Search Engine**: OpenSearch 3.4.0 (recommended over Elasticsearch)
- **Caching**: Moqui built-in caching (Hazelcast for clustering)

### Testing & Quality
- **Unit/Integration Testing**: Spock Framework 2.4+ (Groovy)
- **E2E Testing**: Playwright (optional)
- **Code Quality**: Moqui XML validation, Groovy compilation
- **JUnit**: JUnit 5 (Jupiter) via JUnit Platform

### Deployment & Infrastructure
- **Application Server**: Embedded Jetty 12.1 (via MoquiStart)
- **Containerization**: Docker with updated base images
- **Docker Base Image**: eclipse-temurin:25 (or eclipse-temurin:21)
- **CI/CD**: GitHub Actions, GitLab CI, Jenkins
- **Monitoring**: Log4j2, custom monitoring endpoints

---

## Migration Checklist (3.x → 4.x)

### Code Changes Required

1. **Java/Groovy Imports**
   ```groovy
   // Before (3.x)
   import javax.servlet.http.HttpServletRequest
   import javax.transaction.UserTransaction

   // After (4.x)
   import jakarta.servlet.http.HttpServletRequest
   import jakarta.transaction.UserTransaction
   ```

2. **Gradle Build Scripts**
   ```groovy
   // Before (3.x)
   maxParallelForks 1

   // After (4.x)
   maxParallelForks = 1
   ```

3. **Thread Operations**
   ```groovy
   // Before (3.x)
   Thread.currentThread().getId()

   // After (4.x)
   Thread.currentThread().threadId()
   ```

### Infrastructure Changes

1. **Delete and reindex OpenSearch/Elasticsearch data**
2. **Update PostgreSQL to 18.1+ or MySQL to 9.5+**
3. **Update Docker images to use Java 25 base (or Java 21 minimum)**
4. **Remove docker-compose `version` key (obsolete)**

### Component Updates

All project components have been updated for 4.x compatibility:
- `{project-branch}` branches for all repositories
- Updated build.gradle files for Gradle 9.x compatibility
- Jakarta namespace migration completed

---

## Third-Party Integrations

### Common to Both Versions
- **Email**: Moqui EmailServices (SMTP)
- **Document Generation**: Apache FOP (PDF), POI (Excel)
- **External APIs**: RestClient services, XML-SOAP services

### Version-Specific Libraries

| Library | 3.x Version | 4.x Version |
|---------|-------------|-------------|
| Apache Shiro | 1.x (INI factory) | 2.x (INI environment) |
| Commons FileUpload | javax variant | jakarta.servlet6 variant |
| Mail API | javax.mail | jakarta.mail |

---

## Project Structure

```
moqui-project/
├── framework/              # Moqui Framework (symlink or submodule)
├── runtime/
│   ├── component/          # Application components
│   │   ├── {component}/
│   │   │   ├── .agent-os/  # Project-specific AI agent config
│   │   │   │   └── product/
│   │   │   │       └── tech-stack.md  # Specify 3.x or 4.x here
│   │   │   ├── entity/     # Entity definitions
│   │   │   ├── service/    # Service definitions
│   │   │   ├── screen/     # Screen definitions
│   │   │   ├── data/       # Seed and demo data
│   │   │   └── template/   # FreeMarker templates
│   │   └── agent-os/       # Shared AI agent configuration
│   │       └── standards/
│   │           └── global/
│   │               └── tech-stack.md  # This file (version-neutral)
│   ├── conf/               # Configuration files
│   └── lib/                # External libraries
└── docker/                 # Docker configuration (optional)
```

---

## Branch Naming Convention

| Branch Pattern | Description |
|----------------|-------------|
| `{project-branch}` | Stable 3.x release |
| `{project-branch}` | Development for 3.x |
| `{project-branch}` | Stable 4.x release |
| `{project-branch}` | Development for 4.x |
| `master` / `main` | Upstream original |
