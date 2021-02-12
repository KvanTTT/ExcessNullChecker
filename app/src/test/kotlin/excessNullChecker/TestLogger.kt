package excessNullChecker

import kotlin.test.assertTrue
import kotlin.test.fail

class TestLogger(messages: List<Message>): Logger {
    private val expectedMessages: List<Message> = messages
    var notFoundMessages: HashSet<Message> = messages.toHashSet()

    override fun info(message: Message) {
        println(message)
        assertTrue(expectedMessages.contains(message), "Invalid or false positive message: $message")
        notFoundMessages.remove(message)
    }

    override fun info(message: String) {
        println(message)
    }

    override fun error(message: String) {
        fail(message)
    }
}