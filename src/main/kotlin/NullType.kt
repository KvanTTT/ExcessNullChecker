enum class NullType {
    Uninitialized,
    Mixed,
    Null,
    NotNull;

    fun isDefined(): Boolean {
        return this == Null || this == NotNull
    }

    fun invert(): NullType {
        return if (this == Null) NotNull else if (this == NotNull) Null else this
    }

    fun merge(otherNullType: NullType): NullType {
        if (otherNullType == Uninitialized)
            return this

        if (this == Uninitialized)
            return otherNullType

        return if (this == otherNullType) this else Mixed
    }
}