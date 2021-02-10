package visitors

import CfgNode
import DataEntry
import Dirty
import Logger
import NullType
import Uninitialized
import jdk.internal.org.objectweb.asm.*

enum class BypassType {
    Constructors,
    StaticConstructor,
    Methods,
    All
}

class MethodAnalyzer(
    private val bypassType: BypassType,
    private val fields: Map<String, FieldInfo>,
    private val processedMethods: MutableMap<String, DataEntry>,
    private val processedFinalFields: MutableMap<String, NullType>,
    private val methodsInfos: Map<String, Map<Int, CfgNode>>,
    private val methodToProcess: String? = null,
    private val classFileData: ByteArray,
    private val logger: Logger
)
    : ClassVisitor(Opcodes.ASM5) {

    override fun visitMethod(p0: Int, p1: String?, p2: String?, p3: String?, p4: Array<out String>?): MethodVisitor {
        val isConstructor = p1.equals("<init>")
        val isStaticConstructor = p1.equals("<clinit>")

        if (bypassType == BypassType.Constructors && !isConstructor ||
            bypassType == BypassType.StaticConstructor && !isStaticConstructor ||
            bypassType == BypassType.Methods && (isConstructor || isStaticConstructor)) {
            return EmptyMethodVisitor.instance
        }

        val isStatic = (p0 and Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC
        val signature = Signature.get(isStatic, p1, p2)

        if (methodToProcess != null && methodToProcess != signature.fullName) {
            return EmptyMethodVisitor.instance
        }

        if (!processedMethods.contains(signature.fullName)) {
            val isFinalOrStatic =
                p0 and (Opcodes.ACC_FINAL or Opcodes.ACC_STATIC) != 0 // Other methods may be overridden
            processedMethods[signature.fullName] = if (isFinalOrStatic)
                DataEntry(Uninitialized, NullType.Uninitialized) else
                DataEntry(Dirty, NullType.Mixed)
            return CodeAnalyzer(
                signature,
                fields,
                processedMethods,
                processedFinalFields,
                methodsInfos,
                classFileData,
                logger
            )
        }

        return EmptyMethodVisitor.instance
    }
}