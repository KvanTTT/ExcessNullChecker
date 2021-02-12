package excessNullChecker.visitors

import excessNullChecker.CfgNode
import excessNullChecker.CfgReturnIndex
import org.objectweb.asm.*

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

    override fun visitCode() {
        methodCfg[CfgReturnIndex] = CfgNode(-1)
    }

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

    override fun visitMultiANewArrayInsn(p0: String?, p1: Int) {
        createCfgNode()
    }

    override fun visitLookupSwitchInsn(dflt: Label?, keys: IntArray?, labels: Array<out Label>?) {
        throw Exception("Opcode LOOKUPSWITCH is not supported, TODO: https://github.com/KvanTTT/ExcessNullChecker/issues/6")
    }

    override fun visitTableSwitchInsn(min: Int, max: Int, dflt: Label?, vararg labels: Label?) {
        throw Exception("Opcode TABLESWITCH is not supported, TODO: https://github.com/KvanTTT/ExcessNullChecker/issues/6")
    }

    override fun visitTryCatchBlock(start: Label?, end: Label?, handler: Label?, type: String?) {
        throw Exception("try-catch-finally block is not supported, TODO: https://github.com/KvanTTT/ExcessNullChecker/issues/9")
    }

    override fun visitJumpInsn(p0: Int, p1: Label?) {
        markNextInstruction = p0 != Opcodes.GOTO
        incOffset()
    }

    override fun visitLabel(p0: Label?) {
        methodCfg[offset] = CfgNode(offset)
    }

    private fun createCfgNode() {
        if (markNextInstruction) {
            methodCfg[offset] = CfgNode(offset)
            markNextInstruction = false
        }
        incOffset()
    }
}