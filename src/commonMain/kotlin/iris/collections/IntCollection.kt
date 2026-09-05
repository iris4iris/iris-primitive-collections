package iris.collections

interface IntCollection : PrimitiveCollection {
    operator fun contains(element: Int): Boolean
    fun containsAll(elements: IntCollection): Boolean
    fun containsAll(elements: Collection<Int>): Boolean
    fun containsAny(elements: IntCollection): Boolean
    fun containsAny(elements: Collection<Int>): Boolean
    fun iterator(): PrimitiveIntIterator
    fun toArray(): IntArray
    fun toArray(destination: IntArray): IntArray
    fun add(element: Int): Boolean
    operator fun plusAssign(element: Int) { add(element) }
    fun addAll(elements: IntCollection): Boolean
    fun addAll(elements: IntArray): Boolean
    fun addAll(elements: Collection<Int>): Boolean
    fun removeElement(element: Int): Boolean
    fun removeAll(elements: IntCollection): Boolean
    fun removeAll(elements: Collection<Int>): Boolean
    fun retainAll(elements: IntCollection): Boolean
    fun retainAll(elements: Collection<Int>): Boolean
    fun removeIf(predicate: (Int) -> Boolean): Boolean
    fun clone(): IntCollection
    fun asMutableCollection(): MutableCollection<Int>
    fun joinToString(
        separator: CharSequence = ", ",
        prefix: CharSequence = "",
        postfix: CharSequence = "",
        limit: Int = -1,
        truncated: CharSequence = "...",
        transform: ((Int) -> CharSequence)? = null,
    ): String
}

interface IntList : IntCollection {
    operator fun get(index: Int): Int
    fun indexOf(element: Int): Int
    fun lastIndexOf(element: Int): Int
    fun indexOfRange(element: Int, start: Int, end: Int): Int
    fun lastIndexOfRange(element: Int, start: Int, end: Int): Int
    fun equalsRange(other: IntList, from: Int, to: Int): Boolean
    override fun clone(): IntList
    fun subList(fromIndex: Int, toIndex: Int): IntList
}

interface IntMutableList : IntList {
    operator fun set(index: Int, element: Int): Int
    fun add(index: Int, element: Int)
    fun addAll(index: Int, elements: IntCollection): Boolean
    fun addAll(index: Int, elements: IntArray): Boolean
    fun removeAt(index: Int): Int
    override fun clone(): IntMutableList
    override fun subList(fromIndex: Int, toIndex: Int): IntMutableList
    fun asMutableList(): MutableList<Int>
    fun sort()
    fun sortDescending()
    fun binarySearch(element: Int): Int
}

interface PrimitiveIntIterator {
    fun hasNext(): Boolean
    fun next(): Int
    fun remove()
}
