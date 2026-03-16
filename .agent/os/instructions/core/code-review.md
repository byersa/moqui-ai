---
description: Comprehensive code review workflow for project components
globs:
alwaysApply: false
version: 1.0
encoding: UTF-8
---

# Code Review Workflow

## Overview

Comprehensive code review for project components ensuring quality, compliance, and best practices across all domains through systematic analysis and validation.

**INTERACTIVE WORKFLOW**: This command begins with an interactive selection process to determine what changes to review (uncommitted changes, recent commits, or specific commits) and which repositories to include in the review.

<process_flow>

<step number="0" name="review_target_selection">

### Step 0: Review Target Selection (Interactive)

Determine what changes to review and which repositories to examine.

<target_selection>
  <interactive_options>
    <review_type_selection>
      PROMPT: "What would you like to review?"
      OPTIONS:
        1. Uncommitted changes (working directory modifications)
        2. Latest commit(s) in current branch
        3. Specific commit or commit range
        4. Pull request changes
      
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
        PROMPT: "Enter PR number or branch comparison (e.g., main...feature-branch):"
        REVIEW_MODE: pull_request
        GIT_COMMANDS: [git diff base...head]
    </review_type_selection>
    
    <repository_selection>
      <auto_detection>
        # Detect available repositories
        FRAMEWORK_REPO: (auto-detected from project root)
        COMPONENT_REPOS:
          # Auto-detected: scan runtime/component/ for git repositories
          
        # Check each repository for changes
        FOR_EACH repository:
          IF review_mode == "uncommitted_changes":
            CHECK: git status --porcelain
            IF has_changes:
              MARK_AS: available_for_review
          ELSE:
            MARK_AS: available_for_review
      </auto_detection>
      
      <repository_prompt>
        PROMPT: "Which repositories should be included in the review?"
        OPTIONS:
          1. All repositories with changes (recommended for uncommitted)
          2. Framework repository only
          3. Component repositories only
          4. Specific repositories (select from list)
          
        IF option_4_selected:
          SHOW_LIST: available_repositories
          ALLOW_MULTI_SELECT: true
      </repository_prompt>
    </repository_selection>
  </interactive_options>
  
  <change_collection>
    <collect_changes>
      FOR_EACH selected_repository:
        SWITCH review_mode:
          CASE "uncommitted_changes":
            EXECUTE: git status --porcelain
            EXECUTE: git diff
            STORE: uncommitted_files, diff_content
          
          CASE "recent_commits":
            EXECUTE: git log --oneline -n {commit_count}
            EXECUTE: git diff HEAD~{commit_count}..HEAD --name-status
            EXECUTE: git show HEAD~{i} for each commit
            STORE: commit_info, changed_files, diff_content
          
          CASE "specific_commits":
            EXECUTE: git show {commit_range}
            EXECUTE: git diff {commit_range} --name-status
            STORE: commit_info, changed_files, diff_content
          
          CASE "pull_request":
            EXECUTE: git diff {base}...{head} --name-status
            EXECUTE: git log {base}..{head} --oneline
            STORE: pr_commits, changed_files, diff_content
    </collect_changes>
    
    <change_summary>
      DISPLAY: "Review Summary:"
      SHOW: 
        - Review mode: {review_mode}
        - Repositories included: {selected_repositories}
        - Total files changed: {file_count}
        - Change breakdown by type:
          - Added: {added_count}
          - Modified: {modified_count}
          - Deleted: {deleted_count}
        - Affected domains: {entity, service, screen, data}
    </change_summary>
  </change_collection>
</target_selection>

</step>

<step number="1" name="review_scope_assessment">

### Step 1: Review Scope Assessment and Planning

Analyze collected changes from Step 0 and plan comprehensive review approach.

<scope_analysis>
  <change_assessment>
    <file_analysis>
      # Use changes collected in Step 0
      - Process files from: {change_collection.collected_changes}
      - Review mode: {target_selection.review_mode}
      - Repositories: {target_selection.selected_repositories}
      
      # Analyze collected files
      - Categorize changes by Moqui domain (entity, service, screen, data)
      - Assess complexity and risk level of changes
      - Identify dependencies and integration points
      - Evaluate potential impact on existing functionality
    </file_analysis>
    
    <domain_mapping>
      <entity_changes>
        IF changes_include(entity_definitions, relationships, views, indexes):
          REVIEW_DOMAINS += entity
          RISK_LEVEL: high (foundation changes)
          FOCUS: data_model_integrity_and_performance
      </entity_changes>
      
      <service_changes>
        IF changes_include(service_definitions, business_logic, integrations):
          REVIEW_DOMAINS += service
          RISK_LEVEL: high (business_logic_changes)
          FOCUS: functionality_and_integration_correctness
      </service_changes>
      
      <screen_changes>
        IF changes_include(screen_definitions, forms, transitions, widgets):
          REVIEW_DOMAINS += screen
          RISK_LEVEL: medium (user_interface_changes)
          FOCUS: usability_and_workflow_correctness
      </screen_changes>
      
      <data_changes>
        IF changes_include(seed_data, demo_data, configuration):
          REVIEW_DOMAINS += data
          RISK_LEVEL: medium (configuration_and_data_changes)
          FOCUS: data_consistency_and_loading_integrity
      </data_changes>
      
      <build_changes>
        IF changes_include(build_scripts, dependencies, configuration):
          REVIEW_DOMAINS += build
          RISK_LEVEL: high (system_integration_changes)
          FOCUS: build_and_deployment_integrity
      </build_changes>
    </domain_mapping>
  </change_assessment>
  
  <review_planning>
    <priority_assessment>
      <high_priority_areas>
        - Security-sensitive code changes
        - Performance-critical components
        - Public API and interface modifications
        - Database schema and migration changes
        - Integration point modifications
      </high_priority_areas>
      
      <review_sequence>
        SEQUENCE: entities → services → screens → data → build
        RATIONALE: Review_foundation_layers_before_dependent_layers
        COORDINATION: Ensure_interface_compatibility_across_domains
      </review_sequence>
    </priority_assessment>
    
    <review_methodology>
      <automated_checks>
        - Code formatting and style validation
        - Build compilation and dependency verification
        - Automated test execution and coverage analysis
        - Security vulnerability scanning
        - Performance regression detection
      </automated_checks>
      
      <manual_review_focus>
        - Business logic correctness and completeness
        - Architecture and design pattern compliance
        - Code quality and maintainability assessment
        - Security and privacy considerations
        - Integration and compatibility validation
      </manual_review_focus>
    </review_methodology>
  </review_planning>
</scope_analysis>

</step>

<step number="2" subagent="moqui-entity-specialist" name="entity_domain_review">

### Step 2: Entity Domain Review (Conditional)

Review entity-related changes for data model integrity and performance.

<entity_review_criteria>
  <conditional_execution>
    IF entity_domain_in_scope:
      EXECUTE entity_review_process
      USE_CONTEXT: {review_mode, selected_repositories, collected_changes}
    ELSE:
      SKIP to_step_3
  </conditional_execution>
  
  <entity_design_review>
    <data_model_validation>
      <naming_conventions>
        VERIFY entity_names_follow_PascalCase_convention
        CHECK field_names_use_camelCase_convention
        VALIDATE package_names_follow_lowercase_dot_notation
        CONFIRM relationship_names_are_descriptive_and_clear
      </naming_conventions>
      
      <structural_integrity>
        VERIFY primary_key_strategy_is_appropriate
        CHECK foreign_key_relationships_are_correctly_defined
        VALIDATE entity_field_types_and_constraints
        ENSURE proper_use_of_audit_fields
        REVIEW entity_inheritance_and_extension_patterns
      </structural_integrity>
      
      <relationship_validation>
        VERIFY relationship_mappings_are_correct
        CHECK relationship_types_are_appropriate
        VALIDATE cascading_behavior_and_constraints
        ENSURE circular_dependencies_are_handled_properly
        REVIEW many_to_many_relationship_implementations
      </relationship_validation>
    </data_model_validation>
    
    <performance_review>
      <index_strategy>
        VERIFY indexes_support_expected_query_patterns
        CHECK for_missing_indexes_on_frequently_queried_fields
        VALIDATE composite_indexes_are_properly_ordered
        ENSURE no_unnecessary_or_duplicate_indexes
        REVIEW foreign_key_index_strategies
      </index_strategy>
      
      <query_optimization>
        ANALYZE entity_view_definitions_for_efficiency
        CHECK for_potential_n_plus_one_query_scenarios
        VALIDATE join_strategies_and_fetch_patterns
        REVIEW query_complexity_and_performance_implications
        ENSURE proper_use_of_lazy_loading_strategies
      </query_optimization>
    </performance_review>
  </entity_design_review>
  
  <data_migration_review>
    <migration_validation>
      IF entity_changes_require_migration:
        VERIFY migration_scripts_are_complete_and_accurate
        CHECK rollback_procedures_are_available
        VALIDATE data_transformation_logic_is_correct
        ENSURE migration_is_idempotent_and_safe
        REVIEW performance_impact_of_migration_operations
    </migration_validation>
  </data_migration_review>
</entity_review_criteria>

</step>

<step number="3" subagent="moqui-service-specialist" name="service_domain_review">

### Step 3: Service Domain Review (Conditional)

Review service-related changes for business logic correctness and integration integrity.

<service_review_criteria>
  <conditional_execution>
    IF service_domain_in_scope:
      EXECUTE service_review_process
    ELSE:
      SKIP to_step_4
  </conditional_execution>
  
  <service_design_review>
    <interface_validation>
      <service_definition>
        VERIFY service_names_follow_verb_noun_convention
        CHECK input_output_parameters_are_properly_typed
        VALIDATE parameter_descriptions_are_clear_and_complete
        ENSURE proper_use_of_required_and_optional_parameters
        REVIEW service_authentication_and_authorization_requirements
      </service_definition>
      
      <transaction_management>
        VERIFY transaction_settings_are_appropriate
        CHECK transaction_boundaries_align_with_business_requirements
        VALIDATE error_handling_and_rollback_scenarios
        ENSURE proper_isolation_levels_for_concurrent_operations
        REVIEW nested_transaction_and_propagation_behavior
      </transaction_management>
    </interface_validation>
    
    <business_logic_review>
      <correctness_validation>
        VERIFY business_rules_are_implemented_correctly
        CHECK calculations_and_algorithms_for_accuracy
        VALIDATE input_validation_and_sanitization
        ENSURE proper_error_handling_and_user_feedback
        REVIEW edge_cases_and_boundary_condition_handling
      </correctness_validation>
      
      <performance_review>
        ANALYZE service_execution_efficiency
        CHECK for_unnecessary_database_operations
        VALIDATE caching_strategies_and_implementation
        ENSURE proper_resource_management_and_cleanup
        REVIEW concurrent_execution_and_thread_safety
      </performance_review>
    </business_logic_review>
  </service_design_review>
  
  <integration_review>
    <external_integration>
      IF services_integrate_with_external_systems:
        VERIFY integration_patterns_and_error_handling
        CHECK timeout_and_retry_logic_implementation
        VALIDATE security_measures_for_external_calls
        ENSURE proper_data_transformation_and_mapping
        REVIEW circuit_breaker_and_fallback_mechanisms
    </external_integration>
    
    <internal_integration>
      VERIFY service_dependencies_and_call_patterns
      CHECK parameter_passing_and_transformation
      VALIDATE service_composition_and_orchestration
      ENSURE proper_service_interface_versioning
      REVIEW service_discovery_and_configuration
    </internal_integration>
  </integration_review>
</service_review_criteria>

</step>

<step number="4" subagent="moqui-screen-specialist" name="screen_domain_review">

### Step 4: Screen Domain Review (Conditional)

Review screen-related changes for user experience and interface quality.

<screen_review_criteria>
  <conditional_execution>
    IF screen_domain_in_scope:
      EXECUTE screen_review_process
    ELSE:
      SKIP to_step_5
  </conditional_execution>
  
  <user_interface_review>
    <screen_structure_validation>
      <navigation_and_flow>
        VERIFY screen_organization_follows_logical_hierarchy
        CHECK navigation_patterns_are_intuitive_and_consistent
        VALIDATE screen_transitions_and_parameter_passing
        ENSURE proper_breadcrumb_and_navigation_context
        REVIEW deep_linking_and_URL_structure
      </navigation_and_flow>
      
      <form_and_widget_validation>
        VERIFY form_structures_are_logical_and_user_friendly
        CHECK field_validation_rules_and_error_messages
        VALIDATE widget_configuration_and_behavior
        ENSURE proper_use_of_form_types_and_patterns
        REVIEW responsive_design_and_mobile_compatibility
      </form_and_widget_validation>
    </screen_structure_validation>
    
    <usability_review>
      <user_experience>
        ANALYZE user_workflows_for_efficiency_and_clarity
        CHECK error_handling_and_user_feedback_quality
        VALIDATE accessibility_features_and_compliance
        ENSURE consistent_look_and_feel_across_screens
        REVIEW loading_states_and_performance_perception
      </user_experience>
      
      <interaction_design>
        VERIFY intuitive_form_layouts_and_field_grouping
        CHECK appropriate_use_of_input_types_and_controls
        VALIDATE confirmation_dialogs_and_destructive_actions
        ENSURE proper_handling_of_required_and_optional_fields
        REVIEW keyboard_navigation_and_shortcuts
      </interaction_design>
    </usability_review>
  </user_interface_review>
  
  <security_and_performance_review>
    <security_validation>
      VERIFY proper_authorization_checks_on_screens
      CHECK parameter_validation_and_sanitization
      VALIDATE CSRF_protection_and_secure_form_handling
      ENSURE sensitive_data_protection_and_masking
      REVIEW session_management_and_timeout_handling

      NOTE: For deeper security analysis covering all 8 security dimensions
      (EntityFilters, artifact authorization, service authentication, permission
      consistency, REST API security, input validation, audit/sensitive data,
      screen security), run the /security-review command.
      See standards/backend/security-review.md for the full security checklist.
    </security_validation>
    
    <performance_optimization>
      ANALYZE screen_loading_performance_and_optimization
      CHECK for_unnecessary_data_loading_and_queries
      VALIDATE caching_strategies_for_static_content
      ENSURE efficient_widget_rendering_and_updates
      REVIEW client_side_resource_optimization
    </performance_optimization>
  </security_and_performance_review>
</screen_review_criteria>

</step>

<step number="5" subagent="moqui-data-specialist" name="data_domain_review">

### Step 5: Data Domain Review (Conditional)

Review data-related changes for consistency and integrity.

<data_review_criteria>
  <conditional_execution>
    IF data_domain_in_scope:
      EXECUTE data_review_process
    ELSE:
      SKIP to_step_6
  </conditional_execution>
  
  <data_structure_review>
    <file_organization>
      <naming_and_structure>
        VERIFY data_files_follow_naming_conventions
        CHECK proper_file_organization_and_categorization
        VALIDATE load_sequence_and_priority_numbering
        ENSURE consistent_XML_structure_and_formatting
        REVIEW file_size_and_performance_considerations
      </naming_and_structure>
      
      <content_validation>
        VERIFY data_content_follows_entity_definitions
        CHECK foreign_key_references_are_valid
        VALIDATE data_types_and_field_constraints
        ENSURE proper_use_of_null_values_and_defaults
        REVIEW data_completeness_and_required_fields
      </content_validation>
    </file_organization>
    
    <data_integrity_review>
      <referential_integrity>
        VERIFY all_foreign_key_references_exist
        CHECK for_orphaned_records_and_broken_references
        VALIDATE circular_dependencies_in_data_loading
        ENSURE proper_handling_of_cascade_operations
        REVIEW data_consistency_across_related_entities
      </referential_integrity>
      
      <business_rule_compliance>
        VERIFY data_complies_with_business_validation_rules
        CHECK for_data_that_violates_entity_constraints
        VALIDATE configuration_data_completeness
        ENSURE demo_data_represents_realistic_scenarios
        REVIEW seed_data_covers_all_necessary_configurations
      </business_rule_compliance>
    </data_integrity_review>
  </data_structure_review>
  
  <loading_and_performance_review>
    <loading_sequence>
      VERIFY data_loading_order_respects_dependencies
      CHECK for_circular_dependencies_in_loading
      VALIDATE batch_size_and_performance_optimization
      ENSURE proper_error_handling_during_loading
      REVIEW rollback_and_recovery_procedures
    </loading_sequence>
    
    <performance_impact>
      ANALYZE loading_time_and_resource_usage
      CHECK for_inefficient_bulk_operations
      VALIDATE memory_usage_during_large_data_loads
      ENSURE reasonable_startup_time_impact
      REVIEW indexing_strategy_for_loaded_data
    </performance_impact>
  </loading_and_performance_review>
</data_review_criteria>

</step>

<step number="6" subagent="moqui-build-coordinator" name="build_and_integration_review">

### Step 6: Build and Integration Review

Review build configuration and system integration aspects.

<build_review_criteria>
  <build_configuration_review>
    <dependency_management>
      <dependency_validation>
        VERIFY all_dependencies_are_necessary_and_appropriate
        CHECK for_version_conflicts_and_compatibility_issues
        VALIDATE security_vulnerabilities_in_dependencies
        ENSURE proper_scope_and_transitivity_management
        REVIEW licensing_compliance_for_all_dependencies
      </dependency_validation>
      
      <build_script_validation>
        VERIFY build_scripts_are_correct_and_efficient
        CHECK for_build_performance_optimizations
        VALIDATE deployment_and_packaging_configurations
        ENSURE proper_environment_specific_configurations
        REVIEW build_reproducibility_and_determinism
      </build_script_validation>
    </dependency_management>
    
    <integration_validation>
      <component_integration>
        VERIFY all_components_integrate_correctly
        CHECK for_classpath_conflicts_and_issues
        VALIDATE component_loading_order_and_dependencies
        ENSURE proper_configuration_override_precedence
        REVIEW multi_repository_coordination_and_versioning
      </component_integration>
      
      <deployment_readiness>
        VERIFY deployment_scripts_and_procedures
        CHECK environment_specific_configuration_management
        VALIDATE database_migration_and_upgrade_procedures
        ENSURE proper_backup_and_rollback_capabilities
        REVIEW monitoring_and_health_check_configurations
      </deployment_readiness>
    </integration_validation>
  </build_configuration_review>
  
  <system_quality_review>
    <performance_validation>
      VERIFY build_time_and_resource_usage_are_reasonable
      CHECK for_unnecessary_compilation_and_processing
      VALIDATE startup_time_and_initialization_performance
      ENSURE memory_usage_and_resource_efficiency
      REVIEW scalability_and_load_handling_capabilities
    </performance_validation>
    
    <reliability_validation>
      VERIFY error_handling_and_recovery_mechanisms
      CHECK system_stability_under_load_and_stress
      VALIDATE fault_tolerance_and_graceful_degradation
      ENSURE proper_logging_and_monitoring_capabilities
      REVIEW disaster_recovery_and_backup_procedures
    </reliability_validation>
  </system_quality_review>
</build_review_criteria>

</step>

<step number="7" subagent="moqui-test-specialist" name="test_coverage_and_quality_review">

### Step 7: Test Coverage and Quality Review

Review test coverage and validate testing strategy for changes.

<test_review_criteria>
  <test_coverage_analysis>
    <coverage_validation>
      <unit_test_coverage>
        VERIFY adequate_unit_test_coverage_for_new_code
        CHECK test_quality_and_assertion_completeness
        VALIDATE test_isolation_and_independence
        ENSURE proper_mock_and_stub_usage
        REVIEW test_naming_and_documentation_clarity
      </unit_test_coverage>
      
      <integration_test_coverage>
        VERIFY integration_tests_cover_critical_workflows
        CHECK cross_domain_integration_testing
        VALIDATE end_to_end_scenario_coverage
        ENSURE proper_test_data_management
        REVIEW test_environment_setup_and_teardown
      </integration_test_coverage>
    </coverage_validation>
    
    <test_quality_review>
      <test_design_validation>
        VERIFY tests_cover_both_positive_and_negative_scenarios
        CHECK edge_cases_and_boundary_conditions_are_tested
        VALIDATE error_handling_and_exception_scenarios
        ENSURE performance_and_load_testing_where_appropriate
        REVIEW test_maintainability_and_readability
      </test_design_validation>
      
      <test_reliability>
        VERIFY tests_are_deterministic_and_repeatable
        CHECK for_flaky_tests_and_timing_dependencies
        VALIDATE test_data_consistency_and_isolation
        ENSURE proper_cleanup_and_resource_management
        REVIEW test_execution_performance_and_efficiency
      </test_reliability>
    </test_quality_review>
  </test_coverage_analysis>
  
  <testing_strategy_validation>
    <regression_prevention>
      VERIFY new_tests_prevent_regression_of_bug_fixes
      CHECK test_coverage_for_modified_functionality
      VALIDATE backward_compatibility_testing
      ENSURE performance_regression_testing
      REVIEW security_and_vulnerability_testing
    </regression_prevention>
    
    <test_automation>
      VERIFY appropriate_level_of_test_automation
      CHECK integration_with_continuous_integration
      VALIDATE automated_test_execution_and_reporting
      ENSURE proper_test_failure_analysis_and_reporting
      REVIEW test_maintenance_and_update_procedures
    </test_automation>
  </testing_strategy_validation>
</test_review_criteria>

</step>

<step number="8" name="cross_domain_integration_review">

### Step 8: Cross-Domain Integration and Final Validation

Review integration between all modified domains and final quality validation.

<integration_review>
  <cross_domain_compatibility>
    <interface_validation>
      <entity_service_integration>
        VERIFY entity_changes_support_service_requirements
        CHECK service_parameter_compatibility_with_entities
        VALIDATE transaction_boundaries_across_entity_operations
        ENSURE proper_error_propagation_from_data_to_service_layer
      </entity_service_integration>
      
      <service_screen_integration>
        VERIFY service_interfaces_meet_screen_requirements
        CHECK parameter_passing_between_screens_and_services
        VALIDATE error_handling_and_user_feedback_flow
        ENSURE proper_authorization_and_security_integration
      </service_screen_integration>
      
      <data_all_domains_integration>
        VERIFY configuration_data_supports_all_domain_requirements
        CHECK demo_data_enables_proper_testing_across_domains
        VALIDATE data_loading_supports_system_initialization
        ENSURE proper_data_migration_coordination_across_domains
      </data_all_domains_integration>
    </interface_validation>
    
    <workflow_validation>
      VERIFY complete_user_workflows_function_correctly
      CHECK end_to_end_business_processes_work_as_intended
      VALIDATE error_scenarios_and_recovery_procedures
      ENSURE performance_is_acceptable_across_all_domains
      REVIEW security_and_authorization_throughout_workflows
    </workflow_validation>
  </cross_domain_compatibility>
  
  <final_quality_validation>
    <code_quality_standards>
      <maintainability>
        VERIFY code_is_readable_and_well_documented
        CHECK proper_separation_of_concerns_and_modularity
        VALIDATE consistent_coding_standards_and_patterns
        ENSURE appropriate_use_of_design_patterns
        REVIEW code_complexity_and_technical_debt
      </maintainability>
      
      <security_and_compliance>
        VERIFY security_best_practices_are_followed
        CHECK for_potential_security_vulnerabilities
        VALIDATE data_privacy_and_protection_compliance
        ENSURE proper_input_validation_and_sanitization
        REVIEW authentication_and_authorization_mechanisms
      </security_and_compliance>
    </code_quality_standards>
    
    <performance_and_scalability>
      VERIFY performance_impact_is_within_acceptable_limits
      CHECK for_potential_performance_bottlenecks
      VALIDATE resource_usage_and_efficiency
      ENSURE scalability_considerations_are_addressed
      REVIEW caching_and_optimization_strategies
    </performance_and_scalability>
  </final_quality_validation>
</integration_review>

</step>

<step number="9" name="review_consolidation_and_feedback">

### Step 9: Review Consolidation and Feedback

Consolidate findings and provide comprehensive feedback with action items.

<review_consolidation>
  <review_context>
    INCLUDE in feedback:
      - Review mode: {target_selection.review_mode}
      - Repositories reviewed: {target_selection.selected_repositories}
      - Scope: {uncommitted_changes | commits_reviewed | pr_changes}
  </review_context>
  
  <findings_synthesis>
    <issue_categorization>
      <critical_issues>
        - Security vulnerabilities and data exposure risks
        - Functional correctness problems and business logic errors
        - Performance regressions and scalability concerns
        - Integration failures and compatibility breaks
      </critical_issues>
      
      <important_improvements>
        - Code quality and maintainability enhancements
        - Documentation and commenting improvements
        - Test coverage and quality enhancements
        - Design pattern and architecture optimizations
      </important_improvements>
      
      <minor_suggestions>
        - Code style and formatting consistency
        - Naming convention improvements
        - Performance micro-optimizations
        - Documentation clarifications
      </minor_suggestions>
    </issue_categorization>
    
    <priority_assessment>
      CRITICAL: Must_fix_before_approval
      IMPORTANT: Should_fix_for_optimal_quality
      MINOR: Consider_fixing_for_consistency
      SUGGESTION: Optional_improvements_for_future
    </priority_assessment>
  </findings_synthesis>
  
  <feedback_delivery>
    <actionable_feedback>
      <specific_recommendations>
        FOR_EACH identified_issue:
          PROVIDE specific_location_and_context
          EXPLAIN why_it_is_a_problem
          SUGGEST concrete_solutions_and_alternatives
          INDICATE priority_level_and_impact
      </specific_recommendations>
      
      <positive_recognition>
        HIGHLIGHT well_implemented_patterns_and_solutions
        RECOGNIZE good_design_decisions_and_code_quality
        ACKNOWLEDGE proper_testing_and_documentation
        COMMEND security_and_performance_considerations
      </positive_recognition>
    </actionable_feedback>
    
    <approval_criteria>
      <must_fix_criteria>
        ALL_CRITICAL_ISSUES must_be_resolved
        SECURITY_VULNERABILITIES must_be_addressed
        FUNCTIONAL_CORRECTNESS must_be_verified
        INTEGRATION_COMPATIBILITY must_be_ensured
      </must_fix_criteria>
      
      <approval_conditions>
        IF all_critical_issues_resolved:
          STATUS: Approved_with_recommendations
        ELSE_IF critical_issues_remain:
          STATUS: Needs_work_before_approval
        END_IF
      </approval_conditions>
    </approval_criteria>
  </feedback_delivery>
</review_consolidation>

</step>

</process_flow>

## Review Quality Standards

<quality_gates>
  <domain_specific_quality>
    <entity_quality_standards>
      - [ ] Entity definitions follow Moqui naming conventions
      - [ ] Relationships are properly defined and efficient
      - [ ] Indexes support expected query patterns
      - [ ] Data model changes include migration scripts
      - [ ] Performance impact is analyzed and acceptable
    </entity_quality_standards>
    
    <service_quality_standards>
      - [ ] Service interfaces are clean and well-documented
      - [ ] Business logic is correct and efficient
      - [ ] Error handling is comprehensive and user-friendly
      - [ ] Transaction management is appropriate
      - [ ] Integration patterns follow best practices
    </service_quality_standards>
    
    <screen_quality_standards>
      - [ ] User interfaces are intuitive and accessible
      - [ ] Navigation flows are logical and efficient
      - [ ] Forms provide clear validation and feedback
      - [ ] Responsive design works across devices
      - [ ] Security and authorization are properly implemented
    </screen_quality_standards>
    
    <data_quality_standards>
      - [ ] Data files are properly organized and named
      - [ ] Data integrity and referential consistency maintained
      - [ ] Loading sequence respects dependencies
      - [ ] Demo data scenarios are realistic and complete
      - [ ] Performance impact of data loading is acceptable
    </data_quality_standards>
  </domain_specific_quality>
  
  <cross_cutting_quality>
    <security_standards>
      - [ ] No security vulnerabilities introduced
      - [ ] Input validation and sanitization implemented
      - [ ] Authentication and authorization properly enforced
      - [ ] Sensitive data protected and encrypted
      - [ ] Security best practices followed
    </security_standards>
    
    <performance_standards>
      - [ ] No significant performance regressions
      - [ ] Resource usage is efficient and optimized
      - [ ] Caching strategies are appropriate
      - [ ] Database queries are optimized
      - [ ] Scalability considerations addressed
    </performance_standards>
    
    <maintainability_standards>
      - [ ] Code is readable and well-documented
      - [ ] Design patterns are used appropriately
      - [ ] Separation of concerns is maintained
      - [ ] Technical debt is not increased
      - [ ] Testing coverage is adequate
    </maintainability_standards>
  </cross_cutting_quality>
</quality_gates>

## Success Criteria

<review_success_metrics>
  <quality_assurance>
    - All critical issues identified and resolved
    - Code quality standards met or exceeded
    - Security and privacy requirements satisfied
    - Performance and scalability requirements met
    - Integration and compatibility validated
  </quality_assurance>
  
  <process_effectiveness>
    - Comprehensive coverage of all changed domains
    - Efficient review process with minimal iterations
    - Clear and actionable feedback provided
    - Knowledge sharing and learning achieved
    - Continuous improvement of code quality
  </process_effectiveness>
  
  <stakeholder_satisfaction>
    - Development team confidence in changes
    - Business stakeholder approval of functionality
    - User experience and usability validated
    - Deployment readiness confirmed
    - Risk mitigation strategies in place
  </stakeholder_satisfaction>
</review_success_metrics>