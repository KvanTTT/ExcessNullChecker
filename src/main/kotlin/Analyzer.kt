import jdk.internal.org.objectweb.asm.ClassReader
import visitors.ClassAnalyzer
import visitors.ConstructorAnalyzer
import visitors.FieldCollector
import java.nio.file.Files
import java.nio.file.Paths

class Analyzer(logger: Logger) {
    private val logger: Logger = logger

    fun run(classFileName: String) {
        var bytes = Files.readAllBytes(Paths.get(classFileName))
        var classReader = ClassReader(bytes)

        var fieldCollector = FieldCollector()
        classReader.accept(fieldCollector, 0)

        var constructorAnalyzer = ConstructorAnalyzer(fieldCollector.finalFields, logger)
        classReader.accept(constructorAnalyzer, 0)

        var visitor = ClassAnalyzer(fieldCollector.finalFields, logger)
        classReader.accept(visitor, 0)
    }
}