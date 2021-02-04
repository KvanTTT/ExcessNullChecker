package visitors

import Logger
import NullType
import Utils
import jdk.internal.org.objectweb.asm.*

class ClassAnalyzer(private val finalFields: MutableMap<String, NullType?>, private val logger: Logger) : ClassVisitor(Opcodes.ASM5) {

    override fun visit(p0: Int, p1: Int, p2: String?, p3: String?, p4: String?, p5: Array<out String>?) {
        super.visit(p0, p1, p2, p3, p4, p5)
    }

    override fun visitSource(p0: String?, p1: String?) {
        super.visitSource(p0, p1)
    }

    override fun visitMethod(p0: Int, p1: String?, p2: String?, p3: String?, p4: Array<out String>?): MethodVisitor {
        val isStatic = (p0 and Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC
        return MethodAnalyzer(isStatic, Utils.getParamsCount(p2), finalFields, logger);
    }

    override fun visitAnnotation(p0: String?, p1: Boolean): AnnotationVisitor {
        return super.visitAnnotation(p0, p1)
    }

    override fun visitInnerClass(p0: String?, p1: String?, p2: String?, p3: Int) {
        super.visitInnerClass(p0, p1, p2, p3)
    }

    override fun visitOuterClass(p0: String?, p1: String?, p2: String?) {
        super.visitOuterClass(p0, p1, p2)
    }

    override fun visitEnd() {
        super.visitEnd()
    }
}