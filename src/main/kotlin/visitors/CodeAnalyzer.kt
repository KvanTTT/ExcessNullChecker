package visitors

import DataEntry
import ExcessCheckMessage
import Logger
import NullType
import Signature
import jdk.internal.org.objectweb.asm.*

class CodeAnalyzer(
    private val signature: Signature,
    private val finalFields: MutableMap<String, NullType>,
    private val processedMethods: MutableMap<String, NullType>,
    private val availableMethods: Set<String>,
    private val classFileData: ByteArray,
    private val logger: Logger
) : MethodVisitor(Opcodes.ASM5) {
    private var currentLine: Int = -1
    private val stack: MutableList<DataEntry> = mutableListOf()
    private val resetStates: MutableMap<Label, MutableList<DataEntry>> = mutableMapOf()
    private val returnStates: MutableList<MutableList<NullType>> = mutableListOf()

    init {
        var offset = 0
        if (!signature.static) {
            // First data entry is always not null for instance methods
            offset = 1
            push(DataEntry(0, NullType.NotNull))
        }
        for (i in 0 until signature.paramsCount) {
            // Initialize local variables from parameters
            push(DataEntry(i + offset, NullType.Mixed))
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
            Opcodes.IRETURN,
            Opcodes.DRETURN,
            Opcodes.FRETURN,
            Opcodes.LRETURN -> {
            }
            Opcodes.ARETURN -> {
                val currentDataEntry = pop()
                val methodReturnType = processedMethods[signature.fullName]
                processedMethods[signature.fullName] = when {
                    methodReturnType == NullType.Uninitialized -> currentDataEntry.type // if uninitialized
                    methodReturnType != currentDataEntry.type -> NullType.Mixed
                    else -> methodReturnType
                }
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
        val stateAtLabel = resetStates[p0]
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
        val isStatic = p0 == Opcodes.INVOKESTATIC
        val signature = Signature.get(isStatic, p2, p3)
        if (p0 == Opcodes.INVOKEVIRTUAL || p0 == Opcodes.INVOKESPECIAL || p0 == Opcodes.INVOKESTATIC) {
            // Make virtual call and remove parameters from stack except of the first one
            for (i in 0 until signature.paramsCount) {
                pop()
            }

            if (p0 != Opcodes.INVOKESTATIC) {
                // Mark variable as NotNull because instance is always necessary during invocation
                // a.getHashCode()
                // if (a == null) // prevent excess check
                val invocationDataEntry = pop()
                set(invocationDataEntry.index, NullType.NotNull)
            }

            var returnType: NullType? = null
            if (availableMethods.contains(signature.fullName)) {
                returnType = processedMethods[signature.fullName]
                if (returnType == null) {
                    // Recursive analysing...
                    val methodAnalyzer = MethodAnalyzer(
                        BypassType.All,
                        finalFields,
                        processedMethods,
                        availableMethods,
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
                push(DataEntry(returnType ?: NullType.Mixed))
            }
        }
    }

    override fun visitFieldInsn(p0: Int, p1: String?, p2: String?, p3: String?) {
        when (p0) {
            Opcodes.GETSTATIC -> {
                push(DataEntry(NullType.NotNull))
            }
            Opcodes.GETFIELD -> {
                push(DataEntry(finalFields.getOrDefault(p2, NullType.Mixed)))
            }
            Opcodes.PUTFIELD -> {
                val currentDataEntry = pop()
                val currentFieldType = finalFields[p2]
                if (p2 != null && currentFieldType != null) {
                    finalFields[p2] = when {
                        currentFieldType == NullType.Uninitialized -> currentDataEntry.type // if uninitialized
                        currentFieldType != currentDataEntry.type -> NullType.Mixed
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
        val currentDataEntry = pop()
        if ((p0 == Opcodes.IFNULL || p0 == Opcodes.IFNONNULL) && p1 != null) {
            val currentNullType = if (p0 == Opcodes.IFNULL) {
                NullType.NotNull
            } else {
                NullType.Null
            }
            val currentValueNullType = currentDataEntry.type
            val currentDataEntryIndex = currentDataEntry.index

            var oppositeNullType: NullType
            if (currentValueNullType.isDefined()) {
                oppositeNullType = currentValueNullType
            }
            else
            {
                oppositeNullType = checkReturnReachability(currentDataEntryIndex)
                if (oppositeNullType.isDefined()) {
                    oppositeNullType = when (oppositeNullType) {
                        NullType.Null -> NullType.NotNull
                        NullType.NotNull -> NullType.Null
                        else -> NullType.Mixed
                    }
                }
            }

            if (oppositeNullType.isDefined()) {
                val expressionIsAlwaysTrue =
                    if (oppositeNullType == NullType.Null) currentNullType == NullType.Null
                    else currentNullType == NullType.NotNull
                val diagnostics = ExcessCheckMessage(expressionIsAlwaysTrue, currentLine)
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
        currentLine = p0
    }

    private fun checkReturnReachability(currentVarIndex: Int): NullType {
        var oppositeDataEntryState: NullType = NullType.Mixed
        for (returnState in returnStates) {
            var next = false
            for (i in 0 until returnState.size) {
                if (i != currentVarIndex) { // Check it later
                    if (returnState[i] != stack[i].type) {
                        next = true
                        break
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
                stack.add(DataEntry(NullType.Mixed))
            }
        }
        stack[index].index = index
        stack[index].type = nullType
    }

    private fun push(v: DataEntry) {
        stack.add(v)
    }

    private fun pop(): DataEntry {
        val result = stack[stack.size - 1]
        stack.removeAt(stack.size - 1)
        return result
    }
}