package excessNullChecker.visitors

import org.objectweb.asm.*

class EmptyFieldVisitor : FieldVisitor(Opcodes.ASM5) {
    companion object {
        val instance: EmptyFieldVisitor = EmptyFieldVisitor()
    }
}

class EmptyMethodVisitor : MethodVisitor(Opcodes.ASM5) {
    companion object {
        val instance: EmptyMethodVisitor = EmptyMethodVisitor()
    }
}