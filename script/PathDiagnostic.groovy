import org.moqui.context.ExecutionContext
ExecutionContext ec = context.ec
def runtimePath = ec.factory.runtimePath
def componentName = "aitree"
def type = "service"
def artifactName = "MeetingServices"
def jsonFile = new File(runtimePath, "component/${componentName}/${type}/${artifactName}.json")
ec.logger.info("DIAGNOSTIC: runtimePath=${runtimePath}")
ec.logger.info("DIAGNOSTIC: jsonFile target path=${jsonFile.absolutePath}")
ec.logger.info("DIAGNOSTIC: jsonFile exists? ${jsonFile.exists()}")

def xmlLoc = "component://${componentName}/${type}/${artifactName}.xml"
def xmlRef = ec.resource.getLocationReference(xmlLoc)
ec.logger.info("DIAGNOSTIC: xmlLoc=${xmlLoc}")
ec.logger.info("DIAGNOSTIC: xmlRef exists? ${xmlRef?.exists()}")
if (xmlRef?.exists()) {
    def root = xmlRef.getXmlNode()
    ec.logger.info("DIAGNOSTIC: root name=${root.name()}")
    ec.logger.info("DIAGNOSTIC: root children count=${root.children().size()}")
    root.children().each { c ->
        ec.logger.info("DIAGNOSTIC: child name=${c.name()}")
    }
}
