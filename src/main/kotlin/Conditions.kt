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

class NullCheckCondition(line: Int, val varIndex: Int, val nullType: NullType): Condition(line) {
    override fun Invert(): Condition {
        return NullCheckCondition(line, varIndex, nullType.invert())
    }

    override fun toString(): String {
        return "x${if (varIndex != -1) varIndex else "?"} == $nullType"
    }

    fun isDefined(): Boolean {
        return varIndex >= 0 && nullType.isDefined()
    }
}