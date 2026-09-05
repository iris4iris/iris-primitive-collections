package iris.collections

interface CharCollection : PrimitiveCollection {
    operator fun contains(element: Char): Boolean
    fun containsAll(elements: CharCollection): Boolean
    fun containsAll(elements: Collection<Char>): Boolean
    fun containsAny(elements: CharCollection): Boolean
    fun containsAny(elements: Collection<Char>): Boolean
    fun iterator(): PrimitiveCharIterator
    fun toArray(): CharArray
    fun toArray(destination: CharArray): CharArray
    fun add(element: Char): Boolean
    operator fun plusAssign(element: Char) { add(element) }
    fun addAll(elements: CharCollection): Boolean
    fun addAll(elements: CharArray): Boolean
    fun addAll(elements: Collection<Char>): Boolean
    fun removeElement(element: Char): Boolean
    fun removeAll(elements: CharCollection): Boolean
    fun removeAll(elements: Collection<Char>): Boolean
    fun retainAll(elements: CharCollection): Boolean
    fun retainAll(elements: Collection<Char>): Boolean
    fun removeIf(predicate: (Char) -> Boolean): Boolean
    fun clone(): CharCollection
    fun asMutableCollection(): MutableCollection<Char>
    fun joinToString(
        separator: CharSequence = ", ",
        prefix: CharSequence = "",
        postfix: CharSequence = "",
        limit: Int = -1,
        truncated: CharSequence = "...",
        transform: ((Char) -> CharSequence)? = null,
    ): String
}

interface CharList : CharCollection {
    operator fun get(index: Int): Char
    fun indexOf(element: Char): Int
    fun lastIndexOf(element: Char): Int
    fun indexOfRange(element: Char, start: Int, end: Int): Int
    fun lastIndexOfRange(element: Char, start: Int, end: Int): Int
    fun equalsRange(other: CharList, from: Int, to: Int): Boolean
    override fun clone(): CharList
    fun subList(fromIndex: Int, toIndex: Int): CharList
}

interface CharMutableList : CharList {
    operator fun set(index: Int, element: Char): Char
    fun add(index: Int, element: Char)
    fun addAll(index: Int, elements: CharCollection): Boolean
    fun addAll(index: Int, elements: CharArray): Boolean
    fun removeAt(index: Int): Char
    override fun clone(): CharMutableList
    override fun subList(fromIndex: Int, toIndex: Int): CharMutableList
    fun asMutableList(): MutableList<Char>
    fun sort()
    fun sortDescending()
    fun binarySearch(element: Char): Int
}

interface PrimitiveCharIterator {
    fun hasNext(): Boolean
    fun next(): Char
    fun remove()
}
