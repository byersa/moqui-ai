import java.io.File
import java.io.FilenameFilter

def runtimePath = ec.factory.runtimePath
def targetDir = new File(runtimePath, "component/${componentName}/${type}")

if (!targetDir.exists()) {
    context.artifacts = []
    return
}

def files = targetDir.listFiles({ dir, name -> name.endsWith(".json") } as FilenameFilter)
context.artifacts = files ? files.collect { it.name.replace(".json", "") }.sort() : []
