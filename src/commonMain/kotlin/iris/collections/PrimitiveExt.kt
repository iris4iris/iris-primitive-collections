package iris.collections

@PublishedApi
internal fun Iterable<*>.guessSize(): Int = if (this is Collection<*>) size else 0

inline fun <T> Iterable<T>.mapInts(transform: (T) -> Int): IntArrayList {
    val out = IntArrayList(guessSize())
    for (item in this) out += transform(item)
    return out
}

inline fun <T> Iterable<T>.mapLongs(transform: (T) -> Long): LongArrayList {
    val out = LongArrayList(guessSize())
    for (item in this) out += transform(item)
    return out
}

inline fun <T> Iterable<T>.mapIndexedInts(transform: (index: Int, T) -> Int): IntArrayList {
    val out = IntArrayList(guessSize())
    var i = 0
    for (item in this) out += transform(i++, item)
    return out
}

inline fun <T> Iterable<T>.mapIndexedLongs(transform: (index: Int, T) -> Long): LongArrayList {
    val out = LongArrayList(guessSize())
    var i = 0
    for (item in this) out += transform(i++, item)
    return out
}

inline fun <T> Array<T>.mapInts(transform: (T) -> Int): IntArrayList {
    val out = IntArrayList(size)
    for (item in this) out += transform(item)
    return out
}

inline fun <T> Array<T>.mapLongs(transform: (T) -> Long): LongArrayList {
    val out = LongArrayList(size)
    for (item in this) out += transform(item)
    return out
}

inline fun <T> Iterable<T>.associateIntsBy(keySelector: (T) -> Int): IntMap<T> {
    val out = IntMap<T>(guessSize())
    for (item in this) out[keySelector(item)] = item
    return out
}

inline fun <T> Iterable<T>.associateLongsBy(keySelector: (T) -> Long): LongMap<T> {
    val out = LongMap<T>(guessSize())
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

inline fun <T, V> Iterable<T>.associateLongs(
    keySelector: (T) -> Long,
    valueTransform: (T) -> V,
): LongMap<V> {
    val out = LongMap<V>(guessSize())
    for (item in this) out[keySelector(item)] = valueTransform(item)
    return out
}

inline fun <T> Array<T>.associateIntsBy(keySelector: (T) -> Int): IntMap<T> {
    val out = IntMap<T>(size)
    for (item in this) out[keySelector(item)] = item
    return out
}

inline fun <T> Array<T>.associateLongsBy(keySelector: (T) -> Long): LongMap<T> {
    val out = LongMap<T>(size)
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

inline fun <T, V> Array<T>.associateLongs(
    keySelector: (T) -> Long,
    valueTransform: (T) -> V,
): LongMap<V> {
    val out = LongMap<V>(size)
    for (item in this) out[keySelector(item)] = valueTransform(item)
    return out
}

inline fun <T> Iterable<T>.toIntSet(transform: (T) -> Int): IntSet {
    val out = IntSet(guessSize())
    for (item in this) out += transform(item)
    return out
}

inline fun <T> Iterable<T>.toLongSet(transform: (T) -> Long): LongSet {
    val out = LongSet(guessSize())
    for (item in this) out += transform(item)
    return out
}

inline fun <T> Array<T>.toIntSet(transform: (T) -> Int): IntSet {
    val out = IntSet(size)
    for (item in this) out += transform(item)
    return out
}

inline fun <T> Array<T>.toLongSet(transform: (T) -> Long): LongSet {
    val out = LongSet(size)
    for (item in this) out += transform(item)
    return out
}

fun Iterable<Int>.toIntArrayList(): IntArrayList {
    val out = IntArrayList(guessSize())
    for (item in this) out += item
    return out
}

fun Iterable<Long>.toLongArrayList(): LongArrayList {
    val out = LongArrayList(guessSize())
    for (item in this) out += item
    return out
}

fun Iterable<Int>.toIntSet(): IntSet {
    val out = IntSet(guessSize())
    for (item in this) out += item
    return out
}

fun Iterable<Long>.toLongSet(): LongSet {
    val out = LongSet(guessSize())
    for (item in this) out += item
    return out
}

fun IntArray.toIntArrayList(): IntArrayList = IntArrayList(this)
fun LongArray.toLongArrayList(): LongArrayList = LongArrayList(this)
fun IntArray.toIntSet(): IntSet = IntSet(this)
fun LongArray.toLongSet(): LongSet = LongSet(this)

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

inline fun Iterable<Int>.filterInts(predicate: (Int) -> Boolean): IntArrayList {
    val out = IntArrayList(guessSize())
    for (item in this) if (predicate(item)) out += item
    return out
}

inline fun Iterable<Long>.filterLongs(predicate: (Long) -> Boolean): LongArrayList {
    val out = LongArrayList(guessSize())
    for (item in this) if (predicate(item)) out += item
    return out
}
