# CDATA Script Blocks

Use `<![CDATA[...]]>` to prevent XML parser issues with Groovy code.

## When Required
- Groovy containing `<`, `>`, `&` operators
- Multi-line scripts with complex logic
- Import statements

## Size Threshold
- **<10 lines**: CDATA in service file
- **10+ lines**: External `.groovy` file

```xml
<!-- CDATA for XML-unsafe operators -->
<script><![CDATA[
    if (value > threshold && condition < limit) {
        result = value
    }
]]></script>

<!-- External for complex logic -->
<service verb="process" noun="Data"
         type="script"
         location="component://example/service/ProcessData.groovy">
```

## Common Triggers for CDATA
- Comparison operators (`<`, `>`, `<=`, `>=`)
- Logical AND (`&&`)
- Collections with generics (`List<String>`)
- XML/HTML string manipulation