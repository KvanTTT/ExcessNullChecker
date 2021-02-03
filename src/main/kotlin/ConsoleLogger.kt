class ConsoleLogger : Logger {
    override fun log(diagnostics: Message) {
        println(diagnostics)
    }
}