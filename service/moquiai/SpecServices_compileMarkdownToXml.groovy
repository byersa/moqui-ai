import org.moqui.context.ExecutionContext

ExecutionContext ec = context.ec

// 1. Get location
def mdLocation = "component://${componentName}/${specPath}"
def mdRef = ec.resource.getLocationReference(mdLocation)
if (!mdRef.getExists()) {
    ec.message.addError("Spec not found: ${mdLocation}")
    return
}
def mdContent = mdRef.getText()

// 2. Extract Title from MD header (# Title)
def titleMatch = (mdContent =~ /(?m)^#\s+(.*)/)
def title = titleMatch.find() ? titleMatch.group(1).trim() : specPath.split('/').last().replace(".md", "")
def screenName = specPath.split('/').last().replace(".md", "")

// 3. Resolve Macros from Registry
def registryResult = ec.service.sync().name("moquiai.RegistryServices.get#EffectiveRegistry").parameter("app", componentName).call()
def macros = registryResult.registry.macros

// 4. Generate Groovy DSL
def dslFields = ""
def matcher = mdContent =~ /\[(\w+(-\w+)*):\s*([^\]]+)\]/
matcher.each { match ->
    def macroName = match[1].replace('-', '_')
    def paramName = match[3].trim()
    // Even if not in registry, we'll allow it for now if mentioned as a tag
    dslFields += "    ${macroName}(name: \"${paramName}\")\n"
}

// Minimalistic extraction of other tags if they look like subscreens
def subMatcher = mdContent =~ /<(\w+(-\w+)*)(\s+type="([^"]+)")?\/?>/
while (subMatcher.find()) {
    def tagName = subMatcher.group(1).replace('-', '_')
    def type = subMatcher.group(4)
    if (type) {
        dslFields += "    ${tagName}(type: \"${type}\")\n"
    } else {
        dslFields += "    ${tagName}()\n"
    }
}

// 5. Inject back into MD
def mdFile = new File(ec.factory.runtimePath, "component/${componentName}/${specPath}")
def originalMd = mdFile.text

def dslStr = """screen("${screenName}") {
    label(text: "${title}", type: "h1")
${dslFields}}"""

def groovyBlock = "```groovy\n${dslStr}\n```"
def updatedMd
if (originalMd.contains("```groovy")) {
    updatedMd = originalMd.replaceFirst(/(?s)```groovy.*?```/, groovyBlock)
} else {
    updatedMd = originalMd + "\n\n## Generated DSL\n" + groovyBlock
}

mdFile.text = updatedMd

// 6. Final success
context.success = true
context.mdPath = mdFile.absolutePath
