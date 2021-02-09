package visitors

import CfgNode
import jdk.internal.org.objectweb.asm.ClassVisitor
import jdk.internal.org.objectweb.asm.Label
import jdk.internal.org.objectweb.asm.MethodVisitor
import jdk.internal.org.objectweb.asm.Opcodes

class CfgNodeCreator: ClassVisitor( Opcodes.ASM5) {
    val methodsCfg: MutableMap<String, MutableMap<Int, CfgNode>> = mutableMapOf()

    override fun visitMethod(p0: Int, p1: String?, p2: String?, p3: String?, p4: Array<out String>?): MethodVisitor {
        val methodCfg = mutableMapOf<Int, CfgNode>()
        methodsCfg[p1 + p2] = methodCfg
        return CfgNodeCreatorHelper(methodCfg)
    }
}

class CfgNodeCreatorHelper(
    private val methodCfg: MutableMap<Int, CfgNode>
): AdvancedVisitor() {
    private var markNextInstruction: Boolean = true

    override fun visitInsn(p0: Int) {
        createCfgNode()
    }

    override fun visitVarInsn(p0: Int, p1: Int) {
        createCfgNode()
    }

    override fun visitIincInsn(p0: Int, p1: Int) {
        createCfgNode()
    }

    override fun visitFieldInsn(p0: Int, p1: String?, p2: String?, p3: String?) {
        createCfgNode()
    }

    override fun visitIntInsn(p0: Int, p1: Int) {
        createCfgNode()
    }

    override fun visitLdcInsn(p0: Any?) {
        createCfgNode()
    }

    override fun visitMethodInsn(p0: Int, p1: String?, p2: String?, p3: String?, p4: Boolean) {
        createCfgNode()
    }

    override fun visitTypeInsn(p0: Int, p1: String?) {
        createCfgNode()
    }

    override fun visitJumpInsn(p0: Int, p1: Label?) {
        markNextInstruction = p0 != Opcodes.GOTO
        incOffset()
    }

    private fun createCfgNode() {
        if (markNextInstruction) {
            methodCfg[offset] = CfgNode(offset)
            markNextInstruction = false
        }
        incOffset()
    }

    override fun visitLabel(p0: Label?) {
        methodCfg[offset] = CfgNode(offset)
    }
}