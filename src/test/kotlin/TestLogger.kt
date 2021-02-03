import kotlin.test.assertTrue

class TestLogger(messages: List<Message>): Logger {
    private val expectedMessages: List<Message> = messages
    var notFoundMessages: HashSet<Message> = messages.toHashSet()

    override fun log(message: Message) {
        println(message)
        assertTrue(expectedMessages.contains(message), "Invalid or false positive message: $message")
        notFoundMessages.remove(message)
    }
}