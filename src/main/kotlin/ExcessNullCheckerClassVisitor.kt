import jdk.internal.org.objectweb.asm.*

class ExcessNullCheckerClassVisitor(l: Logger) : ClassVisitor(262144) {
    private val logger: Logger = l

    override fun visit(p0: Int, p1: Int, p2: String?, p3: String?, p4: String?, p5: Array<out String>?) {
        super.visit(p0, p1, p2, p3, p4, p5)
    }

    override fun visitSource(p0: String?, p1: String?) {
        super.visitSource(p0, p1)
    }

    override fun visitField(p0: Int, p1: String?, p2: String?, p3: String?, p4: Any?): FieldVisitor {
        return super.visitField(p0, p1, p2, p3, p4)
    }

    override fun visitMethod(p0: Int, p1: String?, p2: String?, p3: String?, p4: Array<out String>?): MethodVisitor {
        return ExcessNullCheckerMethodVisitor(logger, p0 == 9, Utils.getParamsCount(p2));
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