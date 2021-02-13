package excessNullChecker.visitors

import excessNullChecker.*
import org.objectweb.asm.*

class CodeAnalyzer(
    private val context: Context,
    private val signature: Signature
) : AdvancedVisitor() {
    private var currentState: State
    private val cfgNodes: Map<Int, CfgNode> = context.methodsCfg[signature.fullName] ?: throw Exception("Cfg not found for ${signature.fullName}")
    private val cfgNodeStates: MutableMap<CfgNode, State> = mutableMapOf()

    init {
        currentState = State(cfgNodes[0])
        var offset = 0
        if (!signature.static) {
            // First data entry is always not null for instance methods
            offset = 1
            currentState.push(DataEntry(0, DataEntryType.NotNull))
        }
        for (i in 0 until signature.paramsCount) {
            // Initialize local variables from parameters
            currentState.push(DataEntry(i + offset, DataEntryType.Other))
        }
        for (field in context.fields) {
            val processedFinalField = context.processedFinalFields[field.key]
            currentState.setField(field.key, DataEntry(field.key, processedFinalField ?: DataEntryType.Other))
        }
    }

    override fun visitVarInsn(p0: Int, p1: Int) {
        checkState()
        when (p0)  {
            Opcodes.ILOAD,
            Opcodes.LLOAD,
            Opcodes.FLOAD,
            Opcodes.DLOAD -> {
                currentState.push(DataEntry(p1, DataEntryType.Other))
            }
            Opcodes.ALOAD -> currentState.push(currentState.getLocal(p1))

            Opcodes.ISTORE,
            Opcodes.LSTORE,
            Opcodes.FSTORE,
            Opcodes.DSTORE -> {
                currentState.setLocal(p1, DataEntry(DataEntryType.Other))
            }
            Opcodes.ASTORE -> {
                currentState.setLocal(p1, currentState.pop())
            }

            else -> throwUnsupportedOpcode(p0)
        }
        incOffset()
    }

    override fun visitInsn(p0: Int) {
        checkState()
        when (p0) {
            Opcodes.ACONST_NULL -> {
                currentState.push(DataEntry(DataEntryType.Null))
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
                currentState.push(DataEntry(DataEntryType.Other))
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
                val currentDataEntry =
                    if (p0 != Opcodes.RETURN) currentState.pop() else DataEntry(Dirty, DataEntryType.Other)

                val methodReturnEntry = context.processedMethods[signature.fullName]
                if (methodReturnEntry != null)
                    context.processedMethods[signature.fullName] = methodReturnEntry.merge(currentDataEntry)

                currentState.clear()
            }
            Opcodes.ATHROW -> {
                val objectRef = currentState.pop()
                currentState.clear()
                currentState.push(objectRef)
            }
            Opcodes.IADD,
            Opcodes.LADD,
            Opcodes.FADD,
            Opcodes.DADD,
            Opcodes.IMUL,
            Opcodes.LMUL,
            Opcodes.FMUL,
            Opcodes.DMUL,
            Opcodes.IDIV,
            Opcodes.LDIV,
            Opcodes.FDIV,
            Opcodes.DDIV,
            Opcodes.IREM,
            Opcodes.LREM,
            Opcodes.FREM,
            Opcodes.DREM,
            Opcodes.ISUB,
            Opcodes.LSUB,
            Opcodes.FSUB,
            Opcodes.DSUB,
            Opcodes.ISHL,
            Opcodes.LSHL,
            Opcodes.ISHR,
            Opcodes.LSHR,
            Opcodes.IUSHR,
            Opcodes.IAND,
            Opcodes.LAND,
            Opcodes.IOR,
            Opcodes.LOR,
            Opcodes.IXOR,
            Opcodes.LXOR,
            Opcodes.LCMP,
            Opcodes.FCMPL,
            Opcodes.FCMPG,
            Opcodes.DCMPL,
            Opcodes.DCMPG -> {
                currentState.pop()
                currentState.pop()
                currentState.push(DataEntry(DataEntryType.Other))
            }
            Opcodes.INEG,
            Opcodes.LNEG,
            Opcodes.FNEG,
            Opcodes.DNEG,
            Opcodes.I2L,
            Opcodes.I2F,
            Opcodes.I2D,
            Opcodes.L2I,
            Opcodes.L2F,
            Opcodes.L2D,
            Opcodes.F2I,
            Opcodes.F2L,
            Opcodes.F2D,
            Opcodes.D2I,
            Opcodes.D2L,
            Opcodes.D2F,
            Opcodes.I2B,
            Opcodes.I2C,
            Opcodes.I2S -> {
                currentState.pop()
                currentState.push(DataEntry(DataEntryType.Other))
            }
            Opcodes.ARRAYLENGTH,
            Opcodes.IALOAD,
            Opcodes.LALOAD,
            Opcodes.FALOAD,
            Opcodes.DALOAD,
            Opcodes.AALOAD,
            Opcodes.BALOAD,
            Opcodes.CALOAD,
            Opcodes.SALOAD, -> {
                // TODO: https://github.com/KvanTTT/ExcessNullChecker/issues/7
                if (p0 != Opcodes.ARRAYLENGTH)
                    currentState.pop() // index
                popInstanceAndSetToNotNull()
                currentState.push(DataEntry(DataEntryType.Other)) // put array length or other value
            }
            Opcodes.IASTORE,
            Opcodes.LASTORE,
            Opcodes.FASTORE,
            Opcodes.DASTORE,
            Opcodes.AASTORE,
            Opcodes.BASTORE,
            Opcodes.CASTORE,
            Opcodes.SASTORE, -> {
                // TODO: https://github.com/KvanTTT/ExcessNullChecker/issues/7
                currentState.pop() // value
                currentState.pop() // index
                popInstanceAndSetToNotNull()
            }
            Opcodes.POP -> {
                currentState.pop()
            }
            Opcodes.POP2 -> {
                currentState.pop()
                currentState.pop()
            }
            Opcodes.DUP_X1 -> {
                val value1 = currentState.pop()
                val value2 = currentState.pop()
                currentState.push(value1)
                currentState.push(value2)
                currentState.push(value1)
            }
            Opcodes.DUP_X2 -> {
                val value1 = currentState.pop()
                val value2 = currentState.pop()
                val value3 = currentState.pop()
                currentState.push(value1)
                currentState.push(value3)
                currentState.push(value2)
                currentState.push(value1)
            }
            else -> throwUnsupportedOpcode(p0)
        }
        incOffset()
    }

    override fun visitIntInsn(p0: Int, p1: Int) {
        checkState()
        when (p0) {
            Opcodes.BIPUSH,
            Opcodes.SIPUSH -> {
                currentState.push(DataEntry(DataEntryType.Other))
            }
            Opcodes.NEWARRAY -> {
                currentState.pop()
                currentState.push(DataEntry(DataEntryType.NotNull))
            }
            else -> throwUnsupportedOpcode(p0)
        }
        incOffset()
    }

    override fun visitLdcInsn(p0: Any?) {
        checkState()
        currentState.push(DataEntry(DataEntryType.NotNull))
        incOffset()
    }

    override fun visitTypeInsn(p0: Int, p1: String?) {
        checkState()
        when (p0) {
            Opcodes.NEW,
            Opcodes.ANEWARRAY -> {
                if (p0 != Opcodes.NEW)
                    currentState.pop() // pop array length
                currentState.push(DataEntry(DataEntryType.NotNull))
            }
            Opcodes.CHECKCAST -> {
                val objectRef = currentState.pop()
                currentState.push(objectRef)
            }
            Opcodes.INSTANCEOF -> {
                currentState.pop() // TODO: https://github.com/KvanTTT/ExcessNullChecker/issues/8
                currentState.push(DataEntry(DataEntryType.Other))
            }
            else -> throwUnsupportedOpcode(p0)
        }
        incOffset()
    }

    override fun visitMethodInsn(p0: Int, p1: String?, p2: String?, p3: String?, p4: Boolean) {
        checkState()
        val isStatic = p0 == Opcodes.INVOKESTATIC
        val signature = Signature.get(isStatic, p2, p3)
        if (p0 == Opcodes.INVOKEVIRTUAL || p0 == Opcodes.INVOKESPECIAL || p0 == Opcodes.INVOKESTATIC ||
            p0 == Opcodes.INVOKEINTERFACE) {
            // Make virtual call and remove parameters from stack except of the first one
            val params = mutableListOf<DataEntry>()
            for (i in 0 until signature.paramsCount) {
                params.add(currentState.pop())
            }

            if (p0 != Opcodes.INVOKESTATIC) {
                popInstanceAndSetToNotNull()
            }

            var returnDataEntry: DataEntry? = null
            var returnType: DataEntryType = DataEntryType.Other
            if (context.methodsCfg.containsKey(signature.fullName)) {
                returnDataEntry = context.processedMethods[signature.fullName]
                if (returnDataEntry == null) {
                    // Recursive analysing...
                    val methodAnalyzer = MethodAnalyzer(context, BypassType.All, signature.fullName)
                    val classReader = ClassReader(context.bytes)
                    classReader.accept(methodAnalyzer, 0)
                    returnDataEntry = context.processedMethods[signature.fullName]
                }
                returnType = returnDataEntry?.type ?: DataEntryType.Other
            }

            // Try to link passed param with return value
            if (returnDataEntry != null && returnType == DataEntryType.Other) {
                val linkedLocalVar = returnDataEntry.name.toIntOrNull() // Only local variables are relevant
                if (linkedLocalVar != null) {
                    returnType = params.getOrNull(linkedLocalVar)?.type ?: DataEntryType.Other
                }
            }

            if (!signature.isVoid) {
                currentState.push(DataEntry(Uninitialized, returnType))
            }
        } else {
            throwUnsupportedOpcode(p0)
        }
        incOffset()
    }

    override fun visitFieldInsn(p0: Int, p1: String?, p2: String?, p3: String?) {
        checkState()
        when (p0) {
            Opcodes.GETSTATIC,
            Opcodes.GETFIELD -> {
                if (p0 == Opcodes.GETFIELD) {
                    popInstanceAndSetToNotNull()
                }
                if (p2 != null) {
                    val dataEntry = currentState.getField(p2)
                    currentState.push(dataEntry ?: DataEntry(Dirty, DataEntryType.Other))
                }
            }
            Opcodes.PUTSTATIC,
            Opcodes.PUTFIELD -> {
                val dataEntry = currentState.pop()
                if (p2 != null) {
                    currentState.setField(p2, dataEntry)
                    val finalField = context.processedFinalFields[p2]
                    if (finalField != null) {
                        context.processedFinalFields[p2] = finalField.merge(dataEntry.type)
                    }
                }
                if (p0 == Opcodes.PUTFIELD) {
                    popInstanceAndSetToNotNull()
                }
            }
            else -> throwUnsupportedOpcode(p0)
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
                currentState.condition = EmptyCondition(currentLine)
            }
            Opcodes.IFNULL,
            Opcodes.IFNONNULL -> {
                val dataEntry = currentState.pop()
                val checkType = if (p0 == Opcodes.IFNULL) DataEntryType.Null else DataEntryType.NotNull
                val dataEntryType = dataEntry.type

                var conditionIsAlwaysTrue: Boolean? = null
                if (dataEntryType.isNullOrNotNull()) {
                    conditionIsAlwaysTrue =
                        if (checkType == DataEntryType.Null) dataEntryType == DataEntryType.NotNull
                            else dataEntryType == DataEntryType.Null
                }

                val condition = if (conditionIsAlwaysTrue != null) {
                    context.logger.info(ExcessCheckMessage(conditionIsAlwaysTrue, currentLine))
                    if (conditionIsAlwaysTrue) null else AnotherCondition(currentLine)
                } else {
                    NullCheckCondition(currentLine, dataEntry.name, checkType)
                }

                currentState.condition = condition
            }
            else -> throwUnsupportedOpcode(p0)
        }
        incOffset()
    }

    override fun visitIincInsn(p0: Int, p1: Int) {
        checkState()
        incOffset()
    }

    override fun visitMultiANewArrayInsn(p0: String?, p1: Int) {
        checkState()
        for (i in 0 until p1)
            currentState.pop() // Extract arrays length
        currentState.push(DataEntry(DataEntryType.NotNull))
        incOffset()
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

    private fun popInstanceAndSetToNotNull() {
        // Mark variable as NotNull because instance is always necessary during invocation
        // a.getHashCode()
        // if (a == null) // prevent excess check
        val instance = currentState.pop()
        currentState.set(instance.name, DataEntry(instance.name, DataEntryType.NotNull))
    }

    private fun checkState() {
        val cfgNode = currentState.cfgNode

        if (cfgNode != null && offset == cfgNode.end) {
            // Save state
            if (currentState.condition == null)
                currentState.condition = EmptyCondition(currentLine)
            cfgNodeStates[cfgNode] = State(currentState, cfgNode, currentState.condition)
        }

        val nextCfgNode = cfgNodes[offset]
        if (nextCfgNode != null && offset > 0) {
            // Restore state

            var resultState: State? = null

            val parentLinks = nextCfgNode.getParentLinks()
            for (link in parentLinks) {
                val prevState = cfgNodeStates[link.begin]
                if (prevState != null) {
                    if (resultState == null) {
                        resultState = State(prevState, nextCfgNode, null)
                        currentState = resultState
                    } else {
                        resultState.merge(prevState)
                    }
                }
            }

            if (resultState == null) {
                throw Exception("resultState should be initialized")
            }

            // Set state of outer block after inner return statement
            if (parentLinks.size == 1) {
                val link = parentLinks[0]
                val condition = cfgNodeStates[link.begin]?.condition
                if (condition is NullCheckCondition && condition.isDefined()) {
                    resultState.set(condition.name, DataEntry(condition.name,
                        if (link.type == CfgLinkType.False) condition.dataEntryType.invert() else condition.dataEntryType))
                }
            }
            else if (parentLinks.size == 2) {
                // Check the following:
                // if (a == null)
                //     a = new Object();
                // if (a == null) { // Test: condition_is_always_false
                // }

                val firstState = cfgNodeStates[parentLinks[0].begin]
                val secondState = cfgNodeStates[parentLinks[1].begin]
                val firstCondition = firstState?.condition
                val secondCondition = secondState?.condition
                var varCheckCondition: NullCheckCondition? = null
                var varAssignState: State? = null
                if (firstCondition is NullCheckCondition && firstCondition.isDefined() && secondCondition is EmptyCondition) {
                    varCheckCondition = firstCondition
                    varAssignState = secondState
                } else if (secondCondition is NullCheckCondition && secondCondition.isDefined() && firstCondition is EmptyCondition) {
                    varCheckCondition = secondCondition
                    varAssignState = firstState
                }
                if (varCheckCondition != null && varAssignState != null) {
                    val dataEntry = varAssignState.get(varCheckCondition.name)
                    if (dataEntry?.name == varCheckCondition.name) {
                        var finalDataEntryType: DataEntryType = DataEntryType.Other
                        if (dataEntry.type == DataEntryType.Null && varCheckCondition.dataEntryType == DataEntryType.Null) {
                            finalDataEntryType = DataEntryType.Null
                        } else if (dataEntry.type == DataEntryType.NotNull && varCheckCondition.dataEntryType == DataEntryType.NotNull) {
                            finalDataEntryType = DataEntryType.NotNull
                        }

                        if (finalDataEntryType.isNullOrNotNull()) {
                            resultState.set(dataEntry.name, DataEntry(dataEntry.name, finalDataEntryType))
                        }
                    }
                }
            }
        }
    }
}