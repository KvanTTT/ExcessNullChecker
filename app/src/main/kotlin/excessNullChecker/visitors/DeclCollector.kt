package excessNullChecker.visitors

import org.objectweb.asm.*

class FieldInfo(val name: String, val isFinal: Boolean)

class DeclCollector : ClassVisitor(Opcodes.ASM5) {
    var fields: MutableMap<String, FieldInfo> = mutableMapOf()

    override fun visitField(p0: Int, p1: String?, p2: String?, p3: String?, p4: Any?): FieldVisitor {
        if (p1 != null) {
            val isFinal = (p0 and Opcodes.ACC_FINAL) == Opcodes.ACC_FINAL
            fields[p1] = FieldInfo(p1, isFinal)
        }
        return EmptyFieldVisitor.instance
    }

    override fun visitMethod(p0: Int, p1: String?, p2: String?, p3: String?, p4: Array<out String>?): MethodVisitor {
        return EmptyMethodVisitor.instance
    }
}