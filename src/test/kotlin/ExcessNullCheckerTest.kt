import org.junit.Test
import java.io.File
import java.nio.file.Paths
import kotlin.test.fail

class ExcessNullCheckerTest {
    private val testMarkerRegex: Regex = Regex("//\\s+Test:\\s+(\\w+)")

    @Test
    fun test1() {
        testFile("Example1")
    }

    @Test
    fun test2() {
        testFile("Example2")
    }

    @Test
    fun test3() {
        testFile("Example3")
    }

    @Test
    fun test4() {
        testFile("Example4")
    }

    @Test
    fun test5() {
        testFile("Example5")
    }

    @Test
    fun fields() {
        testFile("Fields")
    }

    @Test
    fun methodCalls() {
        testFile("MethodCalls")
    }

    @Test
    fun exceptions() {
        testFile("Exceptions")
    }

    @Test
    fun misc() {
        testFile("Misc")
    }

    fun testFile(fileName: String) {
        val fullJavaFilePath = Paths.get("src", "test", "resources", "$fileName.java").toString()
        val file = File(fullJavaFilePath)
        val expectedMessages = mutableListOf<Message>()
        var currentLine = 1

        // TODO: use real lexer for correct detection of markers including commented out
        file.forEachLine { line ->
            val matchResult = testMarkerRegex.find(line)
            if (matchResult != null) {
                val value = matchResult.groups[1]?.value?.toLowerCase()
                if (!value.equals("condition_is_always_true") && !value.equals("condition_is_always_false")) {
                    fail("Unable parse test marker ${matchResult.value}")
                }
                expectedMessages.add(ExcessCheckMessage(value == "condition_is_always_true", currentLine))
            }
            currentLine++
        }

        val outputDir = Paths.get("build", "test-results").toString()
        val testLogger = TestLogger(expectedMessages)
        Analyzer(testLogger).runOnJavaFile(File(fullJavaFilePath), false)

        if (testLogger.notFoundMessages.size > 0) {
            fail("Not found: ${testLogger.notFoundMessages.first()}")
        }
    }
}