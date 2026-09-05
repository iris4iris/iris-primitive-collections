package iris.collections

interface DoubleCollection : PrimitiveCollection {
    operator fun contains(element: Double): Boolean
    fun containsAll(elements: DoubleCollection): Boolean
    fun containsAll(elements: Collection<Double>): Boolean
    fun containsAny(elements: DoubleCollection): Boolean
    fun containsAny(elements: Collection<Double>): Boolean
    fun iterator(): PrimitiveDoubleIterator
    fun toArray(): DoubleArray
    fun toArray(destination: DoubleArray): DoubleArray
    fun add(element: Double): Boolean
    operator fun plusAssign(element: Double) { add(element) }
    fun addAll(elements: DoubleCollection): Boolean
    fun addAll(elements: DoubleArray): Boolean
    fun addAll(elements: Collection<Double>): Boolean
    fun removeElement(element: Double): Boolean
    fun removeAll(elements: DoubleCollection): Boolean
    fun removeAll(elements: Collection<Double>): Boolean
    fun retainAll(elements: DoubleCollection): Boolean
    fun retainAll(elements: Collection<Double>): Boolean
    fun removeIf(predicate: (Double) -> Boolean): Boolean
    fun clone(): DoubleCollection
    fun asMutableCollection(): MutableCollection<Double>
    fun joinToString(
        separator: CharSequence = ", ",
        prefix: CharSequence = "",
        postfix: CharSequence = "",
        limit: Int = -1,
        truncated: CharSequence = "...",
        transform: ((Double) -> CharSequence)? = null,
    ): String
}

interface DoubleList : DoubleCollection {
    operator fun get(index: Int): Double
    fun indexOf(element: Double): Int
    fun lastIndexOf(element: Double): Int
    fun indexOfRange(element: Double, start: Int, end: Int): Int
    fun lastIndexOfRange(element: Double, start: Int, end: Int): Int
    fun equalsRange(other: DoubleList, from: Int, to: Int): Boolean
    override fun clone(): DoubleList
    fun subList(fromIndex: Int, toIndex: Int): DoubleList
}

interface DoubleMutableList : DoubleList {
    operator fun set(index: Int, element: Double): Double
    fun add(index: Int, element: Double)
    fun addAll(index: Int, elements: DoubleCollection): Boolean
    fun addAll(index: Int, elements: DoubleArray): Boolean
    fun removeAt(index: Int): Double
    override fun clone(): DoubleMutableList
    override fun subList(fromIndex: Int, toIndex: Int): DoubleMutableList
    fun asMutableList(): MutableList<Double>
    fun sort()
    fun sortDescending()
    fun binarySearch(element: Double): Int
}

interface PrimitiveDoubleIterator {
    fun hasNext(): Boolean
    fun next(): Double
    fun remove()
}
