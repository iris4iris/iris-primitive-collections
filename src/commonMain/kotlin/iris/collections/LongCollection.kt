package iris.collections

interface LongCollection : PrimitiveCollection {
    operator fun contains(element: Long): Boolean
    fun containsAll(elements: LongCollection): Boolean
    fun containsAll(elements: Collection<Long>): Boolean
    fun containsAny(elements: LongCollection): Boolean
    fun containsAny(elements: Collection<Long>): Boolean
    fun iterator(): PrimitiveLongIterator
    fun toArray(): LongArray
    fun toArray(destination: LongArray): LongArray
    fun add(element: Long): Boolean
    operator fun plusAssign(element: Long) { add(element) }
    fun addAll(elements: LongCollection): Boolean
    fun addAll(elements: LongArray): Boolean
    fun addAll(elements: Collection<Long>): Boolean
    fun removeElement(element: Long): Boolean
    fun removeAll(elements: LongCollection): Boolean
    fun removeAll(elements: Collection<Long>): Boolean
    fun retainAll(elements: LongCollection): Boolean
    fun retainAll(elements: Collection<Long>): Boolean
    fun removeIf(predicate: (Long) -> Boolean): Boolean
    fun clone(): LongCollection
    fun asMutableCollection(): MutableCollection<Long>
    fun joinToString(
        separator: CharSequence = ", ",
        prefix: CharSequence = "",
        postfix: CharSequence = "",
        limit: Int = -1,
        truncated: CharSequence = "...",
        transform: ((Long) -> CharSequence)? = null,
    ): String
}

interface LongList : LongCollection {
    operator fun get(index: Int): Long
    fun indexOf(element: Long): Int
    fun lastIndexOf(element: Long): Int
    fun indexOfRange(element: Long, start: Int, end: Int): Int
    fun lastIndexOfRange(element: Long, start: Int, end: Int): Int
    fun equalsRange(other: LongList, from: Int, to: Int): Boolean
    override fun clone(): LongList
    fun subList(fromIndex: Int, toIndex: Int): LongList
}

interface LongMutableList : LongList {
    operator fun set(index: Int, element: Long): Long
    fun add(index: Int, element: Long)
    fun addAll(index: Int, elements: LongCollection): Boolean
    fun addAll(index: Int, elements: LongArray): Boolean
    fun removeAt(index: Int): Long
    override fun clone(): LongMutableList
    override fun subList(fromIndex: Int, toIndex: Int): LongMutableList
    fun asMutableList(): MutableList<Long>
    fun sort()
    fun sortDescending()
    fun binarySearch(element: Long): Int
}

interface PrimitiveLongIterator {
    fun hasNext(): Boolean
    fun next(): Long
    fun remove()
}
