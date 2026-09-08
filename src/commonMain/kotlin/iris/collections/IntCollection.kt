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

    fun sum(): Int {
        var s = 0
        val it = iterator()
        while (it.hasNext()) s += it.next()
        return s
    }

    fun average(): Double {
        if (isEmpty()) return Double.NaN
        return sum().toDouble() / size
    }

    fun first(): Int {
        val it = iterator()
        if (!it.hasNext()) throw NoSuchElementException("Empty IntCollection")
        return it.next()
    }

    fun last(): Int {
        val it = iterator()
        if (!it.hasNext()) throw NoSuchElementException("Empty IntCollection")
        var v = it.next()
        while (it.hasNext()) v = it.next()
        return v
    }

    fun firstOrElse(default: Int): Int {
        val it = iterator()
        return if (it.hasNext()) it.next() else default
    }

    fun lastOrElse(default: Int): Int {
        val it = iterator()
        if (!it.hasNext()) return default
        var v = it.next()
        while (it.hasNext()) v = it.next()
        return v
    }

    fun minOrElse(default: Int): Int {
        val it = iterator()
        if (!it.hasNext()) return default
        var m = it.next()
        while (it.hasNext()) {
            val v = it.next()
            if (v < m) m = v
        }
        return m
    }

    fun maxOrElse(default: Int): Int {
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

inline fun <T> Iterable<T>.mapInts(transform: (T) -> Int): IntArrayList {
    val out = IntArrayList(guessSize())
    for (item in this) out += transform(item)
    return out
}

inline fun <T> Iterable<T>.mapIndexedInts(transform: (index: Int, T) -> Int): IntArrayList {
    val out = IntArrayList(guessSize())
    var i = 0
    for (item in this) out += transform(i++, item)
    return out
}

inline fun <T> Array<T>.mapInts(transform: (T) -> Int): IntArrayList {
    val out = IntArrayList(size)
    for (item in this) out += transform(item)
    return out
}

inline fun <T> Iterable<T>.associateIntsBy(keySelector: (T) -> Int): IntMap<T> {
    val out = IntMap<T>(guessSize())
    for (item in this) out[keySelector(item)] = item
    return out
}

inline fun <T, V> Iterable<T>.associateInts(
    keySelector: (T) -> Int,
    valueTransform: (T) -> V,
): IntMap<V> {
    val out = IntMap<V>(guessSize())
    for (item in this) out[keySelector(item)] = valueTransform(item)
    return out
}

inline fun <T> Array<T>.associateIntsBy(keySelector: (T) -> Int): IntMap<T> {
    val out = IntMap<T>(size)
    for (item in this) out[keySelector(item)] = item
    return out
}

inline fun <T, V> Array<T>.associateInts(
    keySelector: (T) -> Int,
    valueTransform: (T) -> V,
): IntMap<V> {
    val out = IntMap<V>(size)
    for (item in this) out[keySelector(item)] = valueTransform(item)
    return out
}

inline fun <T> Iterable<T>.toIntSet(transform: (T) -> Int): IntSet {
    val out = IntSet(guessSize())
    for (item in this) out += transform(item)
    return out
}

inline fun <T> Array<T>.toIntSet(transform: (T) -> Int): IntSet {
    val out = IntSet(size)
    for (item in this) out += transform(item)
    return out
}

fun Iterable<Int>.toIntArrayList(): IntArrayList {
    val out = IntArrayList(guessSize())
    for (item in this) out += item
    return out
}

fun Iterable<Int>.toIntSet(): IntSet {
    val out = IntSet(guessSize())
    for (item in this) out += item
    return out
}

fun IntArray.toIntSet(): IntSet = IntSet(this)

inline fun <T> Iterable<T>.groupIntsBy(keySelector: (T) -> Int): IntMap<ArrayList<T>> {
    val out = IntMap<ArrayList<T>>(guessSize())
    for (item in this) {
        val key = keySelector(item)
        var bucket = out[key]
        if (bucket == null) {
            bucket = ArrayList()
            out[key] = bucket
        }
        bucket.add(item)
    }
    return out
}

inline fun Iterable<Int>.filterInts(predicate: (Int) -> Boolean): IntArrayList {
    val out = IntArrayList(guessSize())
    for (item in this) if (predicate(item)) out += item
    return out
}
