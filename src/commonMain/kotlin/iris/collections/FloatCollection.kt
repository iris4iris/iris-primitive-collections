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
    fun sum(): Float {
        var s = 0f
        val it = iterator()
        while (it.hasNext()) s += it.next()
        return s
    }

    fun average(): Double {
        if (isEmpty()) return Double.NaN
        return sum().toDouble() / size
    }

    fun first(): Float {
        val it = iterator()
        if (!it.hasNext()) throw NoSuchElementException("Empty FloatCollection")
        return it.next()
    }

    fun last(): Float {
        val it = iterator()
        if (!it.hasNext()) throw NoSuchElementException("Empty FloatCollection")
        var v = it.next()
        while (it.hasNext()) v = it.next()
        return v
    }

    fun firstOrElse(default: Float): Float {
        val it = iterator()
        return if (it.hasNext()) it.next() else default
    }

    fun lastOrElse(default: Float): Float {
        val it = iterator()
        if (!it.hasNext()) return default
        var v = it.next()
        while (it.hasNext()) v = it.next()
        return v
    }

    fun minOrElse(default: Float): Float {
        val it = iterator()
        if (!it.hasNext()) return default
        var m = it.next()
        while (it.hasNext()) {
            val v = it.next()
            if (v < m) m = v
        }
        return m
    }

    fun maxOrElse(default: Float): Float {
        val it = iterator()
        if (!it.hasNext()) return default
        var m = it.next()
        while (it.hasNext()) {
            val v = it.next()
            if (v > m) m = v
        }
        return m
    }
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


inline fun <T> Iterable<T>.mapFloats(transform: (T) -> Float): FloatArrayList {
    val out = FloatArrayList(guessSize())
    for (item in this) out += transform(item)
    return out
}

inline fun <T> Iterable<T>.mapIndexedFloats(transform: (index: Int, T) -> Float): FloatArrayList {
    val out = FloatArrayList(guessSize())
    var i = 0
    for (item in this) out += transform(i++, item)
    return out
}

inline fun <T> Array<T>.mapFloats(transform: (T) -> Float): FloatArrayList {
    val out = FloatArrayList(size)
    for (item in this) out += transform(item)
    return out
}

fun Iterable<Float>.toFloatArrayList(): FloatArrayList {
    val out = FloatArrayList(guessSize())
    for (item in this) out += item
    return out
}

inline fun Iterable<Float>.filterFloats(predicate: (Float) -> Boolean): FloatArrayList {
    val out = FloatArrayList(guessSize())
    for (item in this) if (predicate(item)) out += item
    return out
}
