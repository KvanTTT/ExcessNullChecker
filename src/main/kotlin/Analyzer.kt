import jdk.internal.org.objectweb.asm.ClassReader
import visitors.*
import java.nio.file.Files
import java.nio.file.Paths

class Analyzer(private val logger: Logger) {
    fun run(classFileName: String) {
        val bytes = Files.readAllBytes(Paths.get(classFileName))
        val classReader = ClassReader(bytes)
        EmptyFieldVisitor()
        EmptyMethodVisitor()

        val declCollector = DeclCollector()
        classReader.accept(declCollector, 0)

        val cfgNodeCreator = CfgNodeCreator()
        classReader.accept(cfgNodeCreator, 0)
        classReader.accept(CfgNodeInitializer(cfgNodeCreator.methodsCfg), 0)

        val processedMethods = mutableMapOf<String, DataEntry>()
        val processedFinalFields = mutableMapOf<String, NullType>()
        for (item in declCollector.fields)
            if (item.value.isFinal)
                processedFinalFields[item.key] = NullType.Uninitialized

        val staticConstructorAnalyzer = MethodAnalyzer(
            BypassType.StaticConstructor,
            declCollector.fields,
            processedMethods,
            processedFinalFields,
            cfgNodeCreator.methodsCfg,
            null,
            bytes,
            logger
        )
        classReader.accept(staticConstructorAnalyzer, 0)

        val constructorAnalyzer = MethodAnalyzer(
            BypassType.Constructors,
            declCollector.fields,
            processedMethods,
            processedFinalFields,
            cfgNodeCreator.methodsCfg,
            null,
            bytes,
            logger
        )
        classReader.accept(constructorAnalyzer, 0)

        val methodAnalyzer = MethodAnalyzer(
            BypassType.Methods,
            declCollector.fields,
            processedMethods,
            processedFinalFields,
            cfgNodeCreator.methodsCfg,
            null,
            bytes,
            logger
        )
        classReader.accept(methodAnalyzer, 0)
    }
}