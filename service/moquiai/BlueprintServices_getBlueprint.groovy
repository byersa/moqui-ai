import org.moqui.context.ExecutionContext
import groovy.json.JsonSlurper

ExecutionContext ec = context.ec

def subFolder = "screen"
def fileName = screenPath
if (screenPath.contains("/")) {
    def parts = screenPath.split("/", 2)
    subFolder = parts[0]
    fileName = parts[1]
}

ec.logger.info("MCE Load: requesting ${componentName}/${subFolder}/${fileName}")

def targetComp = componentName
def targetBase = fileName
def targetSrv = null
    ec.logger.info("MCE: getBlueprint, subFolder: ${subFolder}")
if (subFolder == "service" && fileName.contains(".")) {
    def dots = fileName.split("\\.")
    if (dots.length >= 3) {
        targetComp = dots[0]
        targetBase = dots[1]
        targetSrv = dots[2]
    }
}
    ec.logger.info("MCE: getBlueprint, targetSrv : ${targetSrv}")

// 1. Mandatory Conversion from XML (Primary Source of Truth)
def genResult = ec.service.sync().name("moquiai.ProjectServices.generate#BlueprintFromXml")
    .parameters([componentName: targetComp, artifactName: targetBase, type: subFolder])
    .call()

if (genResult && genResult.blueprint) {
    def raw = genResult.blueprint
    if (targetSrv) {
        // Filter for specific service within the blueprint
        def services = raw.services ?: []
        if (raw.verb && raw.noun && !raw.services) services = [raw]
        
        def found = services.find { s -> 
            (s.serviceName == targetSrv) || ("${s.verb}${s.noun}" == targetSrv) || ("${s.verb}#${s.noun}" == targetSrv)
        }
        if (found) {
            context.blueprint = new HashMap(found)
            if (!context.blueprint.meta) context.blueprint.meta = [:]
            if (raw.meta) context.blueprint.meta.putAll(raw.meta)
            context.blueprint.meta.title = "${raw.meta?.title ?: targetBase}: ${found.verb}#${found.noun}"
        } else {
            context.blueprint = [meta: [title: "Service ${targetSrv} not found in ${targetBase}"]]
        }
    } else {
        context.blueprint = new HashMap(raw)
    }
}

// 2. Standardize Result and fallback
if (!context.blueprint) {
    context.blueprint = [ meta: [title: fileName] ]
    if (targetSrv) context.blueprint.serviceName = targetSrv
}

if (!context.blueprint.meta) context.blueprint.meta = [:]
context.blueprint.meta.hipaa_audit = true
if (!context.blueprint.meta.title) context.blueprint.meta.title = fileName

return context
