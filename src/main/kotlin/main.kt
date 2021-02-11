import java.io.File

fun main(args: Array<String>) {
    val logger = ConsoleLogger()

    val file = File(args[0])
    val analyzer = Analyzer(logger)
    when (file.extension) {
        "java" -> analyzer.runOnJavaFile(file)
        "class" -> analyzer.runOnClassFile(file)
        else -> {
            logger.error("Unsupported file ${file.absolutePath}")
        }
    }
}

data class FileNameAndExtension(val fileName: String, val extension: String)

fun getFileWithoutExtension(fileName: String): String {
    val index = fileName.lastIndexOf('.')
    return if (index == -1) {
        fileName
    } else {
        fileName.substring(0, index)
    }
}
