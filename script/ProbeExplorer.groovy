import groovy.json.JsonBuilder
   def ec = org.moqui.Moqui.getExecutionContext()
   def runtimePath = ec.factory.runtimePath
   def targetPath = "${runtimePath}/component/aitree/service"
   def targetDir = new File(targetPath)
   
   def results = [
       pathChecked: targetPath,
       exists: targetDir.exists(),
       isDirectory: targetDir.isDirectory(),
       filesFound: []
   ]
   
   if (targetDir.exists()) {
       targetDir.eachFileRecurse { file -> 
           results.filesFound << [name: file.name, size: file.length(), lastModified: file.lastModified()]
       }
   }
   
   println("!!! MCE PROBE RESULTS !!!")
   println(new JsonBuilder(results).toPrettyString())
   println("!!! END MCE PROBE !!!")