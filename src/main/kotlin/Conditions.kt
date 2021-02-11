abstract class Condition(val line: Int) {
    abstract fun Invert(): Condition
}

class EmptyCondition(line: Int): Condition(line) {
    override fun Invert(): Condition {
        throw Exception("Empty conditional can not be inverted")
    }
}

class AnotherCondition(line: Int, val invert: Boolean = false): Condition(line) {
    override fun Invert(): Condition {
        return AnotherCondition(line, !invert)
    }

    override fun toString(): String {
        return "${if (invert) "not " else ""} another condition"
    }
}

class NullCheckCondition(line: Int, val name: String, val dataEntryType: DataEntryType): Condition(line) {
    override fun Invert(): Condition {
        return NullCheckCondition(line, name, dataEntryType.invert())
    }

    override fun toString(): String {
        return "x${if (isNullOrNotNull(name)) name else "?"} == $dataEntryType"
    }

    fun isDefined(): Boolean {
        return isNullOrNotNull(name) && dataEntryType.isNullOrNotNull()
    }
}