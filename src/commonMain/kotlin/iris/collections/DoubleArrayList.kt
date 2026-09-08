package iris.collections

class DoubleArrayList private constructor(
    initial: DoubleArray,
    initialSize: Int,
    @Suppress("UNUSED_PARAMETER") unused: Boolean,
) : DoubleMutableList {

    constructor(initialCapacity: Int = DEFAULT_CAPACITY) : this(
        when {
            initialCapacity > 0 -> DoubleArray(initialCapacity)
            initialCapacity == 0 -> EMPTY
            else -> throw IllegalArgumentException("Illegal Capacity: $initialCapacity")
        },
        0,
        true,
    )

    constructor(source: DoubleArrayList) : this(source.elementData.copyOf(source.size), source.size, true)

    constructor(source: DoubleArray) : this(source.copyOf(), source.size, true)

    constructor(source: Collection<Double>) : this(maxOf(source.size, 0)) {
        addAll(source)
    }

    constructor(source: DoubleCollection) : this(source.size) {
        addAll(source)
    }

    @PublishedApi internal var elementData: DoubleArray = initial
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
            DoubleArray(maxOf(DEFAULT_CAPACITY, minCapacity))
        }
    }

    private fun growOne() {
        grow(size + 1)
    }

    override operator fun contains(element: Double): Boolean = indexOf(element) >= 0

    override fun containsAll(elements: DoubleCollection): Boolean {
        val it = elements.iterator()
        while (it.hasNext()) if (!contains(it.next())) return false
        return true
    }

    override fun containsAll(elements: Collection<Double>): Boolean {
        for (e in elements) if (!contains(e)) return false
        return true
    }

    override fun containsAny(elements: DoubleCollection): Boolean {
        val it = elements.iterator()
        while (it.hasNext()) if (contains(it.next())) return true
        return false
    }

    override fun containsAny(elements: Collection<Double>): Boolean {
        for (e in elements) if (contains(e)) return true
        return false
    }

    override fun indexOf(element: Double): Int = indexOfRange(element, 0, size)

    override fun lastIndexOf(element: Double): Int = lastIndexOfRange(element, 0, size)

    override fun indexOfRange(element: Double, start: Int, end: Int): Int {
        checkRange(start, end, size)
        val es = elementData
        for (i in start until end) if (element.toRawBits() == es[i].toRawBits()) return i
        return -1
    }

    override fun lastIndexOfRange(element: Double, start: Int, end: Int): Int {
        checkRange(start, end, size)
        val es = elementData
        for (i in end - 1 downTo start) if (element.toRawBits() == es[i].toRawBits()) return i
        return -1
    }

    override fun clone(): DoubleArrayList = DoubleArrayList(this)

    override fun toArray(): DoubleArray = elementData.copyOf(size)

    override fun toArray(destination: DoubleArray): DoubleArray {
        if (destination.size < size) return elementData.copyOf(size)
        elementData.copyInto(destination, 0, 0, size)
        return destination
    }

    override operator fun get(index: Int): Double {
        checkIndex(index, size)
        return elementData[index]
    }

    override operator fun set(index: Int, element: Double): Double {
        checkIndex(index, size)
        val old = elementData[index]
        elementData[index] = element
        return old
    }

    override fun add(element: Double): Boolean {
        if (size == elementData.size) growOne()
        elementData[size] = element
        size++
        modCount++
        return true
    }

    override fun add(index: Int, element: Double) {
        checkPositionIndex(index, size)
        if (size == elementData.size) growOne()
        if (index < size) {
            elementData.copyInto(elementData, index + 1, index, size)
        }
        elementData[index] = element
        size++
        modCount++
    }

    override fun removeAt(index: Int): Double {
        checkIndex(index, size)
        val es = elementData
        val old = es[index]
        val newSize = size - 1
        if (index < newSize) es.copyInto(es, index, index + 1, size)
        size = newSize
        modCount++
        return old
    }

    override fun removeElement(element: Double): Boolean {
        val i = indexOf(element)
        if (i < 0) return false
        removeAt(i)
        return true
    }

    override fun clear() {
        size = 0
        modCount++
    }

    override fun addAll(elements: DoubleCollection): Boolean {
        if (elements.isEmpty()) return false
        if (elements is DoubleArrayList) return addAll(elements.elementData, elements.size)
        ensureCapacity(size + elements.size)
        val it = elements.iterator()
        while (it.hasNext()) add(it.next())
        return true
    }

    override fun addAll(elements: DoubleArray): Boolean = addAll(elements, elements.size)

    private fun addAll(elements: DoubleArray, count: Int): Boolean {
        if (count == 0) return false
        ensureCapacity(size + count)
        elements.copyInto(elementData, size, 0, count)
        size += count
        modCount++
        return true
    }

    override fun addAll(elements: Collection<Double>): Boolean {
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

    override fun addAll(index: Int, elements: DoubleCollection): Boolean {
        checkPositionIndex(index, size)
        if (elements.isEmpty()) return false
        if (elements is DoubleArrayList) return addAll(index, elements.elementData, elements.size)
        val tmp = elements.toArray()
        return addAll(index, tmp, tmp.size)
    }

    override fun addAll(index: Int, elements: DoubleArray): Boolean = addAll(index, elements, elements.size)

    private fun addAll(index: Int, elements: DoubleArray, count: Int): Boolean {
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

    override fun removeAll(elements: DoubleCollection): Boolean {
        return batchRemove(elements, complement = false)
    }

    override fun removeAll(elements: Collection<Double>): Boolean {
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

    override fun retainAll(elements: DoubleCollection): Boolean {
        return batchRemove(elements, complement = true)
    }

    override fun retainAll(elements: Collection<Double>): Boolean {
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

    private fun batchRemove(elements: DoubleCollection, complement: Boolean): Boolean {
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

    override fun removeIf(predicate: (Double) -> Boolean): Boolean {
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

    override fun equalsRange(other: DoubleList, from: Int, to: Int): Boolean {
        checkRange(from, to, size)
        if (to - from != other.size) return false
        val es = elementData
        for (i in from until to) {
            val o = other[i - from]
            if (!(es[i].toRawBits() == o.toRawBits())) return false
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

    override fun binarySearch(element: Double): Int {
        return primitiveBinarySearch(size) { elementData[it].compareTo(element) }
    }

    override fun subList(fromIndex: Int, toIndex: Int): DoubleArrayList {
        checkRange(fromIndex, toIndex, size)
        val n = toIndex - fromIndex
        val copy = DoubleArray(n)
        elementData.copyInto(copy, 0, fromIndex, toIndex)
        return DoubleArrayList(copy, n, true)
    }

    override fun iterator(): PrimitiveDoubleIterator = Itr()

    private inner class Itr : PrimitiveDoubleIterator {
        private var cursor = 0
        private var lastRet = -1
        private var expectedModCount = modCount

        override fun hasNext(): Boolean = cursor != size

        override fun next(): Double {
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

    override fun asMutableCollection(): MutableCollection<Double> = GenericCollection()
    override fun asMutableList(): MutableList<Double> = GenericList()

    private open inner class GenericCollection : MutableCollection<Double> {
        override val size: Int get() = this@DoubleArrayList.size
        override fun isEmpty(): Boolean = this@DoubleArrayList.isEmpty()
        override fun contains(element: Double): Boolean = this@DoubleArrayList.contains(element)
        override fun containsAll(elements: Collection<Double>): Boolean = this@DoubleArrayList.containsAll(elements)
        override fun add(element: Double): Boolean = this@DoubleArrayList.add(element)
        override fun addAll(elements: Collection<Double>): Boolean = this@DoubleArrayList.addAll(elements)
        override fun clear() = this@DoubleArrayList.clear()
        override fun iterator(): MutableIterator<Double> = BoxedItr()
        override fun remove(element: Double): Boolean = removeElement(element)
        override fun removeAll(elements: Collection<Double>): Boolean = this@DoubleArrayList.removeAll(elements)
        override fun retainAll(elements: Collection<Double>): Boolean = this@DoubleArrayList.retainAll(elements)
    }

    private inner class GenericList : GenericCollection(), MutableList<Double> {
        override fun get(index: Int): Double = this@DoubleArrayList[index]
        override fun set(index: Int, element: Double): Double = this@DoubleArrayList.set(index, element)
        override fun add(index: Int, element: Double) = this@DoubleArrayList.add(index, element)
        override fun addAll(index: Int, elements: Collection<Double>): Boolean {
            checkPositionIndex(index, size)
            if (elements.isEmpty()) return false
            val tmp = DoubleArray(elements.size)
            var i = 0
            for (e in elements) tmp[i++] = e
            return this@DoubleArrayList.addAll(index, tmp)
        }
        override fun indexOf(element: Double): Int = this@DoubleArrayList.indexOf(element)
        override fun lastIndexOf(element: Double): Int = this@DoubleArrayList.lastIndexOf(element)
        override fun removeAt(index: Int): Double = this@DoubleArrayList.removeAt(index)
        override fun listIterator(): MutableListIterator<Double> = listIterator(0)
        override fun listIterator(index: Int): MutableListIterator<Double> = BoxedListItr(index)
        override fun subList(fromIndex: Int, toIndex: Int): MutableList<Double> =
            this@DoubleArrayList.subList(fromIndex, toIndex).asMutableList()
    }

    private inner class BoxedItr : MutableIterator<Double> {
        private val delegate = this@DoubleArrayList.iterator()
        override fun hasNext(): Boolean = delegate.hasNext()
        override fun next(): Double = delegate.next()
        override fun remove() = delegate.remove()
    }

    private inner class BoxedListItr(index: Int) : MutableListIterator<Double> {
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

        override fun next(): Double {
            if (expectedModCount != modCount) throw ConcurrentModificationException()
            if (cursor >= size) throw NoSuchElementException()
            lastRet = cursor
            return elementData[cursor++]
        }

        override fun previous(): Double {
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

        override fun set(element: Double) {
            check(lastRet >= 0)
            if (expectedModCount != modCount) throw ConcurrentModificationException()
            this@DoubleArrayList[lastRet] = element
        }

        override fun add(element: Double) {
            if (expectedModCount != modCount) throw ConcurrentModificationException()
            this@DoubleArrayList.add(cursor, element)
            cursor++
            lastRet = -1
            expectedModCount = modCount
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DoubleList) return false
        if (size != other.size) return false
        val es = elementData
        for (i in 0 until size) {
            val o = other[i]
            if (!(es[i].toRawBits() == o.toRawBits())) return false
        }
        return true
    }

    override fun hashCode(): Int {
        var result = 1
        val es = elementData
        for (i in 0 until size) {
            val element = es[i]
            result = 31 * result + (element.toBits().hashCode())
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
        transform: ((Double) -> CharSequence)?,
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


    override fun sum(): Double {
        val data = elementData
        val s = size
        var acc = 0.0
        for (i in 0 until s) acc += data[i]
        return acc
    }

    override fun average(): Double {
        if (size == 0) return Double.NaN
        return sum() / size
    }

    override fun first(): Double {
        if (size == 0) throw NoSuchElementException("Empty DoubleArrayList")
        return elementData[0]
    }

    override fun last(): Double {
        val s = size
        if (s == 0) throw NoSuchElementException("Empty DoubleArrayList")
        return elementData[s - 1]
    }

    override fun firstOrElse(default: Double): Double =
        if (size == 0) default else elementData[0]

    override fun lastOrElse(default: Double): Double {
        val s = size
        return if (s == 0) default else elementData[s - 1]
    }

    override fun minOrElse(default: Double): Double {
        val s = size
        if (s == 0) return default
        val data = elementData
        var m = data[0]
        for (i in 1 until s) if (data[i] < m) m = data[i]
        return m
    }

    override fun maxOrElse(default: Double): Double {
        val s = size
        if (s == 0) return default
        val data = elementData
        var m = data[0]
        for (i in 1 until s) if (data[i] > m) m = data[i]
        return m
    }

    companion object {
        private val EMPTY: DoubleArray = doubleArrayOf()

        fun empty(): DoubleArrayList = DoubleArrayList(0)

        fun init(size: Int, init: (index: Int) -> Double): DoubleArrayList {
            require(size >= 0) { "Illegal size: $size" }
            val data = DoubleArray(size)
            for (i in 0 until size) data[i] = init(i)
            return DoubleArrayList(data, size, true)
        }
    }
}


inline fun DoubleCollection.forEach(action: (Double) -> Unit) {
    val it = iterator()
    while (it.hasNext()) action(it.next())
}

inline fun DoubleArrayList.forEach(action: (Double) -> Unit) {
    val data = elementData
    val s = size
    for (i in 0 until s) action(data[i])
}

inline fun DoubleArrayList.forEachIndexed(action: (index: Int, element: Double) -> Unit) {
    val data = elementData
    val s = size
    for (i in 0 until s) action(i, data[i])
}

inline fun DoubleArrayList.forEachUntil(action: (Double) -> Boolean) {
    val data = elementData
    val s = size
    for (i in 0 until s) if (!action(data[i])) return
}

inline fun DoubleCollection.any(predicate: (Double) -> Boolean): Boolean {
    val it = iterator()
    while (it.hasNext()) if (predicate(it.next())) return true
    return false
}

inline fun DoubleArrayList.any(predicate: (Double) -> Boolean): Boolean {
    val data = elementData
    val s = size
    for (i in 0 until s) if (predicate(data[i])) return true
    return false
}

inline fun DoubleCollection.all(predicate: (Double) -> Boolean): Boolean {
    val it = iterator()
    while (it.hasNext()) if (!predicate(it.next())) return false
    return true
}

inline fun DoubleArrayList.all(predicate: (Double) -> Boolean): Boolean {
    val data = elementData
    val s = size
    for (i in 0 until s) if (!predicate(data[i])) return false
    return true
}

inline fun DoubleCollection.filter(predicate: (Double) -> Boolean): DoubleArrayList {
    val dest = DoubleArrayList()
    val it = iterator()
    while (it.hasNext()) {
        val e = it.next()
        if (predicate(e)) dest.add(e)
    }
    return dest
}

inline fun DoubleArrayList.filter(predicate: (Double) -> Boolean): DoubleArrayList {
    val dest = DoubleArrayList(size)
    val data = elementData
    val s = size
    for (i in 0 until s) {
        val e = data[i]
        if (predicate(e)) dest.add(e)
    }
    return dest
}

inline fun <R> DoubleCollection.map(transform: (Double) -> R): List<R> {
    val dest = ArrayList<R>(size)
    val it = iterator()
    while (it.hasNext()) dest.add(transform(it.next()))
    return dest
}

inline fun <R> DoubleArrayList.map(transform: (Double) -> R): List<R> {
    val dest = ArrayList<R>(size)
    val data = elementData
    val s = size
    for (i in 0 until s) dest.add(transform(data[i]))
    return dest
}

fun DoubleArray.toDoubleArrayList(): DoubleArrayList = DoubleArrayList(this)

fun doubleArrayListOf(vararg values: Double): DoubleArrayList = DoubleArrayList(values)

