package excessNullChecker

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

enum class DataEntryType {
    Uninitialized,
    Other,
    Null,
    NotNull;

    fun isNullOrNotNull(): Boolean {
        return this == Null || this == NotNull
    }

    fun invert(): DataEntryType {
        return if (this == Null) NotNull else if (this == NotNull) Null else this
    }

    fun merge(otherDataEntryType: DataEntryType): DataEntryType {
        if (otherDataEntryType == Uninitialized)
            return this

        if (this == Uninitialized)
            return otherDataEntryType

        return if (this == otherDataEntryType) this else Other
    }
}

class DataEntry(val name: String, val type: DataEntryType) {
    constructor(name: Int, type: DataEntryType) : this(name.toString(), type) {
    }

    constructor(type: DataEntryType) : this(Uninitialized, type) {
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