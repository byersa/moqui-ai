# Moqui Vue 3 Migration Patterns

This document captures the essential patterns and technical decisions made during the migration of Moqui's blueprint-based UI components to Vue 3 and Quasar 2.

## 1. Reactivity in Blueprint Components

In Vue 3, the data context used by the `DeterministicVueRenderer` blueprints must be explicitly reactive to ensure UI updates.

### Pattern: Reactive Context Wrapper
In `BlueprintClient.js`, the context should be initialized as:
```javascript
this.context = Vue.reactive(node.fieldsInitial || {});
```

### Problem: Late-Bound Fields
Quasar components often write to fields that weren't present in the initial server-side `fieldsInitial` payload. Vue 3's `reactive()` cannot track properties added after initialization unless they are nested within an object or handled via a Proxy.

**Solution**: Use a Proxy to ensure all field access is tracked.
```javascript
const proxyContext = new Proxy(this.context, {
    get(target, prop) {
        if (!(prop in target)) target[prop] = null;
        return target[prop];
    }
});
```

---

## 2. Component Binding Patterns (Vue 2 to Vue 3)

### Migration of `v-model`
In Vue 3, using `v-model="modelValue"` inside a component is an **anti-pattern** because props are immutable. This causes "double keystrokes" and input locking as the child component attempts and fails to update the parent's prop directly.

**Correct Pattern**:
```javascript
// Change from:
template: '<q-select v-model="modelValue" ...>'

// To:
template: '<q-select :model-value="modelValue" @update:model-value="$emit(\'update:modelValue\', $event)" ...>'
```

### Date/Time Handling
Quasar's `q-date` and `q-time` components can fail silently or log "Invalid Date" if passed an empty string `""` instead of `null`.

**Solution**: Use a computed property with proper null-handling:
```javascript
computed: {
    dateModel: {
        get() { return this.modelValue || null; },
        set(val) { this.$emit('update:modelValue', val || null); }
    }
}
```

---

## 3. Entity Resolution for Organizations

The `OrganizationDetail` entity often used in legacy code may not exist in standard Mantle installations.

**Correct Mantle Pattern**: 
Use `mantle.party.PartyDetail` and filter by `partyTypeEnumId`.

**XML Definition**:
```xml
<drop-down allow-empty="true">
    <entity-options key-field-name="partyId" text="${organizationName}">
        <entity-find entity-name="mantle.party.PartyDetail">
            <econdition field-name="partyTypeEnumId" value="PtyOrganization"/>
            <order-by field-name="organizationName"/>
        </entity-find>
    </entity-options>
</drop-down>
```

---

## 4. Port Stability for WebMCP

Tools like `webmcp.js` should default to port `4797` to align with the standard AI-Pilot configuration, ensuring consistent subagent connectivity without manual token adjustments.
