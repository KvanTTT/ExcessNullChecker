package excessNullChecker

interface Logger {
    fun info(message: Message)

    fun info(message: String)

    fun error(message: String)
}

class ConsoleLogger : Logger {
    val ANSI_RESET = "\u001B[0m"
    val ANSI_RED = "\u001B[31m"

    override fun info(message: Message) {
        println(message)
    }

    override fun info(message: String) {
        println(message)
    }

    override fun error(message: String) {
        println(ANSI_RED + message + ANSI_RESET)
    }
}