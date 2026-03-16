# Date/Time Format Standardization in Moqui Framework

## Problem Statement

The acme-erp project had inconsistent timestamp formats throughout the codebase:
- **Manual hardcoding**: 44+ instances of explicit `format="dd/MM/yyyy HH:mm"` in screens and services
- **Framework default**: Uses ISO format `yyyy-MM-dd HH:mm` when no format specified
- **Maintenance burden**: Any format changes required updating multiple files
- **User experience**: Inconsistent date/time presentation across the application

## Research Findings

### Moqui Framework String-to-Timestamp Conversion Points

**Key Framework File**: `/framework/src/main/groovy/org/moqui/impl/service/ParameterInfo.java`
- **Method**: `convertType()` - handles service parameter type conversion
- **Timestamp Conversion**: Line 190: `converted = eci.l10nFacade.parseTimestamp(valueStr, format);`
- **Format Source**: Line 88: `format = parameterNode.attribute("format");`

**L10nFacadeImpl**: `/framework/src/main/groovy/org/moqui/impl/context/L10nFacadeImpl.java`
- **Default Formats**: Lines 305-309 define hardcoded ISO timestamp formats
- **Fallback Logic**: When no format specified, tries multiple ISO patterns
- **No Configuration Integration**: Original implementation doesn't check user preferences

### Moqui Preference System Architecture

**Preference Resolution Order (from highest to lowest priority)**:
1. **Individual User Preference** - `moqui.security.UserPreference`
2. **User Group Preference** - `moqui.security.UserGroupPreference`
3. **System Property** - `default-property` in MoquiConf.xml
4. **Framework Default** - Hardcoded fallbacks

**Access Methods**:
- `ec.user.getPreference(key)` - Full preference hierarchy resolution
- `eci.ecfi.getProperty(key)` - Only system properties (bypasses user preferences)

## Solution Architecture

### Framework Modification Approach

**Modified**: `ParameterInfo.java` - Enhanced DATE, TIME, and TIMESTAMP cases:

```java
case TIMESTAMP:
    String timestampFormat = format;
    // Use user preference DefaultDateTimeFormat if no explicit format is specified
    if (timestampFormat == null || timestampFormat.isEmpty()) {
        timestampFormat = eci.user.getPreference("DefaultDateTimeFormat");
    }
    converted = eci.l10nFacade.parseTimestamp(valueStr, timestampFormat);
    // ... error handling
    break;
```

**Benefits**:
- ✅ Respects user preferences when available
- ✅ Falls back to system-wide defaults
- ✅ Maintains backward compatibility with explicit formats
- ✅ Consistent with screen widget behavior

### Configuration Strategy Decision Matrix

| Business Requirement | Implementation Approach | Configuration Method |
|----------------------|-------------------------|---------------------|
| **Organizational Standard** - All users must use same format | System Properties | `<default-property name="DefaultDateTimeFormat" value="dd/MM/yyyy HH:mm"/>` |
| **User Customization** - Users can choose their preferred format | User Group Preferences | `<user-group-preference user-group-id="ALL_USERS" preference-key="DefaultDateTimeFormat" preference-value="dd/MM/yyyy HH:mm"/>` |
| **Hybrid** - Organizational default with user override capability | Both | System property as fallback + User group preference as default |

### Example Implementation

**Chosen Approach**: User Group Preferences (allows user customization)

**MoquiConf.xml Configuration**:
```xml
<!-- User Preferences Configuration for Default Date/Time Formats -->
<user-facade>
    <!-- Set system-wide default format preferences for all users -->
    <user-group-preference user-group-id="ALL_USERS" preference-key="DefaultTimeFormat" preference-value="HH:mm"/>
    <user-group-preference user-group-id="ALL_USERS" preference-key="DefaultDateFormat" preference-value="dd/MM/yyyy"/>
    <user-group-preference user-group-id="ALL_USERS" preference-key="DefaultDateTimeFormat" preference-value="dd/MM/yyyy HH:mm"/>
</user-facade>
```

## Implementation Impact

### Service Parameters
**Before**:
```xml
<parameter name="estimatedStartDate" type="Timestamp" format="dd/MM/yyyy HH:mm"/>
```

**After**:
```xml
<parameter name="estimatedStartDate" type="Timestamp"/>
<!-- Framework automatically uses user preference DefaultDateTimeFormat -->
```

### Screen Fields
**Before**:
```xml
<date-time type="date-time" format="dd/MM/yyyy HH:mm"/>
```

**After**:
```xml
<date-time type="date-time"/>
<!-- Framework automatically uses user preference DefaultDateTimeFormat -->
```

### Explicit Format Override
When specific formatting is required, explicit formats still work:
```xml
<parameter name="logTimestamp" type="Timestamp" format="yyyy-MM-dd HH:mm:ss.SSS"/>
```

## Testing and Validation

### Test Cases
1. **Service Parameter Parsing**: Verify Timestamp parameters without format attribute use DefaultDateTimeFormat
2. **Screen Field Display**: Confirm date-time fields respect user preferences
3. **User Override**: Test individual user preference takes priority over group default
4. **Fallback Behavior**: Ensure framework defaults work when no preferences set

### Verification Methods
- Remove explicit format attributes from test services/screens
- Verify Chilean format (`dd/MM/yyyy HH:mm`) is used automatically
- Test with different user preference values
- Confirm backward compatibility with existing explicit formats

## Migration Strategy

### Phase 1: Framework Enhancement ✅
- Modify ParameterInfo.java to check user preferences
- Configure user group preferences in MoquiConf.xml

### Phase 2: Selective Format Removal
- **High Priority**: Remove redundant explicit formats that match default
- **Low Priority**: Keep explicit formats where different formatting is intentional
- **Documentation**: Update code comments to reference preference system

### Phase 3: User Training
- Document user preference customization options
- Provide examples of personal format overrides
- Create admin guide for organizational format management

## Best Practices

### When to Use Explicit Formats
- **Log timestamps**: Usually need precise formats with milliseconds
- **API responses**: May need specific ISO formats for interoperability
- **Import/Export**: External system compatibility requirements
- **Special displays**: User-facing formats that differ from data entry formats

### When to Use Default Preferences
- **Data entry forms**: User should use their preferred format
- **General display**: Standard business document formatting
- **Service parameters**: Internal data processing
- **Most screen fields**: Consistent user experience

## Common Pitfalls

### `type="date"` Widget with `type="Timestamp"` Service Parameter

A `date-time type="date"` widget sends a date-only value (e.g., `25/02/2026`). If the service parameter declares `type="Timestamp"`, `ParameterInfo.convertType` parses it using the `DefaultDateTimeFormat` preference (`dd/MM/yyyy HH:mm`), which fails because there is no time component. The widget's `format` attribute only controls display, not parsing.

**Fix**: Add `format="dd/MM/yyyy"` to the **service parameter** to match the date-only input:
```xml
<parameter name="fromDate" type="Timestamp" format="dd/MM/yyyy" required="true"/>
```

### Inclusive thruDate (End-of-Day Adjustment)

When a service receives a date-only `thruDate` from a `type="date"` widget, the value is midnight (`00:00:00.000`), making the date effectively exclusive. Adjust to end-of-day in the service to make it inclusive:

```xml
<if condition="thruDate">
    <set field="thruDate" from="new java.sql.Timestamp(thruDate.time + (24L * 60 * 60 * 1000) - 1)"/>
</if>
```

See also: `framework-guide.md` § Date/Time Format Standardization → Common Pitfall / Inclusive thruDate Pattern

## Future Enhancements

### Potential Framework Improvements
1. **Screen Widget Integration**: Extend preference support to more widget types
2. **Locale-Aware Defaults**: Automatic format selection based on user locale
3. **Format Validation**: Preference validation during system startup
4. **Migration Tools**: Utilities to identify and remove redundant explicit formats

### Component-Level Features
1. **User Preference UI**: Screen for users to customize their date/time formats
2. **Admin Dashboard**: Organization-wide format policy management
3. **Format Preview**: Show examples of formats before saving preferences
4. **Bulk Format Migration**: Tools to standardize existing data

## Technical Reference

### Framework Files Modified
- `framework/src/main/groovy/org/moqui/impl/service/ParameterInfo.java`
  - Enhanced DATE, TIME, TIMESTAMP parameter conversion
  - Added user preference fallback logic

### Configuration Files
- `runtime/component/acme-erp/MoquiConf.xml`
  - Added user group preferences for date/time formats
  - Maintained system property fallbacks

### Related Framework Classes
- `L10nFacadeImpl.java` - Core date/time parsing implementation
- `ServiceDefinition.java` - Service parameter validation
- `UserFacadeImpl.java` - User preference resolution

### Preference Keys Used
- `DefaultTimeFormat` - Time-only fields (HH:mm)
- `DefaultDateFormat` - Date-only fields (dd/MM/yyyy)
- `DefaultDateTimeFormat` - Combined date/time fields (dd/MM/yyyy HH:mm)

---

**Last Updated**: 2025-09-24
**Framework Version**: Moqui 3.x
**Component**: acme-erp
**Status**: Implemented and Tested