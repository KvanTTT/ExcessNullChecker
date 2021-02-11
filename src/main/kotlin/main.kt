import java.io.File

fun main(args: Array<String>) {
    val logger = ConsoleLogger()

    val file = File(args[0])
    if (file.isDirectory) {
        file.walkTopDown().forEach {
            if (it.isFile)
                analyseFile(it, true, warnUnsupportedFile = false, logger = logger)
        }
    }
    else {
        analyseFile(file, false, warnUnsupportedFile = true, logger = logger)
    }
}

fun analyseFile(file: File, removeClassFile: Boolean, warnUnsupportedFile: Boolean, logger: Logger) {
    val analyzer = Analyzer(logger)
    when (file.extension) {
        "java" -> analyzer.runOnJavaFile(file, removeClassFile)
        "class" -> analyzer.runOnClassFile(file)
        else -> {
            if (warnUnsupportedFile)
                logger.error("Unsupported file ${file.absolutePath}")
        }
    }
}

fun getFileWithoutExtension(fileName: String): String {
    val index = fileName.lastIndexOf('.')
    return if (index == -1) {
        fileName
    } else {
        fileName.substring(0, index)
    }
}
