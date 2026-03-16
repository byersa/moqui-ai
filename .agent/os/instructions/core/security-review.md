---
description: Security review workflow for Moqui Framework components
globs:
alwaysApply: false
version: 1.0
encoding: UTF-8
---

# Security Review Workflow

## Overview

Systematic security review for Moqui Framework components, examining 8 security dimensions to identify gaps in entity-level security, authorization, authentication, and data protection.

**INTERACTIVE WORKFLOW**: This command begins with an interactive selection process to determine what changes to review, then systematically evaluates each applicable security dimension.

<process_flow>

<step number="0" name="review_target_selection">

### Step 0: Review Target Selection (Interactive)

Determine what to review. Same interactive pattern as code-review.

<target_selection>
  <interactive_options>
    <review_type_selection>
      PROMPT: "What would you like to security-review?"
      OPTIONS:
        1. Uncommitted changes (working directory modifications)
        2. Latest commit(s) in current branch
        3. Specific commit or commit range
        4. Specific component or directory

      IF option_1_selected:
        REVIEW_MODE: uncommitted_changes
        GIT_COMMANDS: [git status, git diff]
      ELSE_IF option_2_selected:
        PROMPT: "How many recent commits to review? (default: 1)"
        REVIEW_MODE: recent_commits
        GIT_COMMANDS: [git show, git diff HEAD~N..HEAD]
      ELSE_IF option_3_selected:
        PROMPT: "Enter commit hash or range (e.g., abc123 or main..feature):"
        REVIEW_MODE: specific_commits
        GIT_COMMANDS: [git show, git diff]
      ELSE_IF option_4_selected:
        PROMPT: "Enter component path or directory to review:"
        REVIEW_MODE: directory_scan
        SCAN_COMMANDS: [glob for entity/service/screen/data/rest files]
    </review_type_selection>
  </interactive_options>

  <change_collection>
    <collect_changes>
      FOR_EACH selected_scope:
        COLLECT: changed_files, diff_content
        CLASSIFY_BY_TYPE:
          - entity_files: *.entity.xml, entity/ directories
          - service_files: *.services.xml, service/ directories
          - screen_files: *.xml in screen/ directories
          - data_files: *.xml in data/ directories
          - rest_files: *.rest.xml
          - groovy_files: *.groovy in service/ directories
    </collect_changes>

    <change_summary>
      DISPLAY: "Security Review Scope:"
      SHOW:
        - Review mode: {review_mode}
        - Files in scope: {file_count}
        - Artifact breakdown:
          - Entity definitions: {entity_count}
          - Service definitions/implementations: {service_count}
          - Screen definitions: {screen_count}
          - Data files (seed/security): {data_count}
          - REST API definitions: {rest_count}
    </change_summary>
  </change_collection>
</target_selection>

</step>

<step number="1" name="security_scope_assessment">

### Step 1: Security Scope Assessment

Classify changed files by artifact type and determine which security dimensions to prioritize.

<scope_assessment>
  <dimension_relevance>
    FOR_EACH changed_file:
      IF entity_definition:
        PRIORITIZE: Dimension 1 (EntityFilters), Dimension 7 (Audit)
      IF service_definition_or_implementation:
        PRIORITIZE: Dimension 3 (Authentication), Dimension 4 (Permissions), Dimension 6 (Validation)
      IF screen_definition:
        PRIORITIZE: Dimension 2 (Authorization), Dimension 8 (Screen Security)
      IF rest_definition:
        PRIORITIZE: Dimension 5 (REST Security), Dimension 2 (Authorization)
      IF data_file:
        PRIORITIZE: Dimension 1 (EntityFilters), Dimension 2 (Authorization)
      IF groovy_script:
        PRIORITIZE: Dimension 6 (Validation), Dimension 3 (Authentication)
  </dimension_relevance>

  <dimension_plan>
    DISPLAY: "Applicable Security Dimensions:"
    FOR_EACH dimension WHERE relevant:
      SHOW: "[Dimension #] {name} — {priority_level} — {reason}"

    NOTE: Dimensions with no relevant files are marked N/A in the final report
  </dimension_plan>
</scope_assessment>

</step>

<step number="2" name="entityfilter_review">

### Step 2: EntityFilter Review (Dimension 1)

Check new entities for filter definitions, fail-safe patterns, context propagation, and view-entity alias matching.

<entityfilter_review>
  <conditional_execution>
    IF entity_files_in_scope OR data_files_in_scope:
      EXECUTE entityfilter_checks
    ELSE:
      MARK Dimension_1 as N/A
      SKIP to_step_3
  </conditional_execution>

  <filter_definition_checks>
    <new_entities>
      FOR_EACH new_or_modified_entity:
        CHECK: Does entity store org-scoped or tenant-scoped data?
        IF yes:
          VERIFY: EntityFilter definition exists in data files
          VERIFY: filterMap uses fail-safe pattern: `filterOrgIds ? filterOrgIds : ['-NO-MATCH-']`
          VERIFY: UserGroupEntityFilterSet records link filter to relevant user groups
          FLAG_IF: filterMap uses bare variable without fallback (e.g., `[field:filterOrgIds]`)
          SEVERITY: CRITICAL if missing, CRITICAL if no fail-safe
    </new_entities>

    <view_entity_alias_matching>
      FOR_EACH new_or_modified_view_entity:
        IDENTIFY: Which member entities have EntityFilters
        FOR_EACH filtered_member:
          VERIFY: Alias name matches the filterMap field name exactly
          VERIFY: At least one alias from filtered member appears in typical queries
          FLAG_IF: alias-all with prefix could mask the filtered field name
          FLAG_IF: filtered member could be trimmed (no alias in SELECT/WHERE/ORDER BY)
          SEVERITY: CRITICAL for alias mismatch, HIGH for potential trimming
    </view_entity_alias_matching>

    <filter_context_propagation>
      FOR_EACH service_querying_filtered_entities:
        VERIFY: Filter context setup is called before entity queries
        CHECK: setup#FilterContext or equivalent is invoked with disable-authz="true"
        FLAG_IF: Entity query occurs without prior filter context setup
        SEVERITY: CRITICAL if missing in REST-facing services
    </filter_context_propagation>
  </filter_definition_checks>
</entityfilter_review>

</step>

<step number="3" name="artifact_authorization_review">

### Step 3: Artifact Authorization Review (Dimension 2)

Check new screens and REST endpoints for ArtifactGroup + ArtifactAuthz records.

<authorization_review>
  <conditional_execution>
    IF screen_files_in_scope OR rest_files_in_scope OR data_files_in_scope:
      EXECUTE authorization_checks
    ELSE:
      MARK Dimension_2 as N/A
      SKIP to_step_4
  </conditional_execution>

  <screen_authorization>
    FOR_EACH new_or_modified_screen:
      VERIFY: ArtifactGroup record exists for the screen
      VERIFY: ArtifactGroupMember references correct screen location
      VERIFY: ArtifactGroupMember has inheritAuthz="Y" for parent screens
      VERIFY: ArtifactAuthz records exist per applicable user group
      FLAG_IF: Screen is mounted but has no ArtifactAuthz (accessible to no one or everyone)
      FLAG_IF: SubscreensItem uses deprecated userGroupId attribute
      SEVERITY: CRITICAL for missing authz on sensitive screens, HIGH for missing inheritAuthz
  </screen_authorization>

  <rest_authorization>
    FOR_EACH new_or_modified_rest_resource:
      VERIFY: ArtifactGroup covers the REST endpoint
      VERIFY: ArtifactAuthz restricts access to appropriate user groups
      FLAG_IF: REST resource has no corresponding authorization data
      SEVERITY: CRITICAL
  </rest_authorization>

  <service_authorization>
    FOR_EACH new_service_that_should_be_restricted:
      CHECK: Is the service covered by an ArtifactGroup?
      FLAG_IF: Service performs sensitive operations but has no ArtifactAuthz
      SEVERITY: HIGH
  </service_authorization>
</authorization_review>

</step>

<step number="4" name="service_auth_and_validation_review">

### Step 4: Service Authentication & Input Validation Review (Dimensions 3, 6)

Review service authentication levels and input parameter validation.

<service_auth_review>
  <conditional_execution>
    IF service_files_in_scope:
      EXECUTE service_checks
    ELSE:
      MARK Dimension_3 AND Dimension_6 as N/A
      SKIP to_step_5
  </conditional_execution>

  <authentication_checks>
    FOR_EACH new_or_modified_service:
      CHECK: authenticate attribute value
      IF authenticate="anonymous-all":
        VERIFY: Justification exists (public endpoint, system job, filter setup service)
        FLAG_IF: Service modifies data but allows anonymous access
        SEVERITY: HIGH

      FOR_EACH service_call_with_disable_authz:
        VERIFY: disable-authz="true" is necessary (calling system service, filter setup)
        FLAG_IF: disable-authz used to bypass authorization without clear justification
        SEVERITY: HIGH
  </authentication_checks>

  <input_validation_checks>
    FOR_EACH service_in_parameter:
      CHECK: type attribute is set and appropriate
      IF text_parameter:
        CHECK: allow-html attribute (should be "none" unless HTML input needed)
        FLAG_IF: allow-html="any" (XSS risk)
        SEVERITY: HIGH for allow-html="any", MEDIUM for missing type

    FOR_EACH groovy_script_in_scope:
      SEARCH_FOR: SQL string concatenation, sql.execute with interpolation
      FLAG_IF: User input used in raw SQL construction
      SEVERITY: CRITICAL
  </input_validation_checks>
</service_auth_review>

</step>

<step number="5" name="rest_api_security_review">

### Step 5: REST API Security Review (Dimension 5)

Review REST endpoint security configuration and filter context setup.

<rest_security_review>
  <conditional_execution>
    IF rest_files_in_scope:
      EXECUTE rest_checks
    ELSE:
      MARK Dimension_5 as N/A
      SKIP to_step_6
  </conditional_execution>

  <authentication_checks>
    FOR_EACH rest_resource:
      CHECK: require-authentication attribute
      IF require-authentication != "true":
        VERIFY: Anonymous access is justified (public API, webhook receiver)
        FLAG_IF: POST/PUT/DELETE methods allow anonymous access
        SEVERITY: CRITICAL
  </authentication_checks>

  <filter_context_checks>
    FOR_EACH rest_method:
      IDENTIFY: Service called by the method
      TRACE: Does the service (or its call chain) set up filter context?
      FLAG_IF: Service queries filtered entities without prior filter context setup
      SEVERITY: CRITICAL
  </filter_context_checks>

  <csrf_pathway_check>
    FOR_EACH POST_rest_endpoint:
      NOTE: CSRF check is bypassed only when authenticated via Basic Auth / API key
      FLAG_IF: Endpoint expects browser-based POST without proper auth mechanism
      SEVERITY: HIGH
  </csrf_pathway_check>
</rest_security_review>

</step>

<step number="6" name="permission_consistency_review">

### Step 6: Permission Consistency Review (Dimension 4)

Trace hasPermission() calls back to UI elements and flag missing ArtifactAuthz.

<permission_review>
  <conditional_execution>
    IF service_files_in_scope OR screen_files_in_scope:
      EXECUTE permission_checks
    ELSE:
      MARK Dimension_4 as N/A
      SKIP to_step_7
  </conditional_execution>

  <permission_tracing>
    FOR_EACH hasPermission_call_in_services:
      IDENTIFY: What permission ID is checked
      TRACE_BACK: Which screen transition or button invokes this service
      CHECK: Does the screen element have ArtifactAuthz that would hide it for unauthorized users?
      FLAG_IF: Button is visible but service rejects with permission error
      SEVERITY: MEDIUM (poor UX, not a security breach)
      RECOMMENDATION: Replace hasPermission() with ArtifactAuthz where possible
  </permission_tracing>

  <permission_data_completeness>
    FOR_EACH permission_id_used_in_code:
      VERIFY: UserPermission record exists
      VERIFY: UserGroupPermission records link to appropriate groups
      FLAG_IF: Permission ID used in code but not defined in seed data
      SEVERITY: MEDIUM
  </permission_data_completeness>
</permission_review>

</step>

<step number="7" name="audit_and_sensitive_data_review">

### Step 7: Audit & Sensitive Data Review (Dimension 7)

Review audit logging configuration and sensitive data protection.

<audit_review>
  <conditional_execution>
    IF entity_files_in_scope OR service_files_in_scope:
      EXECUTE audit_checks
    ELSE:
      MARK Dimension_7 as N/A
      SKIP to_step_8
  </conditional_execution>

  <audit_log_checks>
    FOR_EACH new_or_modified_entity:
      IDENTIFY: Fields that track status, amounts, dates, or legal data
      CHECK: enable-audit-log="true" on sensitive fields
      FLAG_IF: Financial or status fields lack audit logging
      SEVERITY: MEDIUM for status fields, HIGH for financial/legal fields
  </audit_log_checks>

  <sensitive_data_checks>
    FOR_EACH new_or_modified_entity:
      IDENTIFY: PII fields (tax IDs, credit cards, SSN, personal identifiers)
      CHECK: encrypt="true" on PII fields
      FLAG_IF: PII stored in plain text
      SEVERITY: HIGH

    FOR_EACH service_implementation:
      SEARCH_FOR: ec.logger calls that might log sensitive data
      FLAG_IF: Logging PII, API keys, passwords, or tokens
      SEVERITY: MEDIUM

    FOR_EACH rest_service_out_parameters:
      CHECK: Sensitive internal fields not exposed in API responses
      FLAG_IF: Auto-parameters expose fields like passwords or internal IDs
      SEVERITY: MEDIUM
  </sensitive_data_checks>
</audit_review>

</step>

<step number="8" name="report_generation">

### Step 8: Report Generation

Produce a structured security review report following the template in `standards/backend/security-review.md`.

<report_generation>
  <compile_findings>
    AGGREGATE: All findings from Steps 2-7
    SORT_BY: severity (CRITICAL first), then dimension number
    DEDUPLICATE: Merge findings that point to the same root cause
  </compile_findings>

  <generate_report>
    USE_TEMPLATE: standards/backend/security-review.md § "Report Template"

    SECTIONS:
      1. Summary — severity counts
      2. Findings — per-finding details:
         - Location (file:line)
         - Dimension and check number
         - Issue description
         - Risk explanation
         - Specific fix recommendation
         - Standard reference (link to security.md / entity-filters.md section)
      3. Dimension Status — table showing PASS/FAIL/N/A per dimension
      4. Recommendations — prioritized action list

    DISPLAY: Full report to user
  </generate_report>

  <prioritized_recommendations>
    ORDER:
      1. CRITICAL findings (must fix before deployment)
      2. HIGH findings (fix in current iteration)
      3. MEDIUM findings (fix before next release)
      4. LOW findings (track for improvement)

    FOR_EACH recommendation:
      PROVIDE: Specific file path, what to change, which standard to follow
  </prioritized_recommendations>
</report_generation>

</step>

</process_flow>

## Success Criteria

<review_success_metrics>
  <completeness>
    - All applicable dimensions were evaluated
    - All changed files were classified and reviewed
    - Cross-cutting concerns (EntityFilters) checked across entity, service, and screen layers
  </completeness>

  <actionability>
    - Every finding includes a specific location and fix recommendation
    - Recommendations reference the correct standard for remediation guidance
    - Severity levels accurately reflect business risk
  </actionability>

  <accuracy>
    - No false positives from misunderstanding framework behavior
    - Framework auto-hide behavior (ArtifactAuthz) correctly distinguished from hasPermission()
    - View-entity alias trimming correctly identified based on query usage patterns
  </accuracy>
</review_success_metrics>
