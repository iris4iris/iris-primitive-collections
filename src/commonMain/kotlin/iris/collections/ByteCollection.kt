package iris.collections

interface ByteCollection : PrimitiveCollection {
    operator fun contains(element: Byte): Boolean
    fun containsAll(elements: ByteCollection): Boolean
    fun containsAll(elements: Collection<Byte>): Boolean
    fun containsAny(elements: ByteCollection): Boolean
    fun containsAny(elements: Collection<Byte>): Boolean
    fun iterator(): PrimitiveByteIterator
    fun toArray(): ByteArray
    fun toArray(destination: ByteArray): ByteArray
    fun add(element: Byte): Boolean
    operator fun plusAssign(element: Byte) { add(element) }
    fun addAll(elements: ByteCollection): Boolean
    fun addAll(elements: ByteArray): Boolean
    fun addAll(elements: Collection<Byte>): Boolean
    fun removeElement(element: Byte): Boolean
    fun removeAll(elements: ByteCollection): Boolean
    fun removeAll(elements: Collection<Byte>): Boolean
    fun retainAll(elements: ByteCollection): Boolean
    fun retainAll(elements: Collection<Byte>): Boolean
    fun removeIf(predicate: (Byte) -> Boolean): Boolean
    fun clone(): ByteCollection
    fun asMutableCollection(): MutableCollection<Byte>
    fun joinToString(
        separator: CharSequence = ", ",
        prefix: CharSequence = "",
        postfix: CharSequence = "",
        limit: Int = -1,
        truncated: CharSequence = "...",
        transform: ((Byte) -> CharSequence)? = null,
    ): String
}

interface ByteList : ByteCollection {
    operator fun get(index: Int): Byte
    fun indexOf(element: Byte): Int
    fun lastIndexOf(element: Byte): Int
    fun indexOfRange(element: Byte, start: Int, end: Int): Int
    fun lastIndexOfRange(element: Byte, start: Int, end: Int): Int
    fun equalsRange(other: ByteList, from: Int, to: Int): Boolean
    override fun clone(): ByteList
    fun subList(fromIndex: Int, toIndex: Int): ByteList
}

interface ByteMutableList : ByteList {
    operator fun set(index: Int, element: Byte): Byte
    fun add(index: Int, element: Byte)
    fun addAll(index: Int, elements: ByteCollection): Boolean
    fun addAll(index: Int, elements: ByteArray): Boolean
    fun removeAt(index: Int): Byte
    override fun clone(): ByteMutableList
    override fun subList(fromIndex: Int, toIndex: Int): ByteMutableList
    fun asMutableList(): MutableList<Byte>
    fun sort()
    fun sortDescending()
    fun binarySearch(element: Byte): Int
}

interface PrimitiveByteIterator {
    fun hasNext(): Boolean
    fun next(): Byte
    fun remove()
}

inline fun <T> Iterable<T>.mapBytes(transform: (T) -> Byte): ByteArrayList {
    val out = ByteArrayList(guessSize())
    for (item in this) out += transform(item)
    return out
}

inline fun <T> Iterable<T>.mapIndexedBytes(transform: (index: Int, T) -> Byte): ByteArrayList {
    val out = ByteArrayList(guessSize())
    var i = 0
    for (item in this) out += transform(i++, item)
    return out
}

inline fun <T> Array<T>.mapBytes(transform: (T) -> Byte): ByteArrayList {
    val out = ByteArrayList(size)
    for (item in this) out += transform(item)
    return out
}

fun Iterable<Byte>.toByteArrayList(): ByteArrayList {
    val out = ByteArrayList(guessSize())
    for (item in this) out += item
    return out
}

inline fun Iterable<Byte>.filterBytes(predicate: (Byte) -> Boolean): ByteArrayList {
    val out = ByteArrayList(guessSize())
    for (item in this) if (predicate(item)) out += item
    return out
}
