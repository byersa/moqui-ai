# Security & Configuration Blueprint

**Date**: 2026-02-21
**Goal**: Enforce strict separation of secrets and environment configuration from application code, ensuring the `aitree` and `huddle` apps remain clean, publishable, and open-source safe.

## The Problem
Hardcoding database credentials, JWT keys, external API keys, or even local directory paths inside component XML files or Groovy scripts prevents the application from being safely published to a public repository.

## The Solution: Moqui Environment Variables
The AG (Agent) must **never** write hardcoded secrets directly into `runtime/component/*` code. Instead, all configurations must be pulled from the Moqui execution environment.

### 1. Declaring Configuration Dependencies
If the application needs an external configuration (e.g., an API key for a third-party service), the AG should define it in the component's Groovy / XML scripts using Moqui's System property retrieval:

```groovy
// Correct pattern for reading environment/system properties
String apiKey = System.getProperty("aitree_external_api_key")
```

### 2. Local Environment Setup (`.env`)
For developers running the code locally, secrets will be managed by a standard `.env` model. 
- The `.agent/workflows/` directory may contain scripts (e.g., `setup-local-env.sh`) that generate a Git-ignored file.
- The wrapper scripts that start the Moqui instance (`start-moqui.sh`) will load this `.env` file before executing `java -jar moqui.war`.

### 3. Agent Instructions
- **Do not** create custom `config.json` files inside the component directory for secrets.
- **Do not** inject passwords into `<entity-facade-xml>` blocks in component initializers.
- **Do** assume that all required passwords to run the instance have been provided to the underlying `.env` or injected via `MoquiDevConf.xml`.
