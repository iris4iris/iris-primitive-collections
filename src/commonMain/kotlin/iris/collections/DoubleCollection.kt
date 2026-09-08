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
    fun sum(): Double {
        var s = 0.0
        val it = iterator()
        while (it.hasNext()) s += it.next()
        return s
    }

    fun average(): Double {
        if (isEmpty()) return Double.NaN
        return sum() / size
    }

    fun first(): Double {
        val it = iterator()
        if (!it.hasNext()) throw NoSuchElementException("Empty DoubleCollection")
        return it.next()
    }

    fun last(): Double {
        val it = iterator()
        if (!it.hasNext()) throw NoSuchElementException("Empty DoubleCollection")
        var v = it.next()
        while (it.hasNext()) v = it.next()
        return v
    }

    fun firstOrElse(default: Double): Double {
        val it = iterator()
        return if (it.hasNext()) it.next() else default
    }

    fun lastOrElse(default: Double): Double {
        val it = iterator()
        if (!it.hasNext()) return default
        var v = it.next()
        while (it.hasNext()) v = it.next()
        return v
    }

    fun minOrElse(default: Double): Double {
        val it = iterator()
        if (!it.hasNext()) return default
        var m = it.next()
        while (it.hasNext()) {
            val v = it.next()
            if (v < m) m = v
        }
        return m
    }

    fun maxOrElse(default: Double): Double {
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

inline fun <T> Iterable<T>.mapDoubles(transform: (T) -> Double): DoubleArrayList {
    val out = DoubleArrayList(guessSize())
    for (item in this) out += transform(item)
    return out
}

inline fun <T> Iterable<T>.mapIndexedDoubles(transform: (index: Int, T) -> Double): DoubleArrayList {
    val out = DoubleArrayList(guessSize())
    var i = 0
    for (item in this) out += transform(i++, item)
    return out
}

inline fun <T> Array<T>.mapDoubles(transform: (T) -> Double): DoubleArrayList {
    val out = DoubleArrayList(size)
    for (item in this) out += transform(item)
    return out
}

fun Iterable<Double>.toDoubleArrayList(): DoubleArrayList {
    val out = DoubleArrayList(guessSize())
    for (item in this) out += item
    return out
}

inline fun Iterable<Double>.filterDoubles(predicate: (Double) -> Boolean): DoubleArrayList {
    val out = DoubleArrayList(guessSize())
    for (item in this) if (predicate(item)) out += item
    return out
}
