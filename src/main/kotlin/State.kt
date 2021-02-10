class State {
    val stack: MutableList<DataEntry> = mutableListOf()
    val fields: MutableMap<String, DataEntry> = mutableMapOf()
    val cfgNode: CfgNode?
    var condition: Condition? = null

    constructor(anotherState: State, cfgNode: CfgNode?, condition: Condition?) : this(cfgNode) {
        for (item in anotherState.stack)
            stack.add(item)
        for (field in anotherState.fields)
            fields[field.key] = field.value
        this.condition = condition
    }

    constructor(cfgNode: CfgNode?) {
        this.cfgNode = cfgNode
    }

    fun push(dataEntry: DataEntry) {
        stack.add(dataEntry)
    }

    fun pop(): DataEntry {
        val lastInd = stack.size - 1
        val result = stack[lastInd]
        stack.removeAt(lastInd)
        return result
    }

    fun peek(): DataEntry {
        return stack[stack.size - 1]
    }

    fun get(index: Int): DataEntry {
        return stack[index]
    }

    fun getField(name: String): DataEntry? {
        return fields[name]
    }

    fun set(name: String, dataEntry: DataEntry) {
        val index = name.toIntOrNull()
        if (index != null) {
            set(index, dataEntry)
        } else if (isDefined(name)) {
            setField(name, dataEntry)
        }
    }

    fun setField(name: String, dataEntry: DataEntry) {
        fields[name] = DataEntry(
            if (dataEntry.name == Uninitialized) name else
                dataEntry.name,
            dataEntry.type)
    }

    fun set(index: Int, dataEntry: DataEntry) {
        if (index == -1)
            return

        if (index >= stack.size) {
            for (i in 0..index-stack.size) {
                stack.add(DataEntry(NullType.Uninitialized))
            }
        }
        stack[index] = DataEntry(
            if (dataEntry.name == Uninitialized) index.toString() else
                dataEntry.name,
            dataEntry.type)
    }

    fun clear() {
        stack.clear()
    }

    fun merge(otherStack: State) {
        for (i in 0 until minOf(stack.size, otherStack.stack.size)) {
            stack[i] = stack[i].merge(otherStack.stack[i])
        }
        for (item in otherStack.fields) {
            val field = fields[item.key]
            if (field != null) {
                fields[item.key] = field.merge(item.value)
            }
        }
    }
}