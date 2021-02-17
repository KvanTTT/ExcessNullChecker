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
    val methodsCfg: MutableMap<String, MutableMap<Int, MutableList<CfgNode>>> = mutableMapOf()

    override fun visitMethod(p0: Int, p1: String?, p2: String?, p3: String?, p4: Array<out String>?): MethodVisitor {
        val methodCfg = mutableMapOf<Int, MutableList<CfgNode>>()
        methodsCfg[p1 + p2] = methodCfg
        return CfgNodeInitializerHelper(methodCfg)
    }
}

class CfgNodeInitializerHelper(private val cfg: MutableMap<Int, MutableList<CfgNode>>): AdvancedVisitor() {
    private val visitedLabels: MutableMap<Label, CfgNode> = mutableMapOf()
    private val futureLinks: MutableMap<Label, MutableList<Pair<CfgNode, CfgLinkType>>> = mutableMapOf()
    private var currentCfgNode: CfgNode? = null
    private var createNextNode: Boolean = true
    private var nextNodeLinkType: CfgLinkType? = CfgLinkType.Epsilon

    override fun visitCode() {
        cfg[CfgReturnNodeIndex] = mutableListOf(CfgNode(CfgReturnNodeIndex))
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
        visitSwitchCase(labels!!, dflt!!)
    }

    override fun visitTableSwitchInsn(min: Int, max: Int, dflt: Label?, vararg labels: Label?) {
        val notNullLabels = labels.filterNotNull().toTypedArray()
        visitSwitchCase(notNullLabels, dflt!!)
    }

    private fun visitSwitchCase(labels: Array<out Label>, dflt: Label) {
        initializeCfgNode(false)

        var cfgNode: CfgNode? = null
        var nextCfgNode: CfgNode
        var nextCfgNodes = cfg[offset]
        if (nextCfgNodes == null) {
            nextCfgNode = CfgNode(offset)
            nextCfgNode.endOffset = offset
            cfgNode = currentCfgNode
            if (cfgNode != null) {
                cfgNode.endOffset = offset
                addLink(cfgNode, nextCfgNode, CfgLinkType.Epsilon)
            }
            cfgNode = nextCfgNode
            nextCfgNodes = mutableListOf(nextCfgNode)
            cfg[offset] = nextCfgNodes
        }

        for (index in labels.indices) {
            cfgNode = nextCfgNodes[index]
            cfgNode.index = index
            cfgNode.endOffset = offset
            addFutureLink(labels[index], cfgNode, CfgLinkType.True)

            if (index < labels.size - 1) {
                nextCfgNode = CfgNode(offset)
                nextCfgNodes.add(nextCfgNode)
                addLink(cfgNode, nextCfgNode, CfgLinkType.False)
            }
        }

        if (cfgNode != null) {
            // Link with default label
            addFutureLink(dflt, cfgNode, if (labels.isNotEmpty()) CfgLinkType.False else CfgLinkType.Epsilon)
        }

        currentCfgNode = null
        nextNodeLinkType = CfgLinkType.Epsilon

        incOffset()
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
                val returnCfgNode = cfg[CfgReturnNodeIndex]?.first()

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
            } else if (p1 != null) {
                addFutureLink(p1, cfgNode, cfgLinkType)
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

        val futureLink = futureLinks[p0]
        if (futureLink != null && cfgNode != null) {
            for (item in futureLink) {
                addLink(item.first, cfgNode, item.second)
            }
        }
    }

    override fun visitEnd() {
        val cfgNode = currentCfgNode
        if (cfgNode != null) {
            cfgNode.endOffset = offset

            val returnCfgNode = cfg[CfgReturnNodeIndex]?.first()
            if (returnCfgNode != null) {
                returnCfgNode.endOffset = offset
                val nextType = nextNodeLinkType
                if (nextType != null)
                    addLink(cfgNode, returnCfgNode, nextType)
                nextNodeLinkType = CfgLinkType.Epsilon
            }
        }

        optimizeCfg()
    }

    private fun initializeCfgNode(incOffset: Boolean = true) {
        if (createNextNode) {
            val nextCfgNode = CfgNode(offset)
            cfg[offset] = mutableListOf(nextCfgNode)

            val cfgNode = currentCfgNode
            if (cfgNode != null) {
                cfgNode.endOffset = offset

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

    private fun optimizeCfg() {
        for (i in 0 until offset) {
            val nodes = cfg[i]
            if (nodes != null) {
                val nodesToRemove = mutableListOf<CfgNode>()

                for (node in nodes) {
                    val parentLinks = node.getParentLinks()
                    if (parentLinks.size == 1) {
                        val parentLink = parentLinks[0]
                        val parentNode = parentLink.beginNode
                        val childLinks = parentNode.getChildLinks()
                        if (childLinks.size == 1 && parentLink.type == CfgLinkType.Epsilon && parentNode.endOffset == node.beginOffset) {
                            parentNode.endOffset = node.endOffset
                            parentNode.links.remove(parentLink)
                            for (childLink in node.getChildLinks()) {
                                childLink.endNode.links.remove(childLink)
                                addLink(parentNode, childLink.endNode, childLink.type)
                            }
                            nodesToRemove.add(node)
                        }
                    }
                }

                for (nodeToRemove in nodesToRemove)
                    nodes.remove(nodeToRemove)

                if (nodes.size == 0)
                    cfg.remove(i)
            }
        }
    }

    private fun addFutureLink(label: Label, cfgNode: CfgNode, cfgLinkType: CfgLinkType) {
        // Label is declared after the current offset, save the link info for further binding
        var futureLink = futureLinks[label]
        if (futureLink == null) {
            futureLink = mutableListOf()
            futureLinks[label] = futureLink
        }
        futureLink.add(Pair(cfgNode, cfgLinkType))
    }

    private fun addLink(begin: CfgNode, end: CfgNode, linkType: CfgLinkType) {
        val cfgLink = CfgLink(begin, end, linkType)
        begin.links.add(cfgLink)
        end.links.add(cfgLink)
    }
}