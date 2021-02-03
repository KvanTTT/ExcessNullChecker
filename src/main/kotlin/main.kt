import jdk.internal.org.objectweb.asm.ClassReader
import java.nio.file.Files
import java.nio.file.Paths

fun main(args: Array<String>) {
    var fileName = args[0]
    var classFileName = getBaseName(fileName) + ".class"

    println("Excess null checking of $fileName...")
    var result = Runtime.getRuntime().exec("javac $fileName").waitFor()
    if (result != 0) {
        println("Invalid $fileName file")
        return
    }
    var bytes = Files.readAllBytes(Paths.get(classFileName))
    var classReader = ClassReader(bytes)

    var visitor = ExcessNullCheckerClassVisitor(ConsoleLogger())
    classReader.accept(visitor, 0)
}

fun getBaseName(fileName: String): String {
    val index = fileName.lastIndexOf('.')
    return if (index == -1) {
        fileName
    } else {
        fileName.substring(0, index)
    }
}
