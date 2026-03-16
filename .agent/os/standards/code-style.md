# Moqui Framework Code Style Guide

## Context

Code style rules specific to Moqui Framework development projects.

## General Moqui Code Standards

- **Line Width**: Set editor line width to 180 characters for optimal code visibility
- **Block Separation**: Separate code element blocks with blank lines for enhanced readability
- **Documentation Requirement**: Always document code blocks and services with clear objective descriptions
- **Interface Consistency**: Respect mockups built during interface definition phase
- **CSS Customization**: Use Moqui elements for CSS customization rather than inline styles
- **Deprecated Elements**: Remove all non-syntax elements deprecated from Moqui version 1.0

## Language-Specific Guidelines

### Groovy Code Style

- **CDATA Usage**: Use `CDATA` blocks for Groovy code to avoid HTML entities and preserve proper indentation
- **Variable Assignment Patterns**:
  - Use `from` attribute for Groovy expressions and variables
  - Use `value` attribute for string literals in `set field` operations
- **Type Awareness**: Understand implicit String typing when no explicit type is declared for input parameters
- **Expression Context**:
  - `from` attribute: Groovy expressions with full expression evaluation
  - `value` attribute: Localized strings with `${...}` expansion only

### Service Development Standards

- **Service Naming Conventions** (verb#Noun format):
  - `verb` attribute: Use lowercase action words (get, update, delete, create, store, etc.)
  - `noun` attribute: Use capitalized entity names (Customer, PartyFacility, etc.) - NO VERBS in noun
  - Examples: `get#Customer`, `update#OrderStatus`, `create#PartyFacility`, `store#CustomerOrder`
- **Service Priority**: Always prioritize Moqui standard services (mantle-usl) over custom implementations
- **Service Extension**: When modifying standard logic, extend rather than replicate existing functionality
- **Service Documentation**: Include clear descriptions of service purpose, inputs, and expected outputs

### XML Formatting (Moqui Screens, Services, Entities)

- **Indentation**: Use 4 spaces for indentation in Moqui XML files (different from general 2-space rule)
- **Schema Compliance**: Follow Moqui XML schema conventions strictly
- **Element Grouping**: Group related elements logically within XML structures
- **Attribute Formatting**: Place complex attributes on separate lines for readability

## Data Formatting Standards

- **Currency Formatting**: Use `ec.l10n.formatCurrency(amount, '#,##0.###')` for consistent currency display
- **Date Formatting**: Use `ec.l10n.format(date, 'dd MMM yyyy')` for standardized date presentation
- **Localization**: Use English for labels/text with standard patterns; implement translation files for Spanish versions

## Moqui-Specific Naming Conventions

- **Entity Names**: Use PascalCase (e.g., CustomerOrder, PartyFacility)
- **Field Names**: Use camelCase (e.g., firstName, orderDate)
- **Service Names**: Use verb#Noun format (e.g., get#Customer, update#OrderStatus)
- **Screen Names**: Use descriptive PascalCase names (e.g., CustomerProfile, OrderHistory)