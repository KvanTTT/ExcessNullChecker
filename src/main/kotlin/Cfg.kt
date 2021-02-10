const val CfgReturnIndex = -1

class CfgNode(val begin: Int) {
    public var end: Int? = null
    public val links: MutableList<CfgLink> = mutableListOf()

    override fun toString(): String {
        return if (begin != CfgReturnIndex) "$begin-$end" else "return"
    }
}

enum class CfgLinkType {
    True,
    False,
    Epsilon
}

class CfgLink(val begin: CfgNode, val end: CfgNode, val type: CfgLinkType) {
    override fun toString(): String {
        return "$begin -> $end, $type"
    }
}