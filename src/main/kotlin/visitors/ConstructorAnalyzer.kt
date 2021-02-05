package visitors

import Logger
import NullType
import jdk.internal.org.objectweb.asm.ClassVisitor
import jdk.internal.org.objectweb.asm.MethodVisitor
import jdk.internal.org.objectweb.asm.Opcodes

class ConstructorAnalyzer(private val finalFields: MutableMap<String, NullType?>, private val logger: Logger) : ClassVisitor(Opcodes.ASM5) {

    override fun visitMethod(p0: Int, p1: String?, p2: String?, p3: String?, p4: Array<out String>?): MethodVisitor {
        if (p1.equals("<init>")) {
            val isStatic = (p0 and Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC
            return MethodAnalyzer(isStatic, Signature.get(p1, p2), finalFields, logger)
        }
        return EmptyMethodVisitor()
    }
}