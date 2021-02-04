package visitors

import DataEntry
import ExcessCheckMessage
import Logger
import NullType
import Utils
import jdk.internal.org.objectweb.asm.*

class MethodAnalyzer : MethodVisitor {
    private var currentLine: Int = -1
    private val stack: MutableList<DataEntry> = mutableListOf()
    private val resetStates: MutableMap<Label, MutableList<DataEntry>> = mutableMapOf()
    private val returnStates: MutableList<MutableList<NullType>> = mutableListOf()
    private val finalFields: MutableMap<String, NullType?>
    private val logger: Logger

    constructor(isStatic: Boolean, paramCount: Int, finalFields: MutableMap<String, NullType?>, logger: Logger) : super(Opcodes.ASM5) {
        this.finalFields = finalFields
        this.logger = logger
        var offset = 0
        if (!isStatic) {
            // First data entry is always not null for instance methods
            offset = 1
            push(DataEntry(0, NullType.NotNull))
        }
        for (i in 0 until paramCount) {
            // Initialize local variables from parameters
            push(DataEntry(i + offset, NullType.Unknown))
        }
    }

    override fun visitAnnotation(p0: String?, p1: Boolean): AnnotationVisitor {
        return super.visitAnnotation(p0, p1)
    }

    override fun visitCode() {
    }

    override fun visitEnd() {
    }

    override fun visitVarInsn(p0: Int, p1: Int) {
        if (p0 == Opcodes.ALOAD) {
            push(stack[p1])
        }
        else if (p0 == Opcodes.ASTORE) {
            set(p1, pop().type)
        }
    }

    override fun visitInsn(p0: Int) {
        when (p0) {
            Opcodes.ACONST_NULL -> {
                push(DataEntry(NullType.Null))
            }
            Opcodes.RETURN -> {
                // Store reachability at the current return point and check it during IFNULL or IFNONNULL checks
                val returnState: MutableList<NullType> = mutableListOf()
                for (item in stack) {
                    returnState.add(item.type)
                }
                returnStates.add(returnState)
            }
            Opcodes.DUP -> {
                push(stack[stack.size - 1])
            }
        }
    }

    override fun visitLdcInsn(p0: Any?) {
        push(DataEntry(NullType.NotNull))
    }

    override fun visitParameterAnnotation(p0: Int, p1: String?, p2: Boolean): AnnotationVisitor {
        return super.visitParameterAnnotation(p0, p1, p2)
    }

    override fun visitLabel(p0: Label?) {
        // Restore the previous state of data entries after condition block
        var stateAtLabel = resetStates[p0]
        if (stateAtLabel != null) {
            for (cond in stateAtLabel) {
                set(cond.index, cond.type)
            }
        }
    }

    override fun visitTypeAnnotation(p0: Int, p1: TypePath?, p2: String?, p3: Boolean): AnnotationVisitor {
        return super.visitTypeAnnotation(p0, p1, p2, p3)
    }

    override fun visitMultiANewArrayInsn(p0: String?, p1: Int) {
        super.visitMultiANewArrayInsn(p0, p1)
    }

    override fun visitInsnAnnotation(p0: Int, p1: TypePath?, p2: String?, p3: Boolean): AnnotationVisitor {
        return super.visitInsnAnnotation(p0, p1, p2, p3)
    }

    override fun visitIincInsn(p0: Int, p1: Int) {
        super.visitIincInsn(p0, p1)
    }

    override fun visitTypeInsn(p0: Int, p1: String?) {
        if (p0 == Opcodes.NEW) {
            push(DataEntry(NullType.NotNull))
        }
    }

    override fun visitMethodInsn(p0: Int, p1: String?, p2: String?, p3: String?, p4: Boolean) {
        var paramsCount = Utils.getParamsCount(p3)
        if (p0 == Opcodes.INVOKEVIRTUAL || p0 == Opcodes.INVOKESPECIAL) {
            // Make virtual call and remove parameters from stack except of the first one
            for (i in 1..paramsCount)
                pop()

            // Mark variable as NotNull because instance is always necessary during invocation
            // a.getHashCode()
            // if (a == null) // prevent excess check
            var invocationDataEntry = pop()
            set(invocationDataEntry.index, NullType.NotNull)

            push(DataEntry(if (p0 == Opcodes.INVOKESPECIAL) NullType.NotNull else NullType.Unknown))
        }
    }

    override fun visitFieldInsn(p0: Int, p1: String?, p2: String?, p3: String?) {
        when (p0) {
            Opcodes.GETSTATIC -> {
                push(DataEntry(NullType.NotNull))
            }
            Opcodes.GETFIELD -> {
                push(DataEntry(finalFields.getOrDefault(p2, NullType.Unknown) ?: NullType.Unknown))
            }
            Opcodes.PUTFIELD -> {
                var currentDataEntry = pop()
                if (p2 != null && finalFields.containsKey(p2)) {
                    var currentFieldType = finalFields[p2]
                    finalFields[p2] = when {
                        currentFieldType == null -> currentDataEntry.type
                        currentFieldType != currentDataEntry.type -> NullType.Unknown
                        else -> currentFieldType
                    }
                }
            }
        }
    }

    override fun visitLocalVariable(p0: String?, p1: String?, p2: String?, p3: Label?, p4: Label?, p5: Int) {
        super.visitLocalVariable(p0, p1, p2, p3, p4, p5)
    }

    override fun visitIntInsn(p0: Int, p1: Int) {
        super.visitIntInsn(p0, p1)
    }

    override fun visitJumpInsn(p0: Int, p1: Label?) {
        var currentDataEntry = pop()
        if ((p0 == Opcodes.IFNULL || p0 == Opcodes.IFNONNULL) && p1 != null) {
            var currentNullType = if (p0 == Opcodes.IFNULL) {
                NullType.NotNull
            } else {
                NullType.Null
            }
            var currentValueNullType = currentDataEntry.type
            var currentDataEntryIndex = currentDataEntry.index

            var oppositeNullType: NullType
            if (currentValueNullType != NullType.Unknown) {
                oppositeNullType = currentValueNullType
            }
            else
            {
                oppositeNullType = checkReturnReachability(currentDataEntryIndex)
                if (oppositeNullType != NullType.Unknown) {
                    oppositeNullType = if (oppositeNullType == NullType.Null) NullType.NotNull else NullType.Null
                }
            }

            if (oppositeNullType != NullType.Unknown) {
                var expressionIsAlwaysTrue =
                    if (oppositeNullType == NullType.Null) currentNullType == NullType.Null
                    else currentNullType == NullType.NotNull
                var diagnostics = ExcessCheckMessage(expressionIsAlwaysTrue, currentLine)
                logger.log(diagnostics)
            }

            var stateAtLabel = resetStates[p1]
            if (stateAtLabel == null) {
                stateAtLabel = mutableListOf()
                resetStates[p1] = stateAtLabel
            }
            // save previous data entry states for further restoring
            stateAtLabel.add(DataEntry(currentDataEntryIndex, currentValueNullType))

            set(currentDataEntryIndex, currentNullType)
        }
    }

    override fun visitParameter(p0: String?, p1: Int) {
        super.visitParameter(p0, p1)
    }

    override fun visitInvokeDynamicInsn(p0: String?, p1: String?, p2: Handle?, vararg p3: Any?) {
        super.visitInvokeDynamicInsn(p0, p1, p2, *p3)
    }

    override fun visitFrame(p0: Int, p1: Int, p2: Array<out Any>?, p3: Int, p4: Array<out Any>?) {
        super.visitFrame(p0, p1, p2, p3, p4)
    }

    override fun visitLineNumber(p0: Int, p1: Label?) {
        currentLine = p0;
    }

    private fun checkReturnReachability(currentVarIndex: Int): NullType {
        var oppositeDataEntryState: NullType = NullType.Unknown
        for (returnState in returnStates) {
            var next = false
            for (i in 0 until returnState.size) {
                if (i != currentVarIndex) { // Check it later
                    if (returnState[i] != stack[i].type) {
                        next = true
                        break;
                    }
                }
                else {
                    oppositeDataEntryState = returnState[i]
                }
            }
            if (!next)
                return oppositeDataEntryState
        }
        return oppositeDataEntryState
    }

    private fun set(index: Int, nullType: NullType) {
        if (index == -1)
            return

        if (index >= stack.size) {
            for (i in 0..index-stack.size) {
                stack.add(DataEntry(NullType.Unknown))
            }
        }
        stack[index].index = index
        stack[index].type = nullType
    }

    private fun push(v: DataEntry) {
        stack.add(v);
    }

    private fun pop(): DataEntry {
        var result = stack[stack.size - 1]
        stack.removeAt(stack.size - 1)
        return result
    }
}