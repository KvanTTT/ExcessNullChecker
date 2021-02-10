class State {
    val items: MutableList<DataEntry> = mutableListOf()
    val cfgNode: CfgNode?
    public var condition: Condition? = null

    constructor(otherStack: State, cfgNode: CfgNode?, condition: Condition?) : this(cfgNode) {
        for (item in otherStack.items)
            items.add(item)
        this.condition = condition
    }

    constructor(cfgNode: CfgNode?) {
        this.cfgNode = cfgNode
    }

    fun push(dataEntry: DataEntry) {
        items.add(dataEntry)
    }

    fun pop(): DataEntry {
        val lastInd = items.size - 1
        val result = items[lastInd]
        items.removeAt(lastInd)
        return result
    }

    fun peek(): DataEntry {
        return items[items.size - 1]
    }

    fun get(index: Int): DataEntry {
        return items[index]
    }

    fun set(name: String, dataEntry: DataEntry) {
        val index = name.toIntOrNull()
        if (index != null) {
            set(index, dataEntry)
        } else {
            // initialize temp field
        }
    }

    fun set(index: Int, dataEntry: DataEntry) {
        if (index == -1)
            return

        if (index >= items.size) {
            for (i in 0..index-items.size) {
                items.add(DataEntry(NullType.Uninitialized))
            }
        }
        items[index] = DataEntry(
            if (dataEntry.name == Uninitialized) index.toString() else
                dataEntry.name,
            dataEntry.type)
    }

    fun clear() {
        items.clear()
    }

    fun merge(otherStack: State) {
        for (i in 0 until minOf(items.size, otherStack.items.size)) {
            items[i] = items[i].merge(otherStack.items[i])
        }
    }
}