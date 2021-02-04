fun main(args: Array<String>) {
    val fileName = args[0]
    val classFileName = getBaseName(fileName) + ".class"

    println("Excess null checking of $fileName...")
    val result = Runtime.getRuntime().exec("javac $fileName").waitFor()
    if (result != 0) {
        println("Invalid $fileName file")
        return
    }

    Analyzer(ConsoleLogger()).run(classFileName)
}

fun getBaseName(fileName: String): String {
    val index = fileName.lastIndexOf('.')
    return if (index == -1) {
        fileName
    } else {
        fileName.substring(0, index)
    }
}
