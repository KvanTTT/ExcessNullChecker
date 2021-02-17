package excessNullChecker

import excessNullChecker.visitors.FieldInfo

class Context(
    val fields: Map<String, FieldInfo>,
    val methodsCfg: Map<String, Map<Int, CfgNode>>,
    val bytes: ByteArray,
    val logger: Logger
) {
    val processedMethods: MutableMap<String, DataEntry> = mutableMapOf()
    val processedFinalFields: MutableMap<String, DataEntryType> = mutableMapOf()

    init {
        for (item in fields)
            if (item.value.isFinal)
                processedFinalFields[item.key] = DataEntryType.Uninitialized
    }
}