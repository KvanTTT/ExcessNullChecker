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
    fun test6() {
        testFile("Example6")
    }

    @Test
    fun finalField1() {
        testFile("finalField1")
    }

    @Test
    fun finalField2() {
        testFile("finalField2")
    }

    @Test
    fun finalField3() {
        testFile("finalField3")
    }

    fun testFile(fileName: String) {
        var fullJavaFilePath = Paths.get("src", "test", "resources", "$fileName.java").toString()
        var file = File(fullJavaFilePath)
        var expectedMessages = mutableListOf<Message>()
        var currentLine = 1
        file.forEachLine { line ->
            var matchResult = testMarkerRegex.find(line)
            if (matchResult != null) {
                var value = matchResult.groups[1]?.value?.toLowerCase()
                if (!value.equals("true") && !value.equals("false")) {
                    fail("Unable parse test marker ${matchResult.value}")
                }
                expectedMessages.add(ExcessCheckMessage(value.toBoolean(), currentLine))
            }
            currentLine++
        }

        var outputDir = Paths.get("build", "test-results").toString()
        var result = Runtime.getRuntime().exec("javac -d $outputDir $fullJavaFilePath").waitFor()
        if (result != 0) {
            fail("Unable to compile $fullJavaFilePath")
        }

        var classFileName = Paths.get(outputDir, "test", "$fileName.class").toString()
        var testLogger = TestLogger(expectedMessages)

        Analyzer(testLogger).run(classFileName)

        if (testLogger.notFoundMessages.size > 0) {
            fail("Not found: ${testLogger.notFoundMessages.first()}")
        }
    }
}