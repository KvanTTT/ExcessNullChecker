abstract class Condition(val line: Int) {
    abstract fun Invert(): Condition
}

class AnotherCondition(line: Int, val invert: Boolean = false): Condition(line) {
    override fun Invert(): Condition {
        return AnotherCondition(line, !invert)
    }

    override fun toString(): String {
        return "${if (invert) "not " else ""} another condition"
    }
}

class NullCheckCondition(line: Int, val name: String, val nullType: NullType): Condition(line) {
    override fun Invert(): Condition {
        return NullCheckCondition(line, name, nullType.invert())
    }

    override fun toString(): String {
        return "x${if (isDefined(name)) name else "?"} == $nullType"
    }

    fun isDefined(): Boolean {
        return isDefined(name) && nullType.isDefined()
    }
}