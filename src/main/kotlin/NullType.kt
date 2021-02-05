enum class NullType {
    Uninitialized,
    Mixed,
    Null,
    NotNull;

    fun isDefined(): Boolean {
        return this == Null || this == NotNull
    }
}