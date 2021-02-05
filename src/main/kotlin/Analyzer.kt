import jdk.internal.org.objectweb.asm.ClassReader
import visitors.BypassType
import visitors.MethodAnalyzer
import visitors.DeclCollector
import java.nio.file.Files
import java.nio.file.Paths

class Analyzer(private val logger: Logger) {
    fun run(classFileName: String) {
        val bytes = Files.readAllBytes(Paths.get(classFileName))
        val classReader = ClassReader(bytes)

        val declCollector = DeclCollector()
        classReader.accept(declCollector, 0)

        val processedMethods = mutableMapOf<String, NullType>()

        val constructorAnalyzer = MethodAnalyzer(
            BypassType.Constructors,
            declCollector.finalFields,
            processedMethods,
            declCollector.availableMethods,
            null,
            bytes,
            logger
        )
        classReader.accept(constructorAnalyzer, 0)

        val methodAnalyzer = MethodAnalyzer(
            BypassType.Methods,
            declCollector.finalFields,
            processedMethods,
            declCollector.availableMethods,
            null,
            bytes,
            logger
        )
        classReader.accept(methodAnalyzer, 0)
    }
}