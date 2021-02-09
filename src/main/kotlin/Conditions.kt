abstract class Condition(val line: Int) {
}

class AnotherCondition(line: Int): Condition(line) {
    override fun toString(): String {
        return "another condition"
    }
}

class NullCheckCondition(line: Int, val varIndex: Int, val nullType: NullType): Condition(line) {
    override fun toString(): String {
        return "x${if (varIndex != -1) varIndex else "?"} == $nullType"
    }
}