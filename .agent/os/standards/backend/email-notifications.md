# Email and Notification Patterns

Sending emails via templates and handling email ECAs.

## EmailTemplate Pattern

```xml
<!-- Define template in data file -->
<moqui.basic.email.EmailTemplate emailTemplateId="ORDER_CONFIRMATION"
    description="Order Confirmation Email"
    emailServerId="MAIN"
    fromAddress="orders@example.com"
    subject="Order ${orderId} Confirmation"
    bodyScreenLocation="component://example/template/email/OrderConfirmation.html"/>
```

## Sending Email from Service

```xml
<service-call name="org.moqui.impl.EmailServices.send#EmailTemplate">
    <field-map field-name="emailTemplateId" value="ORDER_CONFIRMATION"/>
    <field-map field-name="toAddresses" from="customerEmail"/>
    <field-map field-name="bodyParameters" from="[orderId:orderId, orderDate:orderDate, items:orderItems]"/>
</service-call>
```

## Email with Attachments

```xml
<service-call name="org.moqui.impl.EmailServices.send#EmailTemplate">
    <field-map field-name="emailTemplateId" value="INVOICE_EMAIL"/>
    <field-map field-name="toAddresses" from="customerEmail"/>
    <field-map field-name="bodyParameters" from="context"/>
    <field-map field-name="attachments" from="[
        [fileName:'Invoice.pdf', screenPath:'component://example/screen/InvoicePdf.xml',
         screenRenderMode:'xsl-fo'],
        [fileName:'Terms.pdf', attachmentLocation:'component://example/template/Terms.pdf']
    ]"/>
</service-call>
```

## Email ECA (Event-Condition-Action)

Process incoming emails automatically:

```xml
<!-- In eecas.xml -->
<eeca name="ProcessSupportEmail" run-on-receive="true">
    <condition><expression>fields.subject?.contains('[SUPPORT]')</expression></condition>
    <actions>
        <service-call name="example.SupportServices.create#TicketFromEmail"
            in-map="[fields:fields, bodyPartList:bodyPartList]"/>
    </actions>
</eeca>
```

## Email Service Interface

Implement custom email handling:

```xml
<service verb="process" noun="CustomEmail">
    <implements service="org.moqui.EmailServices.process#EmailEca"/>
    <actions>
        <!-- Access email fields -->
        <set field="fromAddress" from="fields.from"/>
        <set field="subject" from="fields.subject"/>
        <set field="body" from="bodyPartList?.find { it.contentType?.startsWith('text/plain') }?.contentText"/>

        <!-- Process attachments -->
        <iterate list="bodyPartList" entry="part">
            <if condition="part.filename">
                <!-- Handle attachment -->
            </if>
        </iterate>
    </actions>
</service>
```

## Email Message Tracking

```xml
<!-- Query sent emails -->
<entity-find entity-name="moqui.basic.email.EmailMessage" list="emailList">
    <econdition field-name="statusId" value="ES_SENT"/>
    <econdition field-name="toAddresses" operator="like" value="%${email}%"/>
    <order-by field-name="sentDate" descending="true"/>
</entity-find>
```

## Email Status Flow

| Status | Description |
|--------|-------------|
| `ES_DRAFT` | Email created but not ready |
| `ES_READY` | Ready to send |
| `ES_SENDING` | Currently being sent |
| `ES_SENT` | Successfully sent |
| `ES_BOUNCED` | Delivery failed |
| `ES_CANCELLED` | Cancelled before sending |

## Key Rules

1. **Always use EmailTemplate** - Don't hardcode email content in services
2. **Track with createEmailMessage=true** - Default, creates EmailMessage record
3. **Use bodyParameters** - Pass data to template as a Map
4. **Screen attachments** - Use `screenPath` + `screenRenderMode` for dynamic PDFs
5. **Handle bounces** - Monitor ES_BOUNCED status for delivery issues