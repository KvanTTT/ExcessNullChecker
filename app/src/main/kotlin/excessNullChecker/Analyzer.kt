package excessNullChecker

import org.objectweb.asm.ClassReader
import excessNullChecker.visitors.*
import java.io.File
import java.lang.Exception

class Analyzer(private val logger: Logger) {
    fun runOnJavaFile(javaFile: File, removeTempFile: Boolean) {
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

        if (removeTempFile)
            classFile.delete()
    }

    fun runOnClassFile(classFile: File) {
        logger.info("Excess null check of $classFile...")

        try {
            val bytes = classFile.readBytes()
            val classReader = ClassReader(bytes)

            // collect declarations (fields)
            val declCollector = DeclCollector()
            classReader.accept(declCollector, 0)

            // Initialize cfg for every method
            val cfgNodeInitializer = CfgNodeInitializer()
            classReader.accept(cfgNodeInitializer, 0)

            val context = Context(declCollector.fields, cfgNodeInitializer.methodsCfg, bytes, logger)

            // Analyze static constructor
            val staticConstructorAnalyzer = MethodAnalyzer(context, BypassType.StaticConstructor)
            classReader.accept(staticConstructorAnalyzer, 0)

            // Analyze ordinary constructors
            val constructorAnalyzer = MethodAnalyzer(context, BypassType.Constructors)
            classReader.accept(constructorAnalyzer, 0)

            // Analyze rest methods
            val methodAnalyzer = MethodAnalyzer(context, BypassType.Methods)
            classReader.accept(methodAnalyzer, 0)
        }
        catch (ex: Exception) {
            logger.error("Error during $classFile checking: ${ex.message ?: ex.toString()}")
        }
    }
}