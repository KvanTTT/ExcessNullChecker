interface Logger {
    fun info(message: Message)

    fun info(message: String)

    fun error(message: String)
}