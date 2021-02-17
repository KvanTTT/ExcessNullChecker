package excessNullChecker.visitors

import excessNullChecker.CfgLink
import excessNullChecker.CfgLinkType
import excessNullChecker.CfgNode
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import excessNullChecker.CfgReturnNodeIndex

class CfgNodeInitializer : ClassVisitor(Opcodes.ASM5) {
    val methodsCfg: MutableMap<String, MutableMap<Int, CfgNode>> = mutableMapOf()

    override fun visitMethod(p0: Int, p1: String?, p2: String?, p3: String?, p4: Array<out String>?): MethodVisitor {
        val methodCfg = mutableMapOf<Int, CfgNode>()
        methodsCfg[p1 + p2] = methodCfg
        return CfgNodeInitializerHelper(methodCfg)
    }
}

class CfgNodeInitializerHelper(private val cfg: MutableMap<Int, CfgNode>): AdvancedVisitor() {
    private var currentCfgNode: CfgNode? = null
    private val visitedLabels: MutableMap<Label, CfgNode> = mutableMapOf()
    private val linksAtLabels: MutableMap<Label, MutableList<Pair<CfgNode, CfgLinkType>>> = mutableMapOf()
    private var createNextNode: Boolean = true
    private var nextNodeLinkType: CfgLinkType? = CfgLinkType.Epsilon

    override fun visitCode() {
        cfg[CfgReturnNodeIndex] = CfgNode(-1)
    }

    override fun visitVarInsn(p0: Int, p1: Int) {
        initializeCfgNode()
    }

    override fun visitIincInsn(p0: Int, p1: Int) {
        initializeCfgNode()
    }

    override fun visitFieldInsn(p0: Int, p1: String?, p2: String?, p3: String?) {
        initializeCfgNode()
    }

    override fun visitIntInsn(p0: Int, p1: Int) {
        initializeCfgNode()
    }

    override fun visitLdcInsn(p0: Any?) {
        initializeCfgNode()
    }

    override fun visitMethodInsn(p0: Int, p1: String?, p2: String?, p3: String?, p4: Boolean) {
        initializeCfgNode()
    }

    override fun visitTypeInsn(p0: Int, p1: String?) {
        initializeCfgNode()
    }

    override fun visitMultiANewArrayInsn(p0: String?, p1: Int) {
        initializeCfgNode()
    }

    override fun visitLookupSwitchInsn(dflt: Label?, keys: IntArray?, labels: Array<out Label>?) {
        throwUnsupportedOpcode(Opcodes.LOOKUPSWITCH)
    }

    override fun visitTableSwitchInsn(min: Int, max: Int, dflt: Label?, vararg labels: Label?) {
        throwUnsupportedOpcode(Opcodes.TABLESWITCH)
    }

    override fun visitTryCatchBlock(start: Label?, end: Label?, handler: Label?, type: String?) {
        throw Exception("try-catch-finally block is not supported, TODO: https://github.com/KvanTTT/ExcessNullChecker/issues/9")
    }

    override fun visitInsn(p0: Int) {
        initializeCfgNode()

        when (p0) {
            Opcodes.IRETURN,
            Opcodes.DRETURN,
            Opcodes.FRETURN,
            Opcodes.LRETURN,
            Opcodes.ARETURN,
            Opcodes.RETURN,
            Opcodes.ATHROW -> {
                val cfgNode = currentCfgNode
                val returnCfgNode = cfg[CfgReturnNodeIndex]
                if (cfgNode != null && returnCfgNode != null) {
                    addLink(cfgNode, returnCfgNode, CfgLinkType.Epsilon)
                }
                createNextNode = true
                nextNodeLinkType = null
            }
        }
    }

    override fun visitJumpInsn(p0: Int, p1: Label?) {
        initializeCfgNode()

        createNextNode = true
        var cfgLinkType: CfgLinkType = CfgLinkType.Epsilon
        when (p0) {
            Opcodes.IF_ICMPEQ,
            Opcodes.IF_ICMPNE,
            Opcodes.IF_ICMPLT,
            Opcodes.IF_ICMPGE,
            Opcodes.IF_ICMPGT,
            Opcodes.IF_ICMPLE,
            Opcodes.IF_ACMPEQ,
            Opcodes.IF_ACMPNE,
            Opcodes.IFEQ,
            Opcodes.IFNE,
            Opcodes.IFLT,
            Opcodes.IFGE,
            Opcodes.IFGT,
            Opcodes.IFLE,
            Opcodes.IFNULL,
            Opcodes.IFNONNULL -> {
                cfgLinkType = CfgLinkType.True
                nextNodeLinkType = CfgLinkType.False
            }
            Opcodes.GOTO -> {
                cfgLinkType = CfgLinkType.Epsilon
                nextNodeLinkType = null // No CFG link after unconditional instruction
            }
            else -> throwUnsupportedOpcode(p0)
        }

        val cfgNode = currentCfgNode

        if (cfgNode != null) {
            val jumpCfgNode = visitedLabels[p1]
            if (jumpCfgNode != null) {
                // Label is declared before the current offset, link it now
                addLink(cfgNode, jumpCfgNode, cfgLinkType)
            }
            else if (p1 != null) {
                // Label is declared after the current offset, save the link info for further binding
                var linkAtLabel = linksAtLabels[p1]
                if (linkAtLabel == null) {
                    linkAtLabel = mutableListOf()
                    linksAtLabels[p1] = linkAtLabel
                }
                linkAtLabel.add(Pair(cfgNode, cfgLinkType))
            }
        }
    }

    override fun visitLabel(p0: Label?) {
        createNextNode = true

        initializeCfgNode(false)

        val cfgNode = currentCfgNode
        if (p0 != null && cfgNode != null) {
            visitedLabels[p0] = cfgNode
        }

        val linkAtLabel = linksAtLabels[p0]
        if (linkAtLabel != null && cfgNode != null) {
            for (item in linkAtLabel) {
                addLink(item.first, cfgNode, item.second)
            }
        }
    }

    override fun visitEnd() {
        val cfgNode = currentCfgNode
        if (cfgNode != null) {
            cfgNode.end = offset

            val returnCfgNode = cfg[CfgReturnNodeIndex]
            if (returnCfgNode != null) {
                returnCfgNode.end = offset
                val nextType = nextNodeLinkType
                if (nextType != null)
                    addLink(cfgNode, returnCfgNode, nextType)
                nextNodeLinkType = CfgLinkType.Epsilon
            }
        }
    }

    private fun initializeCfgNode(incOffset: Boolean = true) {
        if (createNextNode) {
            val nextCfgNode = CfgNode(offset)
            cfg[offset] = nextCfgNode
            val cfgNode = currentCfgNode
            if (cfgNode != null) {
                cfgNode.end = offset

                val nextType = nextNodeLinkType
                if (nextType != null)
                    addLink(cfgNode, nextCfgNode, nextType)
            }

            currentCfgNode = nextCfgNode
            createNextNode = false
        }
        nextNodeLinkType = CfgLinkType.Epsilon

        if (incOffset)
            incOffset()
    }

    private fun addLink(begin: CfgNode, end: CfgNode, linkType: CfgLinkType) {
        val cfgLink = CfgLink(begin, end, linkType)
        begin.links.add(cfgLink)
        end.links.add(cfgLink)
    }
}