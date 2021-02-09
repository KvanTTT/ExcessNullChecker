package visitors

import NullType
import jdk.internal.org.objectweb.asm.*

class DeclCollector : ClassVisitor(Opcodes.ASM5) {
    var finalFields: MutableMap<String, NullType> = mutableMapOf()

    override fun visitField(p0: Int, p1: String?, p2: String?, p3: String?, p4: Any?): FieldVisitor {
        val isFinal = (p0 and Opcodes.ACC_FINAL) == Opcodes.ACC_FINAL
        if (isFinal && p1 != null) {
            // Consider only final fields
            finalFields[p1] = NullType.Uninitialized
        }
        return EmptyFieldVisitor.instance
    }

    override fun visitMethod(p0: Int, p1: String?, p2: String?, p3: String?, p4: Array<out String>?): MethodVisitor {
        return EmptyMethodVisitor.instance
    }
}