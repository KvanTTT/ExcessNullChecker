const val Uninitialized = "<Uninitialized>"
const val Dirty = "<Dirty>"

fun isDefined(varName: String): Boolean {
    return varName != Uninitialized && varName != Dirty
}

fun mergeNames(name1: String, name2: String): String {
    if (name1 == Uninitialized)
        return name2

    if (name2 == Uninitialized)
        return name1

    return if (name1 == name2) name1 else Dirty
}

class DataEntry(val name: String, val type: NullType) {
    constructor(name: Int, type: NullType) : this(name.toString(), type) {
    }

    constructor(type: NullType) : this(Uninitialized, type) {
    }

    fun merge(other: DataEntry): DataEntry {
        return DataEntry(
            mergeNames(name, other.name),
            type.merge(other.type)
        )
    }

    override fun toString(): String {
        return "{ $name, $type }"
    }
}