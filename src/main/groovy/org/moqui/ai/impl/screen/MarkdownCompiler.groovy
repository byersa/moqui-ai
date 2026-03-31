import groovy.xml.MarkupBuilder
import org.moqui.impl.screen.ScreenDefinition

// 1. Setup the XML Builder
StringWriter writer = new StringWriter()
MarkupBuilder xml = new MarkupBuilder(writer)
xml.setDoubleQuotes(true)

// 2. The DSL "Harness" (The Padded Room)
// We define the words the AI is allowed to use in its Groovy blocks.
def compileDslBlock = { String dslCode ->
    Binding binding = new Binding([
        xml: xml,
        // Our HIPAA Harness for safe CDATA logic
        logic: { String code ->
            xml.script {
                xml.mkp.yieldUnescaped("<![CDATA[\n${code.trim()}\n]]>")
            }
        }
    ])
    
    // Evaluate the block. MarkupBuilder handles the XML generation.
    GroovyShell shell = new GroovyShell(binding)
    shell.evaluate(dslCode)
}

try {
    // 3. THE EXTRACTOR: Rip the Groovy out of the Markdown
    // Matches: ```groovy [code] ```
    def matcher = dslScript =~ /(?s)```groovy\s+(.*?)\s+```/
    
    boolean foundBlocks = false
    while (matcher.find()) {
        foundBlocks = true
        compileDslBlock(matcher.group(1))
    }

    if (!foundBlocks) {
        isValid = false
        errorMessage = "No ```groovy blocks found in the Spec MD."
        return
    }

    String generatedXml = writer.toString()

    // 4. THE PADDOCK: Pre-flight Validation
    // We attempt a dry-run parse to catch 'Unexpected input' errors
    try {
        ec.screen.makeRender().withScreen(generatedXml).render() 
        isValid = true
    } catch (Exception e) {
        isValid = false
        // This error goes back to the AI Agent to trigger self-correction
        errorMessage = "Moqui Parsing Error: ${e.message}"
    }

    xmlOutput = generatedXml
    
    // 5. Commit to Disk if valid
    if (isValid && targetLocation) {
        ec.resource.getLocationReference(targetLocation).putObject(generatedXml)
    }

} catch (Exception e) {
    isValid = false
    errorMessage = "Harness Exception: ${e.message}"
}