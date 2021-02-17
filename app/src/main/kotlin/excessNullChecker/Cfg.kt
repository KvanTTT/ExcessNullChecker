package excessNullChecker

const val CfgReturnNodeIndex = -1

class CfgNode(val begin: Int) {
    var end: Int? = null
    val links: MutableList<CfgLink> = mutableListOf()

    fun getParentLinks(): List<CfgLink> {
        return links.filter { link -> link.end == this }
    }

    override fun toString(): String {
        return if (begin != CfgReturnNodeIndex) "$begin-$end" else "return"
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