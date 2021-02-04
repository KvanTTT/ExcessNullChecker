import jdk.internal.org.objectweb.asm.ClassReader
import visitors.ClassAnalyzer
import visitors.ConstructorAnalyzer
import visitors.FieldCollector
import java.nio.file.Files
import java.nio.file.Paths

class Analyzer(private val logger: Logger) {
    fun run(classFileName: String) {
        val bytes = Files.readAllBytes(Paths.get(classFileName))
        val classReader = ClassReader(bytes)

        val fieldCollector = FieldCollector()
        classReader.accept(fieldCollector, 0)

        val constructorAnalyzer = ConstructorAnalyzer(fieldCollector.finalFields, logger)
        classReader.accept(constructorAnalyzer, 0)

        val visitor = ClassAnalyzer(fieldCollector.finalFields, logger)
        classReader.accept(visitor, 0)
    }
}