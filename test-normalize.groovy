import org.moqui.json.JsonSlurper
import groovy.json.JsonOutput

def ec = org.moqui.Moqui.getExecutionContext()
def jsonFile = new File("runtime/component/moqui-ai/TestBlueprint.json")
def blueprintMap = new JsonSlurper().parseText(jsonFile.text)

def result = ec.service.sync().name("moquiai.JsonServices.normalize#BlueprintKeys")
    .parameter("blueprintMap", blueprintMap)
    .call()

if (result.success) {
    println "Normalization successful: " + JsonOutput.prettyPrint(JsonOutput.toJson(result.normalizedMap))
} else {
    println "Normalization failed: " + result.message
}
ec.destroy()
