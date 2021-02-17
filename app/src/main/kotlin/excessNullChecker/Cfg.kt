package excessNullChecker

const val CfgReturnNodeIndex = -1

class CfgNode(val beginOffset: Int) {
    var endOffset: Int? = null
    var index: Int? = null
    val links: MutableList<CfgLink> = mutableListOf()

    fun getParentLinks(): List<CfgLink> {
        return links.filter { link -> link.endNode == this }
    }

    fun getChildLinks(): List<CfgLink> {
        return links.filter { link -> link.beginNode == this }
    }

    override fun toString(): String {
        if (beginOffset == CfgReturnNodeIndex)
            return "return"

        val suffix = if (index != null) "($index)" else ""
        return "$beginOffset-$endOffset$suffix"
    }
}

enum class CfgLinkType {
    True,
    False,
    Epsilon
}

class CfgLink(val beginNode: CfgNode, val endNode: CfgNode, val type: CfgLinkType) {
    override fun toString(): String {
        return "$beginNode -> $endNode, $type"
    }
}