class ExcessCheckMessage(p: Boolean, l: Int) : Message(l) {
    val param: Boolean = p

    override fun toString(): String {
        return "Condition is always $param at $line"
    }

    override fun equals(other: Any?): Boolean {
        if (other !is ExcessCheckMessage)
            return false

        return this.param == other.param && this.line == other.line
    }

    override fun hashCode(): Int {
        return (if (this.param) 1 else 0) xor this.line
    }
}