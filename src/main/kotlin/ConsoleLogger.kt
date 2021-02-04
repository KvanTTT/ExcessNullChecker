class ConsoleLogger : Logger {
    override fun log(message: Message) {
        println(message)
    }
}