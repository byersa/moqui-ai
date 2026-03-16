# Rendering Modes

### URL Prefixes

| Prefix | Mode | Framework | Recommended |
|--------|------|-----------|-------------|
| `/qapps` | Quasar SPA | Vue.js + Quasar | **Yes (default)** |
| `/vapps` | Vue SPA | Vue.js | Development |
| `/apps` | Server-rendered | jQuery/Bootstrap | Legacy |

### Default Mode: Quasar (`/qapps`)

**All development and production should target `/qapps`:**

```
Production URL: https://example.com/qapps/MyApp/Dashboard
Development URL: http://localhost:8080/qapps/MyApp/Dashboard
```

### REST API Base Path

**REST APIs always use `/apps` regardless of UI rendering mode:**

```javascript
// In Quasar/Vue screens
confBasePath: '/apps'      // REST API calls
confLinkBasePath: '/qapps' // UI navigation
```

### Template Extensions

| Mode | Extension | Template |
|------|-----------|----------|
| Quasar | `.qvt` | WebrootVue.qvt.ftl |
| Vue | `.vuet` | WebrootVue.vuet.ftl |
| Server | `.ftl` | Standard FreeMarker |

### Theme Configuration

| Mode | Theme ID |
|------|----------|
| `/qapps` | `STT_INTERNAL_QUASAR` |
| `/vapps`, `/apps` | `STT_INTERNAL` |

### Quasar Features

- Material Design components (`q-btn`, `q-input`)
- Drawer/sidebar navigation
- Dark mode toggle
- Rich notification system
- Mobile-responsive layout

### Non-Browser Output Modes

| Format | Usage | Parameter |
|--------|-------|-----------|
| CSV | Export to CSV | `?renderMode=csv` |
| XLSX | Excel export | `?renderMode=xlsx` |
| PDF | PDF generation | `?renderMode=pdf` |
| XML | XML export | `?renderMode=xml` |

```
Export URL: /qapps/MyApp/Orders/OrderList?renderMode=csv
```

### Playwright Testing

**Test against production rendering mode (`/qapps`):**

```javascript
// Playwright test
await page.goto('http://localhost:8080/qapps/MyApp/Dashboard');
```

### Production vs Development

| Aspect | Production | Development |
|--------|------------|-------------|
| Libraries | Minified, bundled | Source files |
| Features | Optimized | Hot reload |
| Detection | `instance_purpose` property | Default |

### URL Path Structure

```
/{prefix}/{app-name}/{screen-path}

Examples:
/qapps/MyApp/Orders/FindOrder
/qapps/MyApp/Customers/CustomerDetail?customerId=CUST001
```

### Screen Configuration Files

| File | Purpose |
|------|---------|
| `webroot/screen/webroot/qapps.xml` | Quasar config |
| `webroot/screen/webroot/vapps.xml` | Vue config |
| `webroot/screen/webroot/apps.xml` | Server-rendered config |

### Best Practices

- Use `/qapps` for all production deployments
- Test E2E with same rendering mode as production
- REST APIs always go through `/apps`
- Consider mobile UX (Quasar provides best experience)
- Check theme compatibility for customizations

### Mode Comparison

| Aspect | /qapps | /vapps | /apps |
|--------|--------|--------|-------|
| Rendering | Client | Client | Server |
| Page transitions | SPA | SPA | Full reload |
| UI Framework | Quasar | Custom Vue | Bootstrap |
| Mobile UX | Material adaptive | Responsive | Basic |
| Bundle size | Larger | Medium | Smaller |
| Development | More complex | Moderate | Simpler |

### When to Use Each Mode

- **`/qapps`**: Production applications, enterprise features, mobile support
- **`/vapps`**: Custom Vue development, flexibility needed
- **`/apps`**: Legacy support, simple UX requirements
