package iris.collections

class FloatArrayList private constructor(
    initial: FloatArray,
    initialSize: Int,
    @Suppress("UNUSED_PARAMETER") unused: Boolean,
) : FloatMutableList {

    constructor(initialCapacity: Int = DEFAULT_CAPACITY) : this(
        when {
            initialCapacity > 0 -> FloatArray(initialCapacity)
            initialCapacity == 0 -> EMPTY
            else -> throw IllegalArgumentException("Illegal Capacity: $initialCapacity")
        },
        0,
        true,
    )

    constructor(source: FloatArrayList) : this(source.elementData.copyOf(source.size), source.size, true)

    constructor(source: FloatArray) : this(source.copyOf(), source.size, true)

    constructor(source: Collection<Float>) : this(maxOf(source.size, 0)) {
        addAll(source)
    }

    constructor(source: FloatCollection) : this(source.size) {
        addAll(source)
    }

    @PublishedApi internal var elementData: FloatArray = initial
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
            FloatArray(maxOf(DEFAULT_CAPACITY, minCapacity))
        }
    }

    private fun growOne() {
        grow(size + 1)
    }

    override operator fun contains(element: Float): Boolean = indexOf(element) >= 0

    override fun containsAll(elements: FloatCollection): Boolean {
        val it = elements.iterator()
        while (it.hasNext()) if (!contains(it.next())) return false
        return true
    }

    override fun containsAll(elements: Collection<Float>): Boolean {
        for (e in elements) if (!contains(e)) return false
        return true
    }

    override fun containsAny(elements: FloatCollection): Boolean {
        val it = elements.iterator()
        while (it.hasNext()) if (contains(it.next())) return true
        return false
    }

    override fun containsAny(elements: Collection<Float>): Boolean {
        for (e in elements) if (contains(e)) return true
        return false
    }

    override fun indexOf(element: Float): Int = indexOfRange(element, 0, size)

    override fun lastIndexOf(element: Float): Int = lastIndexOfRange(element, 0, size)

    override fun indexOfRange(element: Float, start: Int, end: Int): Int {
        checkRange(start, end, size)
        val es = elementData
        for (i in start until end) if (element.toBits() == es[i].toBits()) return i
        return -1
    }

    override fun lastIndexOfRange(element: Float, start: Int, end: Int): Int {
        checkRange(start, end, size)
        val es = elementData
        for (i in end - 1 downTo start) if (element.toBits() == es[i].toBits()) return i
        return -1
    }

    override fun clone(): FloatArrayList = FloatArrayList(this)

    override fun toArray(): FloatArray = elementData.copyOf(size)

    override fun toArray(destination: FloatArray): FloatArray {
        if (destination.size < size) return elementData.copyOf(size)
        elementData.copyInto(destination, 0, 0, size)
        return destination
    }

    override operator fun get(index: Int): Float {
        checkIndex(index, size)
        return elementData[index]
    }

    override operator fun set(index: Int, element: Float): Float {
        checkIndex(index, size)
        val old = elementData[index]
        elementData[index] = element
        return old
    }

    override fun add(element: Float): Boolean {
        if (size == elementData.size) growOne()
        elementData[size] = element
        size++
        modCount++
        return true
    }

    override fun add(index: Int, element: Float) {
        checkPositionIndex(index, size)
        if (size == elementData.size) growOne()
        if (index < size) {
            elementData.copyInto(elementData, index + 1, index, size)
        }
        elementData[index] = element
        size++
        modCount++
    }

    override fun removeAt(index: Int): Float {
        checkIndex(index, size)
        val es = elementData
        val old = es[index]
        val newSize = size - 1
        if (index < newSize) es.copyInto(es, index, index + 1, size)
        size = newSize
        modCount++
        return old
    }

    override fun removeElement(element: Float): Boolean {
        val i = indexOf(element)
        if (i < 0) return false
        removeAt(i)
        return true
    }

    override fun clear() {
        size = 0
        modCount++
    }

    override fun addAll(elements: FloatCollection): Boolean {
        if (elements.isEmpty()) return false
        if (elements is FloatArrayList) return addAll(elements.elementData, elements.size)
        ensureCapacity(size + elements.size)
        val it = elements.iterator()
        while (it.hasNext()) add(it.next())
        return true
    }

    override fun addAll(elements: FloatArray): Boolean = addAll(elements, elements.size)

    private fun addAll(elements: FloatArray, count: Int): Boolean {
        if (count == 0) return false
        ensureCapacity(size + count)
        elements.copyInto(elementData, size, 0, count)
        size += count
        modCount++
        return true
    }

    override fun addAll(elements: Collection<Float>): Boolean {
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

    override fun addAll(index: Int, elements: FloatCollection): Boolean {
        checkPositionIndex(index, size)
        if (elements.isEmpty()) return false
        if (elements is FloatArrayList) return addAll(index, elements.elementData, elements.size)
        val tmp = elements.toArray()
        return addAll(index, tmp, tmp.size)
    }

    override fun addAll(index: Int, elements: FloatArray): Boolean = addAll(index, elements, elements.size)

    private fun addAll(index: Int, elements: FloatArray, count: Int): Boolean {
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

    override fun removeAll(elements: FloatCollection): Boolean {
        return batchRemove(elements, complement = false)
    }

    override fun removeAll(elements: Collection<Float>): Boolean {
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

    override fun retainAll(elements: FloatCollection): Boolean {
        return batchRemove(elements, complement = true)
    }

    override fun retainAll(elements: Collection<Float>): Boolean {
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

    private fun batchRemove(elements: FloatCollection, complement: Boolean): Boolean {
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

    override fun removeIf(predicate: (Float) -> Boolean): Boolean {
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

    override fun equalsRange(other: FloatList, from: Int, to: Int): Boolean {
        checkRange(from, to, size)
        if (to - from != other.size) return false
        val es = elementData
        for (i in from until to) {
            val o = other[i - from]
            if (!(es[i].toBits() == o.toBits())) return false
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

    override fun binarySearch(element: Float): Int {
        return primitiveBinarySearch(size) { elementData[it].compareTo(element) }
    }

    override fun subList(fromIndex: Int, toIndex: Int): FloatArrayList {
        checkRange(fromIndex, toIndex, size)
        val n = toIndex - fromIndex
        val copy = FloatArray(n)
        elementData.copyInto(copy, 0, fromIndex, toIndex)
        return FloatArrayList(copy, n, true)
    }

    override fun iterator(): PrimitiveFloatIterator = Itr()

    private inner class Itr : PrimitiveFloatIterator {
        private var cursor = 0
        private var lastRet = -1
        private var expectedModCount = modCount

        override fun hasNext(): Boolean = cursor != size

        override fun next(): Float {
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

    override fun asMutableCollection(): MutableCollection<Float> = GenericCollection()
    override fun asMutableList(): MutableList<Float> = GenericList()

    private open inner class GenericCollection : MutableCollection<Float> {
        override val size: Int get() = this@FloatArrayList.size
        override fun isEmpty(): Boolean = this@FloatArrayList.isEmpty()
        override fun contains(element: Float): Boolean = this@FloatArrayList.contains(element)
        override fun containsAll(elements: Collection<Float>): Boolean = this@FloatArrayList.containsAll(elements)
        override fun add(element: Float): Boolean = this@FloatArrayList.add(element)
        override fun addAll(elements: Collection<Float>): Boolean = this@FloatArrayList.addAll(elements)
        override fun clear() = this@FloatArrayList.clear()
        override fun iterator(): MutableIterator<Float> = BoxedItr()
        override fun remove(element: Float): Boolean = removeElement(element)
        override fun removeAll(elements: Collection<Float>): Boolean = this@FloatArrayList.removeAll(elements)
        override fun retainAll(elements: Collection<Float>): Boolean = this@FloatArrayList.retainAll(elements)
    }

    private inner class GenericList : GenericCollection(), MutableList<Float> {
        override fun get(index: Int): Float = this@FloatArrayList[index]
        override fun set(index: Int, element: Float): Float = this@FloatArrayList.set(index, element)
        override fun add(index: Int, element: Float) = this@FloatArrayList.add(index, element)
        override fun addAll(index: Int, elements: Collection<Float>): Boolean {
            checkPositionIndex(index, size)
            if (elements.isEmpty()) return false
            val tmp = FloatArray(elements.size)
            var i = 0
            for (e in elements) tmp[i++] = e
            return this@FloatArrayList.addAll(index, tmp)
        }
        override fun indexOf(element: Float): Int = this@FloatArrayList.indexOf(element)
        override fun lastIndexOf(element: Float): Int = this@FloatArrayList.lastIndexOf(element)
        override fun removeAt(index: Int): Float = this@FloatArrayList.removeAt(index)
        override fun listIterator(): MutableListIterator<Float> = listIterator(0)
        override fun listIterator(index: Int): MutableListIterator<Float> = BoxedListItr(index)
        override fun subList(fromIndex: Int, toIndex: Int): MutableList<Float> =
            this@FloatArrayList.subList(fromIndex, toIndex).asMutableList()
    }

    private inner class BoxedItr : MutableIterator<Float> {
        private val delegate = this@FloatArrayList.iterator()
        override fun hasNext(): Boolean = delegate.hasNext()
        override fun next(): Float = delegate.next()
        override fun remove() = delegate.remove()
    }

    private inner class BoxedListItr(index: Int) : MutableListIterator<Float> {
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

        override fun next(): Float {
            if (expectedModCount != modCount) throw ConcurrentModificationException()
            if (cursor >= size) throw NoSuchElementException()
            lastRet = cursor
            return elementData[cursor++]
        }

        override fun previous(): Float {
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

        override fun set(element: Float) {
            check(lastRet >= 0)
            if (expectedModCount != modCount) throw ConcurrentModificationException()
            this@FloatArrayList[lastRet] = element
        }

        override fun add(element: Float) {
            if (expectedModCount != modCount) throw ConcurrentModificationException()
            this@FloatArrayList.add(cursor, element)
            cursor++
            lastRet = -1
            expectedModCount = modCount
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FloatList) return false
        if (size != other.size) return false
        val es = elementData
        for (i in 0 until size) {
            val o = other[i]
            if (!(es[i].toBits() == o.toBits())) return false
        }
        return true
    }

    override fun hashCode(): Int {
        var result = 1
        val es = elementData
        for (i in 0 until size) {
            val element = es[i]
            result = 31 * result + (element.toBits())
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
        transform: ((Float) -> CharSequence)?,
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


    override fun sum(): Float {
        val data = elementData
        val s = size
        var acc = 0f
        for (i in 0 until s) acc += data[i]
        return acc
    }

    override fun average(): Double {
        if (size == 0) return Double.NaN
        return sum().toDouble() / size
    }

    override fun first(): Float {
        if (size == 0) throw NoSuchElementException("Empty FloatArrayList")
        return elementData[0]
    }

    override fun last(): Float {
        val s = size
        if (s == 0) throw NoSuchElementException("Empty FloatArrayList")
        return elementData[s - 1]
    }

    override fun firstOrElse(default: Float): Float =
        if (size == 0) default else elementData[0]

    override fun lastOrElse(default: Float): Float {
        val s = size
        return if (s == 0) default else elementData[s - 1]
    }

    override fun minOrElse(default: Float): Float {
        val s = size
        if (s == 0) return default
        val data = elementData
        var m = data[0]
        for (i in 1 until s) if (data[i] < m) m = data[i]
        return m
    }

    override fun maxOrElse(default: Float): Float {
        val s = size
        if (s == 0) return default
        val data = elementData
        var m = data[0]
        for (i in 1 until s) if (data[i] > m) m = data[i]
        return m
    }

    companion object {
        private val EMPTY: FloatArray = floatArrayOf()

        fun empty(): FloatArrayList = FloatArrayList(0)

        fun init(size: Int, init: (index: Int) -> Float): FloatArrayList {
            require(size >= 0) { "Illegal size: $size" }
            val data = FloatArray(size)
            for (i in 0 until size) data[i] = init(i)
            return FloatArrayList(data, size, true)
        }
    }
}


inline fun FloatCollection.forEach(action: (Float) -> Unit) {
    val it = iterator()
    while (it.hasNext()) action(it.next())
}

inline fun FloatArrayList.forEach(action: (Float) -> Unit) {
    val data = elementData
    val s = size
    for (i in 0 until s) action(data[i])
}

inline fun FloatArrayList.forEachIndexed(action: (index: Int, element: Float) -> Unit) {
    val data = elementData
    val s = size
    for (i in 0 until s) action(i, data[i])
}

inline fun FloatArrayList.forEachUntil(action: (Float) -> Boolean) {
    val data = elementData
    val s = size
    for (i in 0 until s) if (!action(data[i])) return
}

inline fun FloatCollection.any(predicate: (Float) -> Boolean): Boolean {
    val it = iterator()
    while (it.hasNext()) if (predicate(it.next())) return true
    return false
}

inline fun FloatArrayList.any(predicate: (Float) -> Boolean): Boolean {
    val data = elementData
    val s = size
    for (i in 0 until s) if (predicate(data[i])) return true
    return false
}

inline fun FloatCollection.all(predicate: (Float) -> Boolean): Boolean {
    val it = iterator()
    while (it.hasNext()) if (!predicate(it.next())) return false
    return true
}

inline fun FloatArrayList.all(predicate: (Float) -> Boolean): Boolean {
    val data = elementData
    val s = size
    for (i in 0 until s) if (!predicate(data[i])) return false
    return true
}

inline fun FloatCollection.filter(predicate: (Float) -> Boolean): FloatArrayList {
    val dest = FloatArrayList()
    val it = iterator()
    while (it.hasNext()) {
        val e = it.next()
        if (predicate(e)) dest.add(e)
    }
    return dest
}

inline fun FloatArrayList.filter(predicate: (Float) -> Boolean): FloatArrayList {
    val dest = FloatArrayList(size)
    val data = elementData
    val s = size
    for (i in 0 until s) {
        val e = data[i]
        if (predicate(e)) dest.add(e)
    }
    return dest
}

inline fun <R> FloatCollection.map(transform: (Float) -> R): List<R> {
    val dest = ArrayList<R>(size)
    val it = iterator()
    while (it.hasNext()) dest.add(transform(it.next()))
    return dest
}

inline fun <R> FloatArrayList.map(transform: (Float) -> R): List<R> {
    val dest = ArrayList<R>(size)
    val data = elementData
    val s = size
    for (i in 0 until s) dest.add(transform(data[i]))
    return dest
}

fun FloatArray.toFloatArrayList(): FloatArrayList = FloatArrayList(this)

fun floatArrayListOf(vararg values: Float): FloatArrayList = FloatArrayList(values)

