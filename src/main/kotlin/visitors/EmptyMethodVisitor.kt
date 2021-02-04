package visitors

import jdk.internal.org.objectweb.asm.MethodVisitor
import jdk.internal.org.objectweb.asm.Opcodes

class EmptyMethodVisitor : MethodVisitor(Opcodes.ASM5) {
}