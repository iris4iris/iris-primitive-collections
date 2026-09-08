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

    fun sum(): Long {
        var s = 0L
        val it = iterator()
        while (it.hasNext()) s += it.next()
        return s
    }

    fun average(): Double {
        if (isEmpty()) return Double.NaN
        return sum().toDouble() / size
    }

    fun first(): Long {
        val it = iterator()
        if (!it.hasNext()) throw NoSuchElementException("Empty LongCollection")
        return it.next()
    }

    fun last(): Long {
        val it = iterator()
        if (!it.hasNext()) throw NoSuchElementException("Empty LongCollection")
        var v = it.next()
        while (it.hasNext()) v = it.next()
        return v
    }

    fun firstOrElse(default: Long): Long {
        val it = iterator()
        return if (it.hasNext()) it.next() else default
    }

    fun lastOrElse(default: Long): Long {
        val it = iterator()
        if (!it.hasNext()) return default
        var v = it.next()
        while (it.hasNext()) v = it.next()
        return v
    }

    fun minOrElse(default: Long): Long {
        val it = iterator()
        if (!it.hasNext()) return default
        var m = it.next()
        while (it.hasNext()) {
            val v = it.next()
            if (v < m) m = v
        }
        return m
    }

    fun maxOrElse(default: Long): Long {
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


inline fun <T> Iterable<T>.mapLongs(transform: (T) -> Long): LongArrayList {
    val out = LongArrayList(guessSize())
    for (item in this) out += transform(item)
    return out
}

inline fun <T> Iterable<T>.mapIndexedLongs(transform: (index: Int, T) -> Long): LongArrayList {
    val out = LongArrayList(guessSize())
    var i = 0
    for (item in this) out += transform(i++, item)
    return out
}

inline fun <T> Array<T>.mapLongs(transform: (T) -> Long): LongArrayList {
    val out = LongArrayList(size)
    for (item in this) out += transform(item)
    return out
}

inline fun <T> Iterable<T>.associateLongsBy(keySelector: (T) -> Long): LongMap<T> {
    val out = LongMap<T>(guessSize())
    for (item in this) out[keySelector(item)] = item
    return out
}

inline fun <T, V> Iterable<T>.associateLongs(
    keySelector: (T) -> Long,
    valueTransform: (T) -> V,
): LongMap<V> {
    val out = LongMap<V>(guessSize())
    for (item in this) out[keySelector(item)] = valueTransform(item)
    return out
}

inline fun <T> Array<T>.associateLongsBy(keySelector: (T) -> Long): LongMap<T> {
    val out = LongMap<T>(size)
    for (item in this) out[keySelector(item)] = item
    return out
}

inline fun <T, V> Array<T>.associateLongs(
    keySelector: (T) -> Long,
    valueTransform: (T) -> V,
): LongMap<V> {
    val out = LongMap<V>(size)
    for (item in this) out[keySelector(item)] = valueTransform(item)
    return out
}

inline fun <T> Iterable<T>.toLongSet(transform: (T) -> Long): LongSet {
    val out = LongSet(guessSize())
    for (item in this) out += transform(item)
    return out
}

inline fun <T> Array<T>.toLongSet(transform: (T) -> Long): LongSet {
    val out = LongSet(size)
    for (item in this) out += transform(item)
    return out
}

fun Iterable<Long>.toLongArrayList(): LongArrayList {
    val out = LongArrayList(guessSize())
    for (item in this) out += item
    return out
}

fun Iterable<Long>.toLongSet(): LongSet {
    val out = LongSet(guessSize())
    for (item in this) out += item
    return out
}

fun LongArray.toLongSet(): LongSet = LongSet(this)

inline fun <T> Iterable<T>.groupLongsBy(keySelector: (T) -> Long): LongMap<ArrayList<T>> {
    val out = LongMap<ArrayList<T>>(guessSize())
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

inline fun Iterable<Long>.filterLongs(predicate: (Long) -> Boolean): LongArrayList {
    val out = LongArrayList(guessSize())
    for (item in this) if (predicate(item)) out += item
    return out
}
