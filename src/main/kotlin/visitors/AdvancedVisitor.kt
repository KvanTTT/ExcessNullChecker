package visitors

import jdk.internal.org.objectweb.asm.Label
import jdk.internal.org.objectweb.asm.MethodVisitor
import jdk.internal.org.objectweb.asm.Opcodes

open class AdvancedVisitor : MethodVisitor(Opcodes.ASM5) {
    private var _offset: Int = 0
    private var _currentLine: Int = 0
    protected val offset: Int
        get() = _offset
    protected val currentLine: Int
        get() = _currentLine

    override fun visitLineNumber(p0: Int, p1: Label?) {
        _currentLine = p0
    }

    override fun visitInsn(p0: Int) {
        incOffset()
    }

    override fun visitVarInsn(p0: Int, p1: Int) {
        incOffset()
    }

    override fun visitIincInsn(p0: Int, p1: Int) {
        incOffset()
    }

    override fun visitFieldInsn(p0: Int, p1: String?, p2: String?, p3: String?) {
        incOffset()
    }

    override fun visitIntInsn(p0: Int, p1: Int) {
        incOffset()
    }

    override fun visitLdcInsn(p0: Any?) {
        incOffset()
    }

    override fun visitMethodInsn(p0: Int, p1: String?, p2: String?, p3: String?, p4: Boolean) {
        incOffset()
    }

    override fun visitJumpInsn(p0: Int, p1: Label?) {
        incOffset()
    }

    override fun visitTypeInsn(p0: Int, p1: String?) {
        incOffset()
    }

    override fun visitMultiANewArrayInsn(p0: String?, p1: Int) {
        incOffset()
    }

    protected fun incOffset() {
        _offset++
    }

    protected fun throwUnsupportedOpcode(opcode: Int) {
        throw UnsupportedOperationException("Opcode $opcode is not supported")
    }
}