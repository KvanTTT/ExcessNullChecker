class DataEntry {
    var index: Int = -1
    var type: NullType = NullType.Mixed

    constructor(index: Int, type: NullType) {
        this.index = index
        this.type = type
    }

    constructor(type: NullType) {
        this.type = type
    }

    override fun toString(): String {
        return "{ $index, $type }"
    }
}