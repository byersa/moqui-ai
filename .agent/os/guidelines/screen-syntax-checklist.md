# Moqui Screen Syntax Quick Validation Checklist

**📋 CRITICAL PRE-SUBMISSION VALIDATION**

Use this checklist before submitting any screen XML code to prevent common syntax errors.

## Entity-Find Validation

- [ ] **NO `use-iterator="true"` attribute** - This attribute does not exist in Moqui Framework
- [ ] **Limit/offset as ATTRIBUTES, not tags**:
  - ✅ Correct: `<entity-find ... limit="20" offset="10">`
  - ❌ Wrong: `<limit from="20"/>` or `<offset from="10"/>`
- [ ] **All referenced fields included in `<select-field>`** for form-list entity-find
- [ ] **No direct tenant filtering** - use EntityFilters instead
- [ ] **Define `recordCount` in actions when using `require-parameters` conditional**:
  - ✅ Correct:
    ```xml
    <actions>
        <entity-find-count entity-name="my.Entity" count-field="recordCount"/>
    </actions>
    <widgets>
        <form-list ...>
            <entity-find ...>
                <search-form-inputs require-parameters="${recordCount > 100000}"/>
            </entity-find>
        </form-list>
    </widgets>
    ```
  - ❌ Wrong: Using `recordCount` variable without defining it in actions - Screen will fail!
  - **Note**: IDE may show schema validation warnings for boolean expressions like `${recordCount > 100000}` but these work correctly at runtime in Moqui

## Header-Field Validation

- [ ] **NO `show="${condition}"` attribute** - Use conditional-field blocks instead
- [ ] **Valid attributes only**: title, show-order-by, sort-field, tooltip, scope
- [ ] **Conditional display uses `<conditional-field>` blocks**

## Hidden Field Validation

- [ ] **NO `default="value"` attribute** in hidden tag
- [ ] **Use `from` attribute at field level**: `<field name="param" from="value">`

## Form-List Validation

- [ ] **MANDATORY: `list` attribute ALWAYS present** - Must reference entity-find list name
  - ✅ Correct: `<form-list name="MyList" list="myEntityList">`
  - ❌ Wrong: `<form-list name="MyList">` - Screen will fail to render!
- [ ] **MANDATORY: `<columns>` with `<field-ref>` elements** - Empty columns cause render failure
  - ✅ Correct: `<columns><column><field-ref name="field1"/></column></columns>`
  - ❌ Wrong: `<columns><column><!-- empty --></column></columns>` - Screen will fail!
  - ❌ Wrong: No `<columns>` section for multi-column layout
- [ ] **List name must match entity-find list attribute**:
  - `<entity-find list="myList">` ↔ `<form-list list="myList">`
- [ ] **Row-selection uses valid `id-field`**
- [ ] **All fields used in conditions/links have corresponding select-field**
- [ ] **Proper pagination for large datasets**

## Transition Validation

- [ ] **MANDATORY: Every transition MUST have `<default-response>`** - Required by Moqui Framework
- [ ] **Use conditional-response for complex flows** but ALWAYS include default-response
- [ ] **Proper service-call syntax with in-map/out-map**

## Quick Syntax Check Commands

```bash
# Check for invalid attributes (run in screen/ directory)
grep -r 'use-iterator="true"' .
grep -r '<limit from=' .
grep -r '<offset from=' .
grep -r 'header-field.*show=' .
grep -r 'hidden.*default=' .

# Check for missing mandatory list attribute in form-list
grep -r '<form-list[^>]*>' . | grep -v 'list=' | grep -v '//'

# Check for empty columns (columns without field-ref)
grep -B2 -A2 '<column>' . | grep -v 'field-ref' | grep -v '//'

# Check for recordCount usage without definition in actions
grep -l 'recordCount' . | xargs -I {} sh -c 'grep -L "<entity-find-count.*count-field=\"recordCount\"" {} | grep recordCount {}'

# Check for transitions missing default-response
grep -r '<transition[^>]*>' . | grep -v 'default-response' | grep -v '//'
```

## Emergency Fixes

If you encounter these errors in existing code:

```bash
# Remove use-iterator attributes
sed -i 's/ use-iterator="true"//g' file.xml

# Convert limit tags to attributes (manual review required)
# Convert offset tags to attributes (manual review required)
```

**⚠️ IMPORTANT**: Always manually review automated fixes before committing.

## References

- **Complete Guide**: `.agent-os/moqui-screen-references/syntax-anti-patterns.md`
- **Framework Guide**: `.agent-os/framework-guide.md` (Screen Widget Best Practices section)
- **Screen Patterns**: `.agent-os/moqui-screen-patterns.md`

---

**Remember**: When in doubt, refer to the complete syntax-anti-patterns.md guide for detailed examples and explanations.