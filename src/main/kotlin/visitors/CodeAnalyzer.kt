package visitors

import AnotherCondition
import CfgLinkType
import CfgNode
import Condition
import DataEntry
import ExcessCheckMessage
import Logger
import NullCheckCondition
import NullType
import Signature
import State
import jdk.internal.org.objectweb.asm.*

class CodeAnalyzer(
    private val signature: Signature,
    private val finalFields: MutableMap<String, NullType>,
    private val processedMethods: MutableMap<String, NullType>,
    private val methodsCfg: Map<String, Map<Int, CfgNode>>,
    private val classFileData: ByteArray,
    private val logger: Logger
) : AdvancedVisitor() {
    private var currentLine: Int = -1
    private var currentState: State
    private val cfgNodes: Map<Int, CfgNode> = methodsCfg[signature.fullName]!!

    private val cfgNodeStates: MutableMap<CfgNode, State> = mutableMapOf()

    init {
        currentState = State(cfgNodes[0])
        var offset = 0
        if (!signature.static) {
            // First data entry is always not null for instance methods
            offset = 1
            currentState.push(DataEntry(0, NullType.NotNull))
        }
        for (i in 0 until signature.paramsCount) {
            // Initialize local variables from parameters
            currentState.push(DataEntry(i + offset, NullType.Mixed))
        }
    }

    override fun visitLineNumber(p0: Int, p1: Label?) {
        currentLine = p0
    }

    override fun visitVarInsn(p0: Int, p1: Int) {
        checkState()
        when (p0)  {
            Opcodes.ILOAD,
            Opcodes.LLOAD,
            Opcodes.FLOAD,
            Opcodes.DLOAD -> {
                currentState.push(DataEntry(p1, NullType.Mixed))
            }
            Opcodes.ALOAD -> currentState.push(currentState.get(p1))
            Opcodes.ASTORE -> currentState.set(p1, currentState.pop())
        }
        incOffset()
    }

    override fun visitInsn(p0: Int) {
        checkState()
        when (p0) {
            Opcodes.ACONST_NULL -> {
                currentState.push(DataEntry(Utils.UninitializedIndex, NullType.Null))
            }
            Opcodes.ICONST_M1,
            Opcodes.ICONST_0,
            Opcodes.ICONST_1,
            Opcodes.ICONST_2,
            Opcodes.ICONST_3,
            Opcodes.ICONST_4,
            Opcodes.ICONST_5,
            Opcodes.LCONST_0,
            Opcodes.LCONST_1,
            Opcodes.FCONST_0,
            Opcodes.FCONST_1,
            Opcodes.FCONST_2,
            Opcodes.DCONST_0,
            Opcodes.DCONST_1 -> {
                currentState.push(DataEntry(Utils.UninitializedIndex, NullType.Mixed))
            }
            Opcodes.DUP -> {
                currentState.push(currentState.peek())
            }
            Opcodes.IRETURN,
            Opcodes.DRETURN,
            Opcodes.FRETURN,
            Opcodes.LRETURN,
            Opcodes.ARETURN,
            Opcodes.RETURN -> {
                if (p0 != Opcodes.RETURN) {
                    val currentDataEntry = currentState.pop()
                    val methodReturnType = processedMethods[signature.fullName]
                    if (methodReturnType != null)
                        processedMethods[signature.fullName] = methodReturnType.merge(currentDataEntry.type)
                }

                currentState.clear()
            }
        }
        incOffset()
    }

    override fun visitIntInsn(p0: Int, p1: Int) {
        checkState()
        when (p0) {
            Opcodes.BIPUSH,
            Opcodes.SIPUSH -> {
                currentState.push(DataEntry(Utils.UninitializedIndex, NullType.Mixed))
            }
        }
        incOffset()
    }

    override fun visitLdcInsn(p0: Any?) {
        checkState()
        currentState.push(DataEntry(Utils.UninitializedIndex, NullType.NotNull))
        incOffset()
    }

    override fun visitTypeInsn(p0: Int, p1: String?) {
        checkState()
        if (p0 == Opcodes.NEW) {
            currentState.push(DataEntry(-1, NullType.NotNull))
        }
        incOffset()
    }

    override fun visitMethodInsn(p0: Int, p1: String?, p2: String?, p3: String?, p4: Boolean) {
        checkState()
        val isStatic = p0 == Opcodes.INVOKESTATIC
        val signature = Signature.get(isStatic, p2, p3)
        if (p0 == Opcodes.INVOKEVIRTUAL || p0 == Opcodes.INVOKESPECIAL || p0 == Opcodes.INVOKESTATIC) {
            // Make virtual call and remove parameters from stack except of the first one
            for (i in 0 until signature.paramsCount) {
                currentState.pop()
            }

            if (p0 != Opcodes.INVOKESTATIC) {
                // Mark variable as NotNull because instance is always necessary during invocation
                // a.getHashCode()
                // if (a == null) // prevent excess check
                val invocationDataEntry = currentState.pop()
                currentState.set(invocationDataEntry.index, DataEntry(invocationDataEntry.index, NullType.NotNull))
            }

            var returnType: NullType? = null
            if (methodsCfg.containsKey(signature.fullName)) {
                returnType = processedMethods[signature.fullName]
                if (returnType == null) {
                    // Recursive analysing...
                    val methodAnalyzer = MethodAnalyzer(
                        BypassType.All,
                        finalFields,
                        processedMethods,
                        methodsCfg,
                        signature.fullName,
                        classFileData,
                        logger
                    )
                    val classReader = ClassReader(classFileData)
                    classReader.accept(methodAnalyzer, 0)
                    returnType = processedMethods[signature.fullName]
                }
            }

            if (!signature.isVoid) {
                currentState.push(DataEntry(Utils.UninitializedIndex, returnType ?: NullType.Mixed))
            }
        }
        incOffset()
    }

    override fun visitFieldInsn(p0: Int, p1: String?, p2: String?, p3: String?) {
        checkState()
        when (p0) {
            Opcodes.GETSTATIC,
            Opcodes.GETFIELD -> {
                if (p0 == Opcodes.GETFIELD) {
                    currentState.pop()
                }
                currentState.push(DataEntry(Utils.UninitializedIndex, finalFields.getOrDefault(p2, NullType.Mixed)))
            }
            Opcodes.PUTSTATIC,
            Opcodes.PUTFIELD -> {
                val currentDataEntry = currentState.pop()
                val currentFieldType = finalFields[p2]
                if (p2 != null && currentFieldType != null) {
                    finalFields[p2] = currentFieldType.merge(currentDataEntry.type)
                }
                if (p0 == Opcodes.PUTFIELD) {
                    currentState.pop()
                }
            }
        }
        incOffset()
    }

    override fun visitJumpInsn(p0: Int, p1: Label?) {
        checkState()
        when (p0) {
            Opcodes.IF_ICMPEQ,
            Opcodes.IF_ICMPNE,
            Opcodes.IF_ICMPLT,
            Opcodes.IF_ICMPGE,
            Opcodes.IF_ICMPGT,
            Opcodes.IF_ICMPLE,
            Opcodes.IF_ACMPEQ,
            Opcodes.IF_ACMPNE -> {
                currentState.pop()
                currentState.pop()
                currentState.condition = AnotherCondition(currentLine)
            }
            Opcodes.IFEQ,
            Opcodes.IFNE,
            Opcodes.IFLT,
            Opcodes.IFGE,
            Opcodes.IFGT,
            Opcodes.IFLE -> {
                currentState.pop()
                currentState.condition = AnotherCondition(currentLine)
            }
            Opcodes.GOTO -> {
                currentState.condition = null
            }
            Opcodes.IFNULL,
            Opcodes.IFNONNULL -> {
                val dataEntry = currentState.pop()
                val checkType = if (p0 == Opcodes.IFNULL) NullType.NotNull else NullType.Null
                val dataEntryType = dataEntry.type

                var conditionIsAlwaysTrue: Boolean? = null
                if (dataEntryType.isDefined()) {
                    conditionIsAlwaysTrue =
                        if (checkType == NullType.Null) dataEntryType == NullType.Null else dataEntryType == NullType.NotNull
                }
                else {
                    // TODO: support of complex nexted return condtions
                }

                val condition = if (conditionIsAlwaysTrue != null) {
                    logger.log(ExcessCheckMessage(conditionIsAlwaysTrue, currentLine))
                    if (conditionIsAlwaysTrue) null else AnotherCondition(currentLine)
                } else {
                    NullCheckCondition(currentLine, dataEntry.index, checkType)
                }

                currentState.condition = condition
            }
        }
        incOffset()
    }

    private fun checkState() {
        val cfgNode = currentState.cfgNode

        if (cfgNode != null && offset == cfgNode.end) {
            // Save state
            cfgNodeStates[cfgNode] = State(currentState, cfgNode)
        }

        var resultState: State? = null

        val nextCfgNode = cfgNodes[offset]
        if (nextCfgNode != null) {
            // Restore state
            for (link in nextCfgNode.links) {
                if (link.end == nextCfgNode) {
                    val prevState = cfgNodeStates[link.begin]
                    if (prevState != null) {
                        if (resultState == null) {
                            resultState = State(prevState, nextCfgNode)
                            currentState = resultState
                        }
                        else {
                            resultState.merge(prevState)
                        }
                    }
                }
            }
        }
    }
}