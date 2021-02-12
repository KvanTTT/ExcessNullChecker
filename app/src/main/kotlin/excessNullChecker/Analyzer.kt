package excessNullChecker

import org.objectweb.asm.ClassReader
import excessNullChecker.visitors.*
import java.io.File
import java.lang.Exception

class Analyzer(private val logger: Logger) {
    fun runOnJavaFile(javaFile: File, removeClassFile: Boolean) {
        // TODO: support of temp dir
        logger.info("Compile ${javaFile.absoluteFile}...")

        try {
            val process = Runtime.getRuntime().exec("javac ${javaFile.absolutePath}")
            val result = process.waitFor() // TODO: fix hanging for large files
            if (result != 0) {
                val errorStream = process.errorStream
                val errorMessage = StringBuilder()
                for (i in 0 until errorStream.available()) {
                    errorMessage.append(errorStream.read().toChar())
                }
                logger.error(errorMessage.toString())
                return
            }
        } catch (e: Exception) {
            logger.error(e.message ?: "Unable to launch javac")
            return
        }

        // TODO: consider nested classes
        val classFile = File(getFileWithoutExtension(javaFile.absolutePath) + ".class")
        runOnClassFile(classFile)

        if (removeClassFile)
            classFile.delete()
    }

    fun runOnClassFile(classFile: File) {
        logger.info("Excess null check of $classFile...")

        EmptyFieldVisitor()
        EmptyMethodVisitor()

        try {
            val bytes = classFile.readBytes()
            val classReader = ClassReader(bytes)

            val declCollector = DeclCollector()
            classReader.accept(declCollector, 0)

            val cfgNodeCreator = CfgNodeCreator()
            classReader.accept(cfgNodeCreator, 0)
            classReader.accept(CfgNodeInitializer(cfgNodeCreator.methodsCfg), 0)

            val processedMethods = mutableMapOf<String, DataEntry>()
            val processedFinalFields = mutableMapOf<String, DataEntryType>()
            for (item in declCollector.fields)
                if (item.value.isFinal)
                    processedFinalFields[item.key] = DataEntryType.Uninitialized

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
        catch (ex: Exception) {
            logger.error(ex.message ?: "Error during $classFile checking")
        }
    }
}