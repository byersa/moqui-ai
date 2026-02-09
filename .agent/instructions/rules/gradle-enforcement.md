# Gradle-First Execution Rule
- **Primary Tool:** Always use the Gradle wrapper (`./gradlew`) for building, loading data, and running tasks.
- **Task Mapping:**
  - Build/Compile: `./gradlew clean build`
  - Load Data: `./gradlew load`
  - Clean: `./gradlew clean`
- **Constraint:** Do not attempt to manually manage classpaths or project dependencies. Trust the `build.gradle` file in the root.

