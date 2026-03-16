# Date/Time Handling

Patterns for dates and timestamps in services.

## Current Time
```xml
<set field="now" from="ec.user.nowTimestamp"/>
```

## Parsing User Input
```xml
<set field="parsedDate" from="ec.l10n.parseTimestamp(inputString, 'yyyy-MM-dd HH:mm')"/>
```

## Formatting for Display
```xml
<set field="formatted" from="ec.l10n.format(timestamp, 'yyyy-MM-dd')"/>
```

## Date Arithmetic (Use Calendar)
```groovy
Calendar cal = Calendar.instance
cal.set(Calendar.HOUR_OF_DAY, 0)
cal.set(Calendar.MINUTE, 0)
cal.set(Calendar.SECOND, 0)
cal.set(Calendar.MILLISECOND, 0)
cal.add(Calendar.DAY_OF_MONTH, 1)
Timestamp tomorrow = new Timestamp(cal.timeInMillis)
```

## Common Patterns
| Task | Approach |
|------|----------|
| Get now | `ec.user.nowTimestamp` |
| Parse string | `ec.l10n.parseTimestamp()` |
| Format output | `ec.l10n.format()` |
| Add/subtract days | `Calendar.add()` |
| Start of day | `Calendar.set(HOUR_OF_DAY, 0)...` |