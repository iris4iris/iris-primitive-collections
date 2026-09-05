package iris.collections

class LongArrayList private constructor(
    initial: LongArray,
    initialSize: Int,
    @Suppress("UNUSED_PARAMETER") unused: Boolean,
) : LongMutableList {

    constructor(initialCapacity: Int = DEFAULT_CAPACITY) : this(
        when {
            initialCapacity > 0 -> LongArray(initialCapacity)
            initialCapacity == 0 -> EMPTY
            else -> throw IllegalArgumentException("Illegal Capacity: $initialCapacity")
        },
        0,
        true,
    )

    constructor(source: LongArrayList) : this(source.elementData.copyOf(source.size), source.size, true)

    constructor(source: LongArray) : this(source.copyOf(), source.size, true)

    constructor(source: Collection<Long>) : this(maxOf(source.size, 0)) {
        addAll(source)
    }

    constructor(source: LongCollection) : this(source.size) {
        addAll(source)
    }

    @PublishedApi internal var elementData: LongArray = initial
    override var size: Int = initialSize
        private set
    private var modCount: Int = 0

    override fun ensureCapacity(minCapacity: Int) {
        if (minCapacity > elementData.size) grow(minCapacity)
    }

    override fun trimToSize() {
        if (size < elementData.size) {
            elementData = if (size == 0) EMPTY else elementData.copyOf(size)
        }
    }

    private fun grow(minCapacity: Int) {
        val oldCapacity = elementData.size
        elementData = if (oldCapacity > 0) {
            val newCapacity = newLength(oldCapacity, minCapacity - oldCapacity, oldCapacity shr 1)
            elementData.copyOf(newCapacity)
        } else {
            LongArray(maxOf(DEFAULT_CAPACITY, minCapacity))
        }
    }

    private fun growOne() {
        grow(size + 1)
    }

    override operator fun contains(element: Long): Boolean = indexOf(element) >= 0

    override fun containsAll(elements: LongCollection): Boolean {
        val it = elements.iterator()
        while (it.hasNext()) if (!contains(it.next())) return false
        return true
    }

    override fun containsAll(elements: Collection<Long>): Boolean {
        for (e in elements) if (!contains(e)) return false
        return true
    }

    override fun containsAny(elements: LongCollection): Boolean {
        val it = elements.iterator()
        while (it.hasNext()) if (contains(it.next())) return true
        return false
    }

    override fun containsAny(elements: Collection<Long>): Boolean {
        for (e in elements) if (contains(e)) return true
        return false
    }

    override fun indexOf(element: Long): Int = indexOfRange(element, 0, size)

    override fun lastIndexOf(element: Long): Int = lastIndexOfRange(element, 0, size)

    override fun indexOfRange(element: Long, start: Int, end: Int): Int {
        checkRange(start, end, size)
        val es = elementData
        for (i in start until end) if (element == es[i]) return i
        return -1
    }

    override fun lastIndexOfRange(element: Long, start: Int, end: Int): Int {
        checkRange(start, end, size)
        val es = elementData
        for (i in end - 1 downTo start) if (element == es[i]) return i
        return -1
    }

    override fun clone(): LongArrayList = LongArrayList(this)

    override fun toArray(): LongArray = elementData.copyOf(size)

    override fun toArray(destination: LongArray): LongArray {
        if (destination.size < size) return elementData.copyOf(size)
        elementData.copyInto(destination, 0, 0, size)
        return destination
    }

    override operator fun get(index: Int): Long {
        checkIndex(index, size)
        return elementData[index]
    }

    override operator fun set(index: Int, element: Long): Long {
        checkIndex(index, size)
        val old = elementData[index]
        elementData[index] = element
        return old
    }

    override fun add(element: Long): Boolean {
        if (size == elementData.size) growOne()
        elementData[size] = element
        size++
        modCount++
        return true
    }

    override fun add(index: Int, element: Long) {
        checkPositionIndex(index, size)
        if (size == elementData.size) growOne()
        if (index < size) {
            elementData.copyInto(elementData, index + 1, index, size)
        }
        elementData[index] = element
        size++
        modCount++
    }

    override fun removeAt(index: Int): Long {
        checkIndex(index, size)
        val es = elementData
        val old = es[index]
        val newSize = size - 1
        if (index < newSize) es.copyInto(es, index, index + 1, size)
        size = newSize
        modCount++
        return old
    }

    override fun removeElement(element: Long): Boolean {
        val i = indexOf(element)
        if (i < 0) return false
        removeAt(i)
        return true
    }

    override fun clear() {
        size = 0
        modCount++
    }

    override fun addAll(elements: LongCollection): Boolean {
        if (elements.isEmpty()) return false
        if (elements is LongArrayList) return addAll(elements.elementData, elements.size)
        ensureCapacity(size + elements.size)
        val it = elements.iterator()
        while (it.hasNext()) add(it.next())
        return true
    }

    override fun addAll(elements: LongArray): Boolean = addAll(elements, elements.size)

    private fun addAll(elements: LongArray, count: Int): Boolean {
        if (count == 0) return false
        ensureCapacity(size + count)
        elements.copyInto(elementData, size, 0, count)
        size += count
        modCount++
        return true
    }

    override fun addAll(elements: Collection<Long>): Boolean {
        if (elements.isEmpty()) return false
        ensureCapacity(size + elements.size)
        for (e in elements) {
            if (size == elementData.size) growOne()
            elementData[size] = e
            size++
        }
        modCount++
        return true
    }

    override fun addAll(index: Int, elements: LongCollection): Boolean {
        checkPositionIndex(index, size)
        if (elements.isEmpty()) return false
        if (elements is LongArrayList) return addAll(index, elements.elementData, elements.size)
        val tmp = elements.toArray()
        return addAll(index, tmp, tmp.size)
    }

    override fun addAll(index: Int, elements: LongArray): Boolean = addAll(index, elements, elements.size)

    private fun addAll(index: Int, elements: LongArray, count: Int): Boolean {
        checkPositionIndex(index, size)
        if (count == 0) return false
        ensureCapacity(size + count)
        if (index < size) {
            elementData.copyInto(elementData, index + count, index, size)
        }
        elements.copyInto(elementData, index, 0, count)
        size += count
        modCount++
        return true
    }

    override fun removeAll(elements: LongCollection): Boolean {
        return batchRemove(elements, complement = false)
    }

    override fun removeAll(elements: Collection<Long>): Boolean {
        var changed = false
        var w = 0
        val es = elementData
        val s = size
        for (r in 0 until s) {
            val e = es[r]
            if (e !in elements) es[w++] = e else changed = true
        }
        if (changed) {
            size = w
            modCount++
        }
        return changed
    }

    override fun retainAll(elements: LongCollection): Boolean {
        return batchRemove(elements, complement = true)
    }

    override fun retainAll(elements: Collection<Long>): Boolean {
        var changed = false
        var w = 0
        val es = elementData
        val s = size
        for (r in 0 until s) {
            val e = es[r]
            if (e in elements) es[w++] = e else changed = true
        }
        if (changed) {
            size = w
            modCount++
        }
        return changed
    }

    private fun batchRemove(elements: LongCollection, complement: Boolean): Boolean {
        var w = 0
        val es = elementData
        val s = size
        var changed = false
        for (r in 0 until s) {
            val e = es[r]
            if (elements.contains(e) == complement) es[w++] = e else changed = true
        }
        if (changed) {
            size = w
            modCount++
        }
        return changed
    }

    override fun removeIf(predicate: (Long) -> Boolean): Boolean {
        var w = 0
        val es = elementData
        val s = size
        var changed = false
        for (r in 0 until s) {
            val e = es[r]
            if (!predicate(e)) es[w++] = e else changed = true
        }
        if (changed) {
            size = w
            modCount++
        }
        return changed
    }

    override fun equalsRange(other: LongList, from: Int, to: Int): Boolean {
        checkRange(from, to, size)
        if (to - from != other.size) return false
        val es = elementData
        for (i in from until to) {
            val o = other[i - from]
            if (!(es[i] == o)) return false
        }
        return true
    }

    override fun sort() {
        if (size > 1) {
            elementData.sort(fromIndex = 0, toIndex = size)
            modCount++
        }
    }

    override fun sortDescending() {
        sort()
        reverseInPlace()
    }

    private fun reverseInPlace() {
        val es = elementData
        var i = 0
        var j = size - 1
        while (i < j) {
            val tmp = es[i]
            es[i] = es[j]
            es[j] = tmp
            i++
            j--
        }
        modCount++
    }

    override fun binarySearch(element: Long): Int {
        return primitiveBinarySearch(size) { elementData[it].compareTo(element) }
    }

    override fun subList(fromIndex: Int, toIndex: Int): LongArrayList {
        checkRange(fromIndex, toIndex, size)
        val n = toIndex - fromIndex
        val copy = LongArray(n)
        elementData.copyInto(copy, 0, fromIndex, toIndex)
        return LongArrayList(copy, n, true)
    }

    override fun iterator(): PrimitiveLongIterator = Itr()

    private inner class Itr : PrimitiveLongIterator {
        private var cursor = 0
        private var lastRet = -1
        private var expectedModCount = modCount

        override fun hasNext(): Boolean = cursor != size

        override fun next(): Long {
            if (expectedModCount != modCount) throw ConcurrentModificationException()
            if (cursor >= size) throw NoSuchElementException()
            lastRet = cursor
            return elementData[cursor++]
        }

        override fun remove() {
            check(lastRet >= 0) { "next() has not been called" }
            if (expectedModCount != modCount) throw ConcurrentModificationException()
            removeAt(lastRet)
            cursor = lastRet
            lastRet = -1
            expectedModCount = modCount
        }
    }

    override fun asMutableCollection(): MutableCollection<Long> = GenericCollection()
    override fun asMutableList(): MutableList<Long> = GenericList()

    private open inner class GenericCollection : MutableCollection<Long> {
        override val size: Int get() = this@LongArrayList.size
        override fun isEmpty(): Boolean = this@LongArrayList.isEmpty()
        override fun contains(element: Long): Boolean = this@LongArrayList.contains(element)
        override fun containsAll(elements: Collection<Long>): Boolean = this@LongArrayList.containsAll(elements)
        override fun add(element: Long): Boolean = this@LongArrayList.add(element)
        override fun addAll(elements: Collection<Long>): Boolean = this@LongArrayList.addAll(elements)
        override fun clear() = this@LongArrayList.clear()
        override fun iterator(): MutableIterator<Long> = BoxedItr()
        override fun remove(element: Long): Boolean = removeElement(element)
        override fun removeAll(elements: Collection<Long>): Boolean = this@LongArrayList.removeAll(elements)
        override fun retainAll(elements: Collection<Long>): Boolean = this@LongArrayList.retainAll(elements)
    }

    private inner class GenericList : GenericCollection(), MutableList<Long> {
        override fun get(index: Int): Long = this@LongArrayList[index]
        override fun set(index: Int, element: Long): Long = this@LongArrayList.set(index, element)
        override fun add(index: Int, element: Long) = this@LongArrayList.add(index, element)
        override fun addAll(index: Int, elements: Collection<Long>): Boolean {
            checkPositionIndex(index, size)
            if (elements.isEmpty()) return false
            val tmp = LongArray(elements.size)
            var i = 0
            for (e in elements) tmp[i++] = e
            return this@LongArrayList.addAll(index, tmp)
        }
        override fun indexOf(element: Long): Int = this@LongArrayList.indexOf(element)
        override fun lastIndexOf(element: Long): Int = this@LongArrayList.lastIndexOf(element)
        override fun removeAt(index: Int): Long = this@LongArrayList.removeAt(index)
        override fun listIterator(): MutableListIterator<Long> = listIterator(0)
        override fun listIterator(index: Int): MutableListIterator<Long> = BoxedListItr(index)
        override fun subList(fromIndex: Int, toIndex: Int): MutableList<Long> =
            this@LongArrayList.subList(fromIndex, toIndex).asMutableList()
    }

    private inner class BoxedItr : MutableIterator<Long> {
        private val delegate = this@LongArrayList.iterator()
        override fun hasNext(): Boolean = delegate.hasNext()
        override fun next(): Long = delegate.next()
        override fun remove() = delegate.remove()
    }

    private inner class BoxedListItr(index: Int) : MutableListIterator<Long> {
        private var cursor = index
        private var lastRet = -1
        private var expectedModCount = modCount

        init {
            checkPositionIndex(index, size)
        }

        override fun hasNext(): Boolean = cursor != size
        override fun hasPrevious(): Boolean = cursor != 0
        override fun nextIndex(): Int = cursor
        override fun previousIndex(): Int = cursor - 1

        override fun next(): Long {
            if (expectedModCount != modCount) throw ConcurrentModificationException()
            if (cursor >= size) throw NoSuchElementException()
            lastRet = cursor
            return elementData[cursor++]
        }

        override fun previous(): Long {
            if (expectedModCount != modCount) throw ConcurrentModificationException()
            if (cursor <= 0) throw NoSuchElementException()
            cursor--
            lastRet = cursor
            return elementData[cursor]
        }

        override fun remove() {
            check(lastRet >= 0)
            if (expectedModCount != modCount) throw ConcurrentModificationException()
            removeAt(lastRet)
            cursor = lastRet
            lastRet = -1
            expectedModCount = modCount
        }

        override fun set(element: Long) {
            check(lastRet >= 0)
            if (expectedModCount != modCount) throw ConcurrentModificationException()
            this@LongArrayList[lastRet] = element
        }

        override fun add(element: Long) {
            if (expectedModCount != modCount) throw ConcurrentModificationException()
            this@LongArrayList.add(cursor, element)
            cursor++
            lastRet = -1
            expectedModCount = modCount
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LongList) return false
        if (size != other.size) return false
        val es = elementData
        for (i in 0 until size) {
            val o = other[i]
            if (!(es[i] == o)) return false
        }
        return true
    }

    override fun hashCode(): Int {
        var result = 1
        val es = elementData
        for (i in 0 until size) {
            val element = es[i]
            result = 31 * result + (element.hashCode())
        }
        return result
    }

    override fun toString(): String = joinToString(prefix = "[", postfix = "]", limit = 100)

    override fun joinToString(
        separator: CharSequence,
        prefix: CharSequence,
        postfix: CharSequence,
        limit: Int,
        truncated: CharSequence,
        transform: ((Long) -> CharSequence)?,
    ): String {
        val sb = StringBuilder()
        sb.append(prefix)
        val s = size
        var count = 0
        for (i in 0 until s) {
            if (limit >= 0 && count >= limit) {
                sb.append(truncated)
                break
            }
            if (count > 0) sb.append(separator)
            val element = elementData[i]
            if (transform != null) sb.append(transform(element)) else sb.append(element)
            count++
        }
        sb.append(postfix)
        return sb.toString()
    }

    companion object {
        private val EMPTY: LongArray = longArrayOf()

        fun empty(): LongArrayList = LongArrayList(0)

        fun init(size: Int, init: (index: Int) -> Long): LongArrayList {
            require(size >= 0) { "Illegal size: $size" }
            val data = LongArray(size)
            for (i in 0 until size) data[i] = init(i)
            return LongArrayList(data, size, true)
        }
    }
}


fun LongList.sum(): Long {
    var s: Long = 0L
    forEach { s += it }
    return s
}


fun LongList.average(): Double {
    if (isEmpty()) return Double.NaN
    return sum().toDouble() / size
}


fun LongList.minOrNull(): Long? {
    if (isEmpty()) return null
    var m = this[0]
    for (i in 1 until size) if (this[i] < m) m = this[i]
    return m
}

fun LongList.maxOrNull(): Long? {
    if (isEmpty()) return null
    var m = this[0]
    for (i in 1 until size) if (this[i] > m) m = this[i]
    return m
}

fun LongList.first(): Long {
    if (isEmpty()) throw NoSuchElementException("Empty LongList")
    return this[0]
}

fun LongList.last(): Long {
    if (isEmpty()) throw NoSuchElementException("Empty LongList")
    return this[lastIndex]
}

fun LongList.firstOrNull(): Long? = if (isEmpty()) null else this[0]
fun LongList.lastOrNull(): Long? = if (isEmpty()) null else this[lastIndex]

inline fun LongCollection.forEach(action: (Long) -> Unit) {
    val it = iterator()
    while (it.hasNext()) action(it.next())
}

inline fun LongArrayList.forEach(action: (Long) -> Unit) {
    val data = elementData
    val s = size
    for (i in 0 until s) action(data[i])
}

inline fun LongArrayList.forEachIndexed(action: (index: Int, element: Long) -> Unit) {
    val data = elementData
    val s = size
    for (i in 0 until s) action(i, data[i])
}

inline fun LongArrayList.forEachUntil(action: (Long) -> Boolean) {
    val data = elementData
    val s = size
    for (i in 0 until s) if (!action(data[i])) return
}

inline fun LongCollection.any(predicate: (Long) -> Boolean): Boolean {
    val it = iterator()
    while (it.hasNext()) if (predicate(it.next())) return true
    return false
}

inline fun LongArrayList.any(predicate: (Long) -> Boolean): Boolean {
    val data = elementData
    val s = size
    for (i in 0 until s) if (predicate(data[i])) return true
    return false
}

inline fun LongCollection.all(predicate: (Long) -> Boolean): Boolean {
    val it = iterator()
    while (it.hasNext()) if (!predicate(it.next())) return false
    return true
}

inline fun LongArrayList.all(predicate: (Long) -> Boolean): Boolean {
    val data = elementData
    val s = size
    for (i in 0 until s) if (!predicate(data[i])) return false
    return true
}

inline fun LongCollection.filter(predicate: (Long) -> Boolean): LongArrayList {
    val dest = LongArrayList()
    val it = iterator()
    while (it.hasNext()) {
        val e = it.next()
        if (predicate(e)) dest.add(e)
    }
    return dest
}

inline fun LongArrayList.filter(predicate: (Long) -> Boolean): LongArrayList {
    val dest = LongArrayList(size)
    val data = elementData
    val s = size
    for (i in 0 until s) {
        val e = data[i]
        if (predicate(e)) dest.add(e)
    }
    return dest
}

inline fun <R> LongCollection.map(transform: (Long) -> R): List<R> {
    val dest = ArrayList<R>(size)
    val it = iterator()
    while (it.hasNext()) dest.add(transform(it.next()))
    return dest
}

inline fun <R> LongArrayList.map(transform: (Long) -> R): List<R> {
    val dest = ArrayList<R>(size)
    val data = elementData
    val s = size
    for (i in 0 until s) dest.add(transform(data[i]))
    return dest
}

fun LongArray.toLongArrayList(): LongArrayList = LongArrayList(this)

fun longArrayListOf(vararg values: Long): LongArrayList = LongArrayList(values)

