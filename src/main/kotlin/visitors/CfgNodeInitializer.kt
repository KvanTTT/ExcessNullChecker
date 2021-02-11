package visitors

import CfgLink
import CfgLinkType
import CfgNode
import CfgReturnIndex
import jdk.internal.org.objectweb.asm.ClassVisitor
import jdk.internal.org.objectweb.asm.Label
import jdk.internal.org.objectweb.asm.MethodVisitor
import jdk.internal.org.objectweb.asm.Opcodes

class CfgNodeInitializer(private val methodsCfg: Map<String, Map<Int, CfgNode>>) : ClassVisitor(Opcodes.ASM5) {
    override fun visitMethod(p0: Int, p1: String?, p2: String?, p3: String?, p4: Array<out String>?): MethodVisitor {
        return CfgNodeInitializerHelper(methodsCfg[p1 + p2]!!)
    }
}

class CfgNodeInitializerHelper(private val cfg: Map<Int, CfgNode>): AdvancedVisitor() {
    private var currentCfgNode: CfgNode? = null
    private val visitedLabels: MutableMap<Label, CfgNode> = mutableMapOf()
    private val linksAtLabels: MutableMap<Label, MutableList<Pair<CfgNode, CfgLinkType>>> = mutableMapOf()
    private var nextLinkType: CfgLinkType? = CfgLinkType.Epsilon

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

    override fun visitInsn(p0: Int) {
        initializeCfgNode()

        when (p0) {
            Opcodes.IRETURN,
            Opcodes.DRETURN,
            Opcodes.FRETURN,
            Opcodes.LRETURN,
            Opcodes.ARETURN,
            Opcodes.RETURN -> {
                val cfgNode = currentCfgNode
                val returnCfgNode = cfg[CfgReturnIndex]
                if (cfgNode != null && returnCfgNode != null) {
                    addLink(cfgNode, returnCfgNode, CfgLinkType.Epsilon)
                    nextLinkType = null
                }
            }
        }
    }

    override fun visitJumpInsn(p0: Int, p1: Label?) {
        initializeCfgNode()

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
                nextLinkType = CfgLinkType.False
            }
            Opcodes.GOTO -> {
                cfgLinkType = CfgLinkType.Epsilon
                nextLinkType = null
            }
            else -> throwUnsupportedOpcode(p0)
        }

        val cfgNode = currentCfgNode

        if (cfgNode != null) {
            val jumpCfgNode = visitedLabels[p1]
            if (jumpCfgNode != null) {
                addLink(cfgNode, jumpCfgNode, cfgLinkType)
            }
            else if (p1 != null) {
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

            val returnCfgNode = cfg[CfgReturnIndex]
            if (returnCfgNode != null) {
                returnCfgNode.end = offset
                val nextType = nextLinkType
                if (nextType != null)
                    addLink(cfgNode, returnCfgNode, nextType)
                nextLinkType = CfgLinkType.Epsilon
            }
        }
    }

    private fun initializeCfgNode(incOffset: Boolean = true) {
        val nextCfgNode = cfg[offset]

        if (nextCfgNode != null) {
            val cfgNode = currentCfgNode
            if (cfgNode != null && cfgNode != nextCfgNode) {
                cfgNode.end = offset

                val nextType = nextLinkType
                if (nextType != null)
                    addLink(cfgNode, nextCfgNode, nextType)
                nextLinkType = CfgLinkType.Epsilon
            }

            currentCfgNode = nextCfgNode
        }
        if (incOffset)
            incOffset()
    }

    private fun addLink(begin: CfgNode, end: CfgNode, linkType: CfgLinkType) {
        val cfgLink = CfgLink(begin, end, linkType)
        begin.links.add(cfgLink)
        end.links.add(cfgLink)
    }
}