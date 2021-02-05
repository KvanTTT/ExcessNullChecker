package visitors

import NullType
import jdk.internal.org.objectweb.asm.ClassVisitor
import jdk.internal.org.objectweb.asm.FieldVisitor
import jdk.internal.org.objectweb.asm.MethodVisitor
import jdk.internal.org.objectweb.asm.Opcodes

class DeclCollector : ClassVisitor(Opcodes.ASM5) {
    var finalFields: MutableMap<String, NullType> = mutableMapOf()
    var availableMethods: MutableSet<String> = mutableSetOf()

    override fun visitField(p0: Int, p1: String?, p2: String?, p3: String?, p4: Any?): FieldVisitor {
        val isFinal = (p0 and Opcodes.ACC_FINAL) == Opcodes.ACC_FINAL
        if (isFinal && p1 != null) {
            // Consider only final fields
            finalFields[p1] = NullType.Uninitialized
        }
        return EmptyFieldVisitor()
    }

    override fun visitMethod(p0: Int, p1: String?, p2: String?, p3: String?, p4: Array<out String>?): MethodVisitor {
        availableMethods.add(p1 + p2)
        return EmptyMethodVisitor()
    }
}