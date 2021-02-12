package excessNullChecker.visitors

import org.objectweb.asm.*

class EmptyFieldVisitor : FieldVisitor(Opcodes.ASM5) {
    companion object {
        lateinit var instance: EmptyFieldVisitor
    }

    init {
        instance = this
    }
}

class EmptyMethodVisitor : MethodVisitor(Opcodes.ASM5) {
    companion object {
        lateinit var instance: EmptyMethodVisitor
    }

    init {
        instance = this
    }
}