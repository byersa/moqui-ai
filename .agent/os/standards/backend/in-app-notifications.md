# In-App Notification Patterns

Sending in-app notifications using Moqui's `NotificationMessage` API.

## NotificationTopic Seed Data

```xml
<!-- Define topic in seed data file -->
<moqui.security.user.NotificationTopic topic="OrderStatusChanged"
        description="Order Status Changed Notification"
        typeString="info"
        showAlert="Y"
        persistOnSend="Y"
        receiveNotifications="Y"
        emailNotifications="N"
        emailTemplateId="NOTIFICATION"
        titleTemplate="Order Updated: ${orderId}"
        linkTemplate="/app/ViewOrder?orderId=${orderId}"/>
```

| Attribute | Purpose |
|-----------|---------|
| `typeString` | Alert style: `info`, `success`, `warning`, `danger` |
| `showAlert` | Show as pop-up alert (`Y`/`N`) |
| `persistOnSend` | Store in database for later retrieval (`Y`/`N`) |
| `receiveNotifications` | Users can opt in/out of this topic (`Y`/`N`) |
| `emailNotifications` | Also send as email (`Y`/`N`) |
| `titleTemplate` | GString template with message map variables |
| `linkTemplate` | Click-through URL template |

## Sending Notifications from Services

### Standard Pattern

```xml
<service verb="send" noun="StatusChangeNotification">
    <in-parameters>
        <parameter name="orderId" required="true"/>
    </in-parameters>
    <out-parameters>
        <parameter name="notificationSent" type="Boolean"/>
    </out-parameters>
    <actions>
        <!-- 1. Collect recipient userIds -->
        <set field="userIdSet" from="new HashSet()"/>

        <entity-find entity-name="moqui.security.UserGroupMember" list="members">
            <econdition field-name="userGroupId" value="ORDER_MANAGERS"/>
            <date-filter/>
        </entity-find>
        <iterate list="members" entry="member">
            <script>userIdSet.add(member.userId)</script>
        </iterate>

        <!-- 2. Remove current user (no self-notification) -->
        <script>userIdSet.remove(ec.user.userId)</script>

        <!-- 3. Guard: skip if no recipients -->
        <if condition="userIdSet.isEmpty()">
            <set field="notificationSent" from="false"/>
            <return/>
        </if>

        <!-- 4. Build and send notification (CDATA script block) -->
        <script><![CDATA[
            def nm = ec.makeNotificationMessage()
            nm.topic("OrderStatusChanged")
            nm.type("info")
            nm.title("Order Updated: ${orderId}")
            nm.link("/app/ViewOrder?orderId=${orderId}")
            nm.message([
                orderId: orderId,
                statusId: newStatusId,
                updatedBy: ec.user.username
            ])
            nm.userIds(userIdSet)
            nm.showAlert(true)
            nm.persistOnSend(true)
            nm.send()
        ]]></script>

        <set field="notificationSent" from="true"/>
    </actions>
</service>
```

### NotificationMessage API Reference

| Method | Purpose |
|--------|---------|
| `.topic(String)` | Must match a `NotificationTopic.topic` value |
| `.type(String)` | Override topic's `typeString`: `info`, `success`, `warning`, `danger` |
| `.title(String)` | Override topic's `titleTemplate` |
| `.link(String)` | Override topic's `linkTemplate` |
| `.message(Map)` | Data payload; variables available in `titleTemplate`/`linkTemplate` |
| `.userIds(Set)` | Recipients (HashSet of userId strings) |
| `.showAlert(boolean)` | Pop-up in browser |
| `.alertNoAutoHide(boolean)` | Keep alert visible until dismissed |
| `.persistOnSend(boolean)` | Store in database for notification history |
| `.send()` | Dispatch the notification |

## Key Rules

1. **Always define a NotificationTopic** in seed data before sending
2. **Use HashSet for userIds** to avoid duplicate notifications
3. **Remove current user** from recipients to prevent self-notification
4. **Guard against empty recipients** before building the message
5. **Use CDATA script block** for the notification builder (follows XML DSL vs script guidance)
6. **Message map keys** must match variables used in `titleTemplate` and `linkTemplate`

## Difference from Email Notifications

| Aspect | In-App (NotificationMessage) | Email (EmailTemplate) |
|--------|------------------------------|----------------------|
| Delivery | Browser pop-up + history | Email inbox |
| API | `ec.makeNotificationMessage()` | `send#EmailTemplate` service |
| Recipients | `userIds` (Set) | `toAddresses` (String) |
| Template | `titleTemplate` on topic | `bodyScreenLocation` on template |
| Persistence | `persistOnSend` flag | Always creates `EmailMessage` record |
