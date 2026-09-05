package iris.collections

interface FloatCollection : PrimitiveCollection {
    operator fun contains(element: Float): Boolean
    fun containsAll(elements: FloatCollection): Boolean
    fun containsAll(elements: Collection<Float>): Boolean
    fun containsAny(elements: FloatCollection): Boolean
    fun containsAny(elements: Collection<Float>): Boolean
    fun iterator(): PrimitiveFloatIterator
    fun toArray(): FloatArray
    fun toArray(destination: FloatArray): FloatArray
    fun add(element: Float): Boolean
    operator fun plusAssign(element: Float) { add(element) }
    fun addAll(elements: FloatCollection): Boolean
    fun addAll(elements: FloatArray): Boolean
    fun addAll(elements: Collection<Float>): Boolean
    fun removeElement(element: Float): Boolean
    fun removeAll(elements: FloatCollection): Boolean
    fun removeAll(elements: Collection<Float>): Boolean
    fun retainAll(elements: FloatCollection): Boolean
    fun retainAll(elements: Collection<Float>): Boolean
    fun removeIf(predicate: (Float) -> Boolean): Boolean
    fun clone(): FloatCollection
    fun asMutableCollection(): MutableCollection<Float>
    fun joinToString(
        separator: CharSequence = ", ",
        prefix: CharSequence = "",
        postfix: CharSequence = "",
        limit: Int = -1,
        truncated: CharSequence = "...",
        transform: ((Float) -> CharSequence)? = null,
    ): String
}

interface FloatList : FloatCollection {
    operator fun get(index: Int): Float
    fun indexOf(element: Float): Int
    fun lastIndexOf(element: Float): Int
    fun indexOfRange(element: Float, start: Int, end: Int): Int
    fun lastIndexOfRange(element: Float, start: Int, end: Int): Int
    fun equalsRange(other: FloatList, from: Int, to: Int): Boolean
    override fun clone(): FloatList
    fun subList(fromIndex: Int, toIndex: Int): FloatList
}

interface FloatMutableList : FloatList {
    operator fun set(index: Int, element: Float): Float
    fun add(index: Int, element: Float)
    fun addAll(index: Int, elements: FloatCollection): Boolean
    fun addAll(index: Int, elements: FloatArray): Boolean
    fun removeAt(index: Int): Float
    override fun clone(): FloatMutableList
    override fun subList(fromIndex: Int, toIndex: Int): FloatMutableList
    fun asMutableList(): MutableList<Float>
    fun sort()
    fun sortDescending()
    fun binarySearch(element: Float): Int
}

interface PrimitiveFloatIterator {
    fun hasNext(): Boolean
    fun next(): Float
    fun remove()
}
