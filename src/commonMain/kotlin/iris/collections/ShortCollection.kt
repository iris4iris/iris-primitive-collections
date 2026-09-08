package iris.collections

interface ShortCollection : PrimitiveCollection {
    operator fun contains(element: Short): Boolean
    fun containsAll(elements: ShortCollection): Boolean
    fun containsAll(elements: Collection<Short>): Boolean
    fun containsAny(elements: ShortCollection): Boolean
    fun containsAny(elements: Collection<Short>): Boolean
    fun iterator(): PrimitiveShortIterator
    fun toArray(): ShortArray
    fun toArray(destination: ShortArray): ShortArray
    fun add(element: Short): Boolean
    operator fun plusAssign(element: Short) { add(element) }
    fun addAll(elements: ShortCollection): Boolean
    fun addAll(elements: ShortArray): Boolean
    fun addAll(elements: Collection<Short>): Boolean
    fun removeElement(element: Short): Boolean
    fun removeAll(elements: ShortCollection): Boolean
    fun removeAll(elements: Collection<Short>): Boolean
    fun retainAll(elements: ShortCollection): Boolean
    fun retainAll(elements: Collection<Short>): Boolean
    fun removeIf(predicate: (Short) -> Boolean): Boolean
    fun clone(): ShortCollection
    fun asMutableCollection(): MutableCollection<Short>
    fun joinToString(
        separator: CharSequence = ", ",
        prefix: CharSequence = "",
        postfix: CharSequence = "",
        limit: Int = -1,
        truncated: CharSequence = "...",
        transform: ((Short) -> CharSequence)? = null,
    ): String
}

interface ShortList : ShortCollection {
    operator fun get(index: Int): Short
    fun indexOf(element: Short): Int
    fun lastIndexOf(element: Short): Int
    fun indexOfRange(element: Short, start: Int, end: Int): Int
    fun lastIndexOfRange(element: Short, start: Int, end: Int): Int
    fun equalsRange(other: ShortList, from: Int, to: Int): Boolean
    override fun clone(): ShortList
    fun subList(fromIndex: Int, toIndex: Int): ShortList
}

interface ShortMutableList : ShortList {
    operator fun set(index: Int, element: Short): Short
    fun add(index: Int, element: Short)
    fun addAll(index: Int, elements: ShortCollection): Boolean
    fun addAll(index: Int, elements: ShortArray): Boolean
    fun removeAt(index: Int): Short
    override fun clone(): ShortMutableList
    override fun subList(fromIndex: Int, toIndex: Int): ShortMutableList
    fun asMutableList(): MutableList<Short>
    fun sort()
    fun sortDescending()
    fun binarySearch(element: Short): Int
}

interface PrimitiveShortIterator {
    fun hasNext(): Boolean
    fun next(): Short
    fun remove()
}

inline fun <T> Iterable<T>.mapShorts(transform: (T) -> Short): ShortArrayList {
    val out = ShortArrayList(guessSize())
    for (item in this) out += transform(item)
    return out
}

inline fun <T> Iterable<T>.mapIndexedShorts(transform: (index: Int, T) -> Short): ShortArrayList {
    val out = ShortArrayList(guessSize())
    var i = 0
    for (item in this) out += transform(i++, item)
    return out
}

inline fun <T> Array<T>.mapShorts(transform: (T) -> Short): ShortArrayList {
    val out = ShortArrayList(size)
    for (item in this) out += transform(item)
    return out
}

fun Iterable<Short>.toShortArrayList(): ShortArrayList {
    val out = ShortArrayList(guessSize())
    for (item in this) out += item
    return out
}

inline fun Iterable<Short>.filterShorts(predicate: (Short) -> Boolean): ShortArrayList {
    val out = ShortArrayList(guessSize())
    for (item in this) if (predicate(item)) out += item
    return out
}
