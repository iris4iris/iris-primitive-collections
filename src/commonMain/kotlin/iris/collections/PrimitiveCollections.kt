package iris.collections

/**
 * Shared helpers for primitive lists. JVM/Native/JS safe.
 */
internal const val DEFAULT_CAPACITY = 10
internal const val MAX_ARRAY_LENGTH = Int.MAX_VALUE - 8

internal fun newLength(oldLength: Int, minGrowth: Int, prefGrowth: Int): Int {
    val newLength = maxOf(minGrowth, prefGrowth) + oldLength
    return if (newLength - MAX_ARRAY_LENGTH <= 0) {
        newLength
    } else {
        val minLength = oldLength + minGrowth
        if (minLength < 0) throw Error("Required array length too large")
        if (minLength <= MAX_ARRAY_LENGTH) MAX_ARRAY_LENGTH else Int.MAX_VALUE
    }
}

internal fun checkIndex(index: Int, size: Int) {
    if (index < 0 || index >= size) {
        throw IndexOutOfBoundsException("Index: $index, Size: $size")
    }
}

internal fun checkPositionIndex(index: Int, size: Int) {
    if (index < 0 || index > size) {
        throw IndexOutOfBoundsException("Index: $index, Size: $size")
    }
}

internal fun checkRange(fromIndex: Int, toIndex: Int, size: Int) {
    if (fromIndex < 0 || toIndex > size || fromIndex > toIndex) {
        throw IndexOutOfBoundsException("From: $fromIndex, To: $toIndex, Size: $size")
    }
}

/**
 * Common binary search. [compare] receives an index and must return
 * `elementData[index].compareTo(key)` — same contract as `Arrays.binarySearch`.
 */
internal inline fun primitiveBinarySearch(size: Int, compare: (index: Int) -> Int): Int {
    var low = 0
    var high = size - 1
    while (low <= high) {
        val mid = (low + high) ushr 1
        val cmp = compare(mid)
        when {
            cmp < 0 -> low = mid + 1
            cmp > 0 -> high = mid - 1
            else -> return mid
        }
    }
    return -(low + 1)
}

interface PrimitiveCollection {
    val size: Int
    val lastIndex: Int get() = size - 1
    val indices: IntRange get() = 0 until size
    fun isEmpty(): Boolean = size == 0
    fun isNotEmpty(): Boolean = size != 0
    fun clear()
    fun ensureCapacity(minCapacity: Int)
    fun trimToSize()
}

@PublishedApi
internal fun Iterable<*>.guessSize(): Int = if (this is Collection<*>) size else 0
