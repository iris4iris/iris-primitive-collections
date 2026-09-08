package iris.collections

@PublishedApi internal const val HASH_EMPTY: Byte = 0
@PublishedApi internal const val HASH_FULL: Byte = 1
@PublishedApi internal const val HASH_DELETED: Byte = 2

internal const val DEFAULT_HASH_CAPACITY = 8
private const val MAX_LOAD_NUM = 2
private const val MAX_LOAD_DEN = 3

internal fun mixInt(key: Int): Int {
    var h = key * -0x3361d2af
    h = h xor (h ushr 16)
    return h
}

internal fun mixLong(key: Long): Int {
    var h = (key xor (key ushr 32)).toInt()
    return mixInt(h)
}

internal fun hashCapacityFor(expectedSize: Int): Int {
    require(expectedSize >= 0) { "Illegal size: $expectedSize" }
    if (expectedSize == 0) return DEFAULT_HASH_CAPACITY
    val need = expectedSize.toLong() * MAX_LOAD_DEN / MAX_LOAD_NUM + 1
    var n = DEFAULT_HASH_CAPACITY.toLong()
    while (n < need) {
        n = n shl 1
        if (n > MAX_ARRAY_LENGTH) throw Error("Required table length too large")
    }
    return n.toInt()
}

internal fun nextPowerOfTwo(value: Int): Int {
    var n = DEFAULT_HASH_CAPACITY
    while (n < value) {
        n = n shl 1
        if (n <= 0 || n > MAX_ARRAY_LENGTH) throw Error("Required table length too large")
    }
    return n
}

internal fun hashNeedsGrow(size: Int, deleted: Int, capacity: Int): Boolean {
    return (size + deleted).toLong() * MAX_LOAD_DEN >= capacity.toLong() * MAX_LOAD_NUM
}
