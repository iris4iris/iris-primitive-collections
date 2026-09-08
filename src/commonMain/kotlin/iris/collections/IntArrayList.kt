package iris.collections

class IntArrayList private constructor(
    initial: IntArray,
    initialSize: Int,
    @Suppress("UNUSED_PARAMETER") unused: Boolean,
) : IntMutableList {

    constructor(initialCapacity: Int = DEFAULT_CAPACITY) : this(
        when {
            initialCapacity > 0 -> IntArray(initialCapacity)
            initialCapacity == 0 -> EMPTY
            else -> throw IllegalArgumentException("Illegal Capacity: $initialCapacity")
        },
        0,
        true,
    )

    constructor(source: IntArrayList) : this(source.elementData.copyOf(source.size), source.size, true)

    constructor(source: IntArray) : this(source.copyOf(), source.size, true)

    constructor(source: Collection<Int>) : this(maxOf(source.size, 0)) {
        addAll(source)
    }

    constructor(source: IntCollection) : this(source.size) {
        addAll(source)
    }

    @PublishedApi internal var elementData: IntArray = initial
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
            IntArray(maxOf(DEFAULT_CAPACITY, minCapacity))
        }
    }

    private fun growOne() {
        grow(size + 1)
    }

    override operator fun contains(element: Int): Boolean = indexOf(element) >= 0

    override fun containsAll(elements: IntCollection): Boolean {
        val it = elements.iterator()
        while (it.hasNext()) if (!contains(it.next())) return false
        return true
    }

    override fun containsAll(elements: Collection<Int>): Boolean {
        for (e in elements) if (!contains(e)) return false
        return true
    }

    override fun containsAny(elements: IntCollection): Boolean {
        val it = elements.iterator()
        while (it.hasNext()) if (contains(it.next())) return true
        return false
    }

    override fun containsAny(elements: Collection<Int>): Boolean {
        for (e in elements) if (contains(e)) return true
        return false
    }

    override fun indexOf(element: Int): Int = indexOfRange(element, 0, size)

    override fun lastIndexOf(element: Int): Int = lastIndexOfRange(element, 0, size)

    override fun indexOfRange(element: Int, start: Int, end: Int): Int {
        checkRange(start, end, size)
        val es = elementData
        for (i in start until end) if (element == es[i]) return i
        return -1
    }

    override fun lastIndexOfRange(element: Int, start: Int, end: Int): Int {
        checkRange(start, end, size)
        val es = elementData
        for (i in end - 1 downTo start) if (element == es[i]) return i
        return -1
    }

    override fun clone(): IntArrayList = IntArrayList(this)

    override fun toArray(): IntArray = elementData.copyOf(size)

    override fun toArray(destination: IntArray): IntArray {
        if (destination.size < size) return elementData.copyOf(size)
        elementData.copyInto(destination, 0, 0, size)
        return destination
    }

    override operator fun get(index: Int): Int {
        checkIndex(index, size)
        return elementData[index]
    }

    override operator fun set(index: Int, element: Int): Int {
        checkIndex(index, size)
        val old = elementData[index]
        elementData[index] = element
        return old
    }

    override fun add(element: Int): Boolean {
        if (size == elementData.size) growOne()
        elementData[size] = element
        size++
        modCount++
        return true
    }

    override fun add(index: Int, element: Int) {
        checkPositionIndex(index, size)
        if (size == elementData.size) growOne()
        if (index < size) {
            elementData.copyInto(elementData, index + 1, index, size)
        }
        elementData[index] = element
        size++
        modCount++
    }

    override fun removeAt(index: Int): Int {
        checkIndex(index, size)
        val es = elementData
        val old = es[index]
        val newSize = size - 1
        if (index < newSize) es.copyInto(es, index, index + 1, size)
        size = newSize
        modCount++
        return old
    }

    override fun removeElement(element: Int): Boolean {
        val i = indexOf(element)
        if (i < 0) return false
        removeAt(i)
        return true
    }

    override fun clear() {
        size = 0
        modCount++
    }

    override fun addAll(elements: IntCollection): Boolean {
        if (elements.isEmpty()) return false
        if (elements is IntArrayList) return addAll(elements.elementData, elements.size)
        ensureCapacity(size + elements.size)
        val it = elements.iterator()
        while (it.hasNext()) add(it.next())
        return true
    }

    override fun addAll(elements: IntArray): Boolean = addAll(elements, elements.size)

    private fun addAll(elements: IntArray, count: Int): Boolean {
        if (count == 0) return false
        ensureCapacity(size + count)
        elements.copyInto(elementData, size, 0, count)
        size += count
        modCount++
        return true
    }

    override fun addAll(elements: Collection<Int>): Boolean {
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

    override fun addAll(index: Int, elements: IntCollection): Boolean {
        checkPositionIndex(index, size)
        if (elements.isEmpty()) return false
        if (elements is IntArrayList) return addAll(index, elements.elementData, elements.size)
        val tmp = elements.toArray()
        return addAll(index, tmp, tmp.size)
    }

    override fun addAll(index: Int, elements: IntArray): Boolean = addAll(index, elements, elements.size)

    private fun addAll(index: Int, elements: IntArray, count: Int): Boolean {
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

    override fun removeAll(elements: IntCollection): Boolean {
        return batchRemove(elements, complement = false)
    }

    override fun removeAll(elements: Collection<Int>): Boolean {
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

    override fun retainAll(elements: IntCollection): Boolean {
        return batchRemove(elements, complement = true)
    }

    override fun retainAll(elements: Collection<Int>): Boolean {
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

    private fun batchRemove(elements: IntCollection, complement: Boolean): Boolean {
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

    override fun removeIf(predicate: (Int) -> Boolean): Boolean {
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

    override fun equalsRange(other: IntList, from: Int, to: Int): Boolean {
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

    override fun binarySearch(element: Int): Int {
        return primitiveBinarySearch(size) { elementData[it].compareTo(element) }
    }

    override fun subList(fromIndex: Int, toIndex: Int): IntArrayList {
        checkRange(fromIndex, toIndex, size)
        val n = toIndex - fromIndex
        val copy = IntArray(n)
        elementData.copyInto(copy, 0, fromIndex, toIndex)
        return IntArrayList(copy, n, true)
    }

    override fun iterator(): PrimitiveIntIterator = Itr()

    private inner class Itr : PrimitiveIntIterator {
        private var cursor = 0
        private var lastRet = -1
        private var expectedModCount = modCount

        override fun hasNext(): Boolean = cursor != size

        override fun next(): Int {
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

    override fun asMutableCollection(): MutableCollection<Int> = GenericCollection()
    override fun asMutableList(): MutableList<Int> = GenericList()

    private open inner class GenericCollection : MutableCollection<Int> {
        override val size: Int get() = this@IntArrayList.size
        override fun isEmpty(): Boolean = this@IntArrayList.isEmpty()
        override fun contains(element: Int): Boolean = this@IntArrayList.contains(element)
        override fun containsAll(elements: Collection<Int>): Boolean = this@IntArrayList.containsAll(elements)
        override fun add(element: Int): Boolean = this@IntArrayList.add(element)
        override fun addAll(elements: Collection<Int>): Boolean = this@IntArrayList.addAll(elements)
        override fun clear() = this@IntArrayList.clear()
        override fun iterator(): MutableIterator<Int> = BoxedItr()
        override fun remove(element: Int): Boolean = removeElement(element)
        override fun removeAll(elements: Collection<Int>): Boolean = this@IntArrayList.removeAll(elements)
        override fun retainAll(elements: Collection<Int>): Boolean = this@IntArrayList.retainAll(elements)
    }

    private inner class GenericList : GenericCollection(), MutableList<Int> {
        override fun get(index: Int): Int = this@IntArrayList[index]
        override fun set(index: Int, element: Int): Int = this@IntArrayList.set(index, element)
        override fun add(index: Int, element: Int) = this@IntArrayList.add(index, element)
        override fun addAll(index: Int, elements: Collection<Int>): Boolean {
            checkPositionIndex(index, size)
            if (elements.isEmpty()) return false
            val tmp = IntArray(elements.size)
            var i = 0
            for (e in elements) tmp[i++] = e
            return this@IntArrayList.addAll(index, tmp)
        }
        override fun indexOf(element: Int): Int = this@IntArrayList.indexOf(element)
        override fun lastIndexOf(element: Int): Int = this@IntArrayList.lastIndexOf(element)
        override fun removeAt(index: Int): Int = this@IntArrayList.removeAt(index)
        override fun listIterator(): MutableListIterator<Int> = listIterator(0)
        override fun listIterator(index: Int): MutableListIterator<Int> = BoxedListItr(index)
        override fun subList(fromIndex: Int, toIndex: Int): MutableList<Int> =
            this@IntArrayList.subList(fromIndex, toIndex).asMutableList()
    }

    private inner class BoxedItr : MutableIterator<Int> {
        private val delegate = this@IntArrayList.iterator()
        override fun hasNext(): Boolean = delegate.hasNext()
        override fun next(): Int = delegate.next()
        override fun remove() = delegate.remove()
    }

    private inner class BoxedListItr(index: Int) : MutableListIterator<Int> {
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

        override fun next(): Int {
            if (expectedModCount != modCount) throw ConcurrentModificationException()
            if (cursor >= size) throw NoSuchElementException()
            lastRet = cursor
            return elementData[cursor++]
        }

        override fun previous(): Int {
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

        override fun set(element: Int) {
            check(lastRet >= 0)
            if (expectedModCount != modCount) throw ConcurrentModificationException()
            this@IntArrayList[lastRet] = element
        }

        override fun add(element: Int) {
            if (expectedModCount != modCount) throw ConcurrentModificationException()
            this@IntArrayList.add(cursor, element)
            cursor++
            lastRet = -1
            expectedModCount = modCount
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IntList) return false
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
            result = 31 * result + (element)
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
        transform: ((Int) -> CharSequence)?,
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

    override fun sum(): Int {
        val data = elementData
        val s = size
        var acc = 0
        for (i in 0 until s) acc += data[i]
        return acc
    }

    override fun average(): Double {
        if (size == 0) return Double.NaN
        return sum().toDouble() / size
    }

    override fun first(): Int {
        if (size == 0) throw NoSuchElementException("Empty IntArrayList")
        return elementData[0]
    }

    override fun last(): Int {
        val s = size
        if (s == 0) throw NoSuchElementException("Empty IntArrayList")
        return elementData[s - 1]
    }

    override fun firstOrElse(default: Int): Int =
        if (size == 0) default else elementData[0]

    override fun lastOrElse(default: Int): Int {
        val s = size
        return if (s == 0) default else elementData[s - 1]
    }

    override fun minOrElse(default: Int): Int {
        val s = size
        if (s == 0) return default
        val data = elementData
        var m = data[0]
        for (i in 1 until s) if (data[i] < m) m = data[i]
        return m
    }

    override fun maxOrElse(default: Int): Int {
        val s = size
        if (s == 0) return default
        val data = elementData
        var m = data[0]
        for (i in 1 until s) if (data[i] > m) m = data[i]
        return m
    }

    companion object {
        private val EMPTY: IntArray = intArrayOf()

        fun empty(): IntArrayList = IntArrayList(0)

        fun init(size: Int, init: (index: Int) -> Int): IntArrayList {
            require(size >= 0) { "Illegal size: $size" }
            val data = IntArray(size)
            for (i in 0 until size) data[i] = init(i)
            return IntArrayList(data, size, true)
        }
    }
}


inline fun IntCollection.forEach(action: (Int) -> Unit) {
    val it = iterator()
    while (it.hasNext()) action(it.next())
}

inline fun IntArrayList.forEach(action: (Int) -> Unit) {
    val data = elementData
    val s = size
    for (i in 0 until s) action(data[i])
}

inline fun IntArrayList.forEachIndexed(action: (index: Int, element: Int) -> Unit) {
    val data = elementData
    val s = size
    for (i in 0 until s) action(i, data[i])
}

inline fun IntArrayList.forEachUntil(action: (Int) -> Boolean) {
    val data = elementData
    val s = size
    for (i in 0 until s) if (!action(data[i])) return
}

inline fun IntCollection.any(predicate: (Int) -> Boolean): Boolean {
    val it = iterator()
    while (it.hasNext()) if (predicate(it.next())) return true
    return false
}

inline fun IntArrayList.any(predicate: (Int) -> Boolean): Boolean {
    val data = elementData
    val s = size
    for (i in 0 until s) if (predicate(data[i])) return true
    return false
}

inline fun IntCollection.all(predicate: (Int) -> Boolean): Boolean {
    val it = iterator()
    while (it.hasNext()) if (!predicate(it.next())) return false
    return true
}

inline fun IntArrayList.all(predicate: (Int) -> Boolean): Boolean {
    val data = elementData
    val s = size
    for (i in 0 until s) if (!predicate(data[i])) return false
    return true
}

inline fun IntCollection.filter(predicate: (Int) -> Boolean): IntArrayList {
    val dest = IntArrayList()
    val it = iterator()
    while (it.hasNext()) {
        val e = it.next()
        if (predicate(e)) dest.add(e)
    }
    return dest
}

inline fun IntArrayList.filter(predicate: (Int) -> Boolean): IntArrayList {
    val dest = IntArrayList(size)
    val data = elementData
    val s = size
    for (i in 0 until s) {
        val e = data[i]
        if (predicate(e)) dest.add(e)
    }
    return dest
}

inline fun <R> IntCollection.map(transform: (Int) -> R): List<R> {
    val dest = ArrayList<R>(size)
    val it = iterator()
    while (it.hasNext()) dest.add(transform(it.next()))
    return dest
}

inline fun <R> IntArrayList.map(transform: (Int) -> R): List<R> {
    val dest = ArrayList<R>(size)
    val data = elementData
    val s = size
    for (i in 0 until s) dest.add(transform(data[i]))
    return dest
}

fun IntArray.toIntArrayList(): IntArrayList = IntArrayList(this)

fun intArrayListOf(vararg values: Int): IntArrayList = IntArrayList(values)

