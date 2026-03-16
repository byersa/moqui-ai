# Moqui Rendering Modes

**Standards Reference**: For declarative conventions, see:
- `standards/frontend/rendering-modes.md` - URL prefixes, default SPA mode

---

## Overview

Moqui provides three distinct rendering modes for web applications:

| URL Prefix | Rendering Mode | Framework | Best For |
|------------|----------------|-----------|----------|
| `/apps` | Server-rendered HTML | jQuery/Bootstrap | Traditional apps, simple UX |
| `/vapps` | Vue.js SPA | Vue.js | Modern SPA, flexibility |
| `/qapps` | Quasar SPA | Vue.js + Quasar | Enterprise SPA, Material Design |

**Default Mode**: **Quasar (`/qapps`) is the default and recommended rendering mode**.

---

## URL Path Structure

All three rendering modes share the same application structure after the prefix:

```
/{prefix}/{app-name}/{screen-path}

Examples:
/apps/MyApp/Customers/FindCustomer
/vapps/MyApp/Customers/FindCustomer
/qapps/MyApp/Customers/FindCustomer
```

**Important**: REST API calls always use `/apps` as the base path regardless of the rendering mode being used for the UI.

---

## /apps - Server-Rendered HTML (Traditional)

### Configuration
- **Screen file**: `runtime/base-component/webroot/screen/webroot/apps.xml`
- **Theme**: `STT_INTERNAL` (Standard Internal Applications)

### Use Cases
- Legacy applications
- Simple UX requirements
- Environments with limited JavaScript support
- Quick prototyping

### Template Extension
Server-rendered screens use FreeMarker templates (`.ftl`) directly.

---

## /vapps - Vue.js SPA

### Configuration
- **Screen file**: `runtime/base-component/webroot/screen/webroot/vapps.xml`
- **Theme**: `STT_INTERNAL`
- **Template**: `WebrootVue.vuet.ftl`

### Configuration Values
```javascript
confBasePath: '/apps'      // REST API base path
confLinkBasePath: '/vapps' // Navigation link path
```

### Template Extension
Vue screens use `.vuet` extension (Vue template format).

### Use Cases
- Modern web applications
- Flexible UI customization
- Development/debugging (hot reload in dev mode)

---

## /qapps - Quasar SPA (Recommended)

### Configuration
- **Screen file**: `runtime/base-component/webroot/screen/webroot/qapps.xml`
- **Theme**: `STT_INTERNAL_QUASAR`
- **Template**: `WebrootVue.qvt.ftl`

### Configuration Values
```javascript
confBasePath: '/apps'      // REST API base path
confLinkBasePath: '/qapps' // Navigation link path
confDarkMode: 'false'      // Dark mode preference
confLeftOpen: 'false'      // Drawer state
```

### Template Extension
Quasar screens use `.qvt` extension (Quasar-Vue template format).

### Features
- Quasar Layout system (`q-layout`, `q-header`, `q-drawer`)
- Material Design icons and components
- Advanced drawer/sidebar navigation
- Rich notification system with `q-banner`
- Dark mode toggle

### Use Cases
- Enterprise applications
- Rich UI requirements
- Material Design consistency
- Mobile-responsive applications

---

## Comparative Analysis

| Aspect | /apps | /vapps | /qapps |
|--------|-------|--------|--------|
| **Rendering** | Server-side | Client-side | Client-side |
| **Page Transitions** | Full reload | Dynamic SPA | Dynamic SPA |
| **State Management** | Server session | Client-side | Client-side |
| **UI Framework** | Bootstrap | Custom Vue | Quasar Material |
| **Dark Mode** | Limited | Supported | Full Quasar |
| **Mobile UX** | Basic responsive | Responsive | Material adaptive |
| **Bundle Size** | Smaller | Medium | Larger |

---

## Playwright Testing Considerations

When writing E2E tests with Playwright, the rendering mode affects element selectors:

### URL Configuration
```javascript
// For Quasar (recommended)
await page.goto('http://localhost:8080/qapps/MyApp/Screen');

// For Vue
await page.goto('http://localhost:8080/vapps/MyApp/Screen');

// For traditional
await page.goto('http://localhost:8080/apps/MyApp/Screen');
```

### Element Differences
- **Quasar**: Uses Quasar-specific components (`q-btn`, `q-input`, etc.)
- **Vue/Apps**: Uses standard HTML elements with Bootstrap classes

### Recommended Approach
Test against `/qapps` for production-representative testing.

---

## Non-Browser Output Modes

| Format | Usage | Notes |
|--------|-------|-------|
| **CSV** | `?renderMode=csv` | Export lists to CSV |
| **XLSX** | `?renderMode=xlsx` | Excel spreadsheet export |
| **PDF** | `?renderMode=pdf` | PDF document generation |
| **XML** | `?renderMode=xml` | XML data export |
| **JSON** | REST API | Data-only responses |

These are triggered via query parameters on any screen URL.

---

## Production vs Development Mode

All rendering modes behave differently based on `System.getProperty("instance_purpose")`:

| Mode | Libraries | Features |
|------|-----------|----------|
| **Production** | Minified, bundled | Optimized performance |
| **Development** | Source files | Hot reload, debugging |

---

## Configuration Files Reference

| File | Purpose |
|------|---------|
| `webroot/screen/webroot/apps.xml` | Traditional rendering config |
| `webroot/screen/webroot/vapps.xml` | Vue.js rendering config |
| `webroot/screen/webroot/qapps.xml` | Quasar rendering config |
| `webroot/screen/webroot.xml` | Root screen with default subscreen |
| `webroot/data/WebrootQuasarThemeData.xml` | Quasar theme data |

---

## Best Practices

1. **Use `/qapps` for production** - Most feature-rich and actively maintained
2. **Test with the same mode used in production** - Avoid rendering mode mismatches
3. **REST APIs always use `/apps`** - Even when UI uses `/vapps` or `/qapps`
4. **Consider mobile UX** - Quasar provides best mobile experience
5. **Check theme compatibility** - Some customizations are theme-specific