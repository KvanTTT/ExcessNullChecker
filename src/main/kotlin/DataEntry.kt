class DataEntry(val index: Int, val type: NullType) {
    fun isDefined(): Boolean {
        return index != -1 && type.isDefined()
    }

    fun merge(other: DataEntry): DataEntry {
        return DataEntry(
            when (index) {
                other.index -> index
                Utils.UninitializedIndex -> other.index
                else -> Utils.DirtyIndex
            },
            when (type) {
                other.type -> type
                NullType.Uninitialized -> other.type
                else -> NullType.Mixed
            }
        )
    }

    override fun toString(): String {
        return "{ $index, $type }"
    }
}