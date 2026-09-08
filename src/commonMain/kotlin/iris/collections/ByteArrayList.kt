package iris.collections

class ByteArrayList private constructor(
    initial: ByteArray,
    initialSize: Int,
    @Suppress("UNUSED_PARAMETER") unused: Boolean,
) : ByteMutableList {

    constructor(initialCapacity: Int = DEFAULT_CAPACITY) : this(
        when {
            initialCapacity > 0 -> ByteArray(initialCapacity)
            initialCapacity == 0 -> EMPTY
            else -> throw IllegalArgumentException("Illegal Capacity: $initialCapacity")
        },
        0,
        true,
    )

    constructor(source: ByteArrayList) : this(source.elementData.copyOf(source.size), source.size, true)

    constructor(source: ByteArray) : this(source.copyOf(), source.size, true)

    constructor(source: Collection<Byte>) : this(maxOf(source.size, 0)) {
        addAll(source)
    }

    constructor(source: ByteCollection) : this(source.size) {
        addAll(source)
    }

    @PublishedApi internal var elementData: ByteArray = initial
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
            ByteArray(maxOf(DEFAULT_CAPACITY, minCapacity))
        }
    }

    private fun growOne() {
        grow(size + 1)
    }

    override operator fun contains(element: Byte): Boolean = indexOf(element) >= 0

    override fun containsAll(elements: ByteCollection): Boolean {
        val it = elements.iterator()
        while (it.hasNext()) if (!contains(it.next())) return false
        return true
    }

    override fun containsAll(elements: Collection<Byte>): Boolean {
        for (e in elements) if (!contains(e)) return false
        return true
    }

    override fun containsAny(elements: ByteCollection): Boolean {
        val it = elements.iterator()
        while (it.hasNext()) if (contains(it.next())) return true
        return false
    }

    override fun containsAny(elements: Collection<Byte>): Boolean {
        for (e in elements) if (contains(e)) return true
        return false
    }

    override fun indexOf(element: Byte): Int = indexOfRange(element, 0, size)

    override fun lastIndexOf(element: Byte): Int = lastIndexOfRange(element, 0, size)

    override fun indexOfRange(element: Byte, start: Int, end: Int): Int {
        checkRange(start, end, size)
        val es = elementData
        for (i in start until end) if (element == es[i]) return i
        return -1
    }

    override fun lastIndexOfRange(element: Byte, start: Int, end: Int): Int {
        checkRange(start, end, size)
        val es = elementData
        for (i in end - 1 downTo start) if (element == es[i]) return i
        return -1
    }

    override fun clone(): ByteArrayList = ByteArrayList(this)

    override fun toArray(): ByteArray = elementData.copyOf(size)

    override fun toArray(destination: ByteArray): ByteArray {
        if (destination.size < size) return elementData.copyOf(size)
        elementData.copyInto(destination, 0, 0, size)
        return destination
    }

    override operator fun get(index: Int): Byte {
        checkIndex(index, size)
        return elementData[index]
    }

    override operator fun set(index: Int, element: Byte): Byte {
        checkIndex(index, size)
        val old = elementData[index]
        elementData[index] = element
        return old
    }

    override fun add(element: Byte): Boolean {
        if (size == elementData.size) growOne()
        elementData[size] = element
        size++
        modCount++
        return true
    }

    override fun add(index: Int, element: Byte) {
        checkPositionIndex(index, size)
        if (size == elementData.size) growOne()
        if (index < size) {
            elementData.copyInto(elementData, index + 1, index, size)
        }
        elementData[index] = element
        size++
        modCount++
    }

    override fun removeAt(index: Int): Byte {
        checkIndex(index, size)
        val es = elementData
        val old = es[index]
        val newSize = size - 1
        if (index < newSize) es.copyInto(es, index, index + 1, size)
        size = newSize
        modCount++
        return old
    }

    override fun removeElement(element: Byte): Boolean {
        val i = indexOf(element)
        if (i < 0) return false
        removeAt(i)
        return true
    }

    override fun clear() {
        size = 0
        modCount++
    }

    override fun addAll(elements: ByteCollection): Boolean {
        if (elements.isEmpty()) return false
        if (elements is ByteArrayList) return addAll(elements.elementData, elements.size)
        ensureCapacity(size + elements.size)
        val it = elements.iterator()
        while (it.hasNext()) add(it.next())
        return true
    }

    override fun addAll(elements: ByteArray): Boolean = addAll(elements, elements.size)

    private fun addAll(elements: ByteArray, count: Int): Boolean {
        if (count == 0) return false
        ensureCapacity(size + count)
        elements.copyInto(elementData, size, 0, count)
        size += count
        modCount++
        return true
    }

    override fun addAll(elements: Collection<Byte>): Boolean {
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

    override fun addAll(index: Int, elements: ByteCollection): Boolean {
        checkPositionIndex(index, size)
        if (elements.isEmpty()) return false
        if (elements is ByteArrayList) return addAll(index, elements.elementData, elements.size)
        val tmp = elements.toArray()
        return addAll(index, tmp, tmp.size)
    }

    override fun addAll(index: Int, elements: ByteArray): Boolean = addAll(index, elements, elements.size)

    private fun addAll(index: Int, elements: ByteArray, count: Int): Boolean {
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

    override fun removeAll(elements: ByteCollection): Boolean {
        return batchRemove(elements, complement = false)
    }

    override fun removeAll(elements: Collection<Byte>): Boolean {
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

    override fun retainAll(elements: ByteCollection): Boolean {
        return batchRemove(elements, complement = true)
    }

    override fun retainAll(elements: Collection<Byte>): Boolean {
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

    private fun batchRemove(elements: ByteCollection, complement: Boolean): Boolean {
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

    override fun removeIf(predicate: (Byte) -> Boolean): Boolean {
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

    override fun equalsRange(other: ByteList, from: Int, to: Int): Boolean {
        checkRange(from, to, size)
        if (to - from != other.size) return false
        val es = elementData
        for (i in from until to) {
            val o = other[i - from]
            if (es[i] != o) return false
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

    override fun binarySearch(element: Byte): Int {
        return primitiveBinarySearch(size) { elementData[it].compareTo(element) }
    }

    override fun subList(fromIndex: Int, toIndex: Int): ByteArrayList {
        checkRange(fromIndex, toIndex, size)
        val n = toIndex - fromIndex
        val copy = ByteArray(n)
        elementData.copyInto(copy, 0, fromIndex, toIndex)
        return ByteArrayList(copy, n, true)
    }

    override fun iterator(): PrimitiveByteIterator = Itr()

    private inner class Itr : PrimitiveByteIterator {
        private var cursor = 0
        private var lastRet = -1
        private var expectedModCount = modCount

        override fun hasNext(): Boolean = cursor != size

        override fun next(): Byte {
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

    override fun asMutableCollection(): MutableCollection<Byte> = GenericCollection()
    override fun asMutableList(): MutableList<Byte> = GenericList()

    private open inner class GenericCollection : MutableCollection<Byte> {
        override val size: Int get() = this@ByteArrayList.size
        override fun isEmpty(): Boolean = this@ByteArrayList.isEmpty()
        override fun contains(element: Byte): Boolean = this@ByteArrayList.contains(element)
        override fun containsAll(elements: Collection<Byte>): Boolean = this@ByteArrayList.containsAll(elements)
        override fun add(element: Byte): Boolean = this@ByteArrayList.add(element)
        override fun addAll(elements: Collection<Byte>): Boolean = this@ByteArrayList.addAll(elements)
        override fun clear() = this@ByteArrayList.clear()
        override fun iterator(): MutableIterator<Byte> = BoxedItr()
        override fun remove(element: Byte): Boolean = removeElement(element)
        override fun removeAll(elements: Collection<Byte>): Boolean = this@ByteArrayList.removeAll(elements)
        override fun retainAll(elements: Collection<Byte>): Boolean = this@ByteArrayList.retainAll(elements)
    }

    private inner class GenericList : GenericCollection(), MutableList<Byte> {
        override fun get(index: Int): Byte = this@ByteArrayList[index]
        override fun set(index: Int, element: Byte): Byte = this@ByteArrayList.set(index, element)
        override fun add(index: Int, element: Byte) = this@ByteArrayList.add(index, element)
        override fun addAll(index: Int, elements: Collection<Byte>): Boolean {
            checkPositionIndex(index, size)
            if (elements.isEmpty()) return false
            val tmp = ByteArray(elements.size)
            var i = 0
            for (e in elements) tmp[i++] = e
            return this@ByteArrayList.addAll(index, tmp)
        }
        override fun indexOf(element: Byte): Int = this@ByteArrayList.indexOf(element)
        override fun lastIndexOf(element: Byte): Int = this@ByteArrayList.lastIndexOf(element)
        override fun removeAt(index: Int): Byte = this@ByteArrayList.removeAt(index)
        override fun listIterator(): MutableListIterator<Byte> = listIterator(0)
        override fun listIterator(index: Int): MutableListIterator<Byte> = BoxedListItr(index)
        override fun subList(fromIndex: Int, toIndex: Int): MutableList<Byte> =
            this@ByteArrayList.subList(fromIndex, toIndex).asMutableList()
    }

    private inner class BoxedItr : MutableIterator<Byte> {
        private val delegate = this@ByteArrayList.iterator()
        override fun hasNext(): Boolean = delegate.hasNext()
        override fun next(): Byte = delegate.next()
        override fun remove() = delegate.remove()
    }

    private inner class BoxedListItr(index: Int) : MutableListIterator<Byte> {
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

        override fun next(): Byte {
            if (expectedModCount != modCount) throw ConcurrentModificationException()
            if (cursor >= size) throw NoSuchElementException()
            lastRet = cursor
            return elementData[cursor++]
        }

        override fun previous(): Byte {
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

        override fun set(element: Byte) {
            check(lastRet >= 0)
            if (expectedModCount != modCount) throw ConcurrentModificationException()
            this@ByteArrayList[lastRet] = element
        }

        override fun add(element: Byte) {
            if (expectedModCount != modCount) throw ConcurrentModificationException()
            this@ByteArrayList.add(cursor, element)
            cursor++
            lastRet = -1
            expectedModCount = modCount
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ByteList) return false
        if (size != other.size) return false
        val es = elementData
        for (i in 0 until size) {
            val o = other[i]
            if (es[i] != o) return false
        }
        return true
    }

    override fun hashCode(): Int {
        var result = 1
        val es = elementData
        for (i in 0 until size) {
            val element = es[i]
            result = 31 * result + (element.toInt())
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
        transform: ((Byte) -> CharSequence)?,
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
        for (i in 0 until s) acc += data[i].toInt()
        return acc
    }

    override fun average(): Double {
        if (size == 0) return Double.NaN
        return sum().toDouble() / size
    }

    override fun first(): Byte {
        if (size == 0) throw NoSuchElementException("Empty ByteArrayList")
        return elementData[0]
    }

    override fun last(): Byte {
        val s = size
        if (s == 0) throw NoSuchElementException("Empty ByteArrayList")
        return elementData[s - 1]
    }

    override fun firstOrElse(default: Byte): Byte =
        if (size == 0) default else elementData[0]

    override fun lastOrElse(default: Byte): Byte {
        val s = size
        return if (s == 0) default else elementData[s - 1]
    }

    override fun minOrElse(default: Byte): Byte {
        val s = size
        if (s == 0) return default
        val data = elementData
        var m = data[0]
        for (i in 1 until s) if (data[i] < m) m = data[i]
        return m
    }

    override fun maxOrElse(default: Byte): Byte {
        val s = size
        if (s == 0) return default
        val data = elementData
        var m = data[0]
        for (i in 1 until s) if (data[i] > m) m = data[i]
        return m
    }

    companion object {
        private val EMPTY: ByteArray = byteArrayOf()

        fun empty(): ByteArrayList = ByteArrayList(0)

        fun init(size: Int, init: (index: Int) -> Byte): ByteArrayList {
            require(size >= 0) { "Illegal size: $size" }
            val data = ByteArray(size)
            for (i in 0 until size) data[i] = init(i)
            return ByteArrayList(data, size, true)
        }
    }
}


inline fun ByteCollection.forEach(action: (Byte) -> Unit) {
    val it = iterator()
    while (it.hasNext()) action(it.next())
}

inline fun ByteArrayList.forEach(action: (Byte) -> Unit) {
    val data = elementData
    val s = size
    for (i in 0 until s) action(data[i])
}

inline fun ByteArrayList.forEachIndexed(action: (index: Int, element: Byte) -> Unit) {
    val data = elementData
    val s = size
    for (i in 0 until s) action(i, data[i])
}

inline fun ByteArrayList.forEachUntil(action: (Byte) -> Boolean) {
    val data = elementData
    val s = size
    for (i in 0 until s) if (!action(data[i])) return
}

inline fun ByteCollection.any(predicate: (Byte) -> Boolean): Boolean {
    val it = iterator()
    while (it.hasNext()) if (predicate(it.next())) return true
    return false
}

inline fun ByteArrayList.any(predicate: (Byte) -> Boolean): Boolean {
    val data = elementData
    val s = size
    for (i in 0 until s) if (predicate(data[i])) return true
    return false
}

inline fun ByteCollection.all(predicate: (Byte) -> Boolean): Boolean {
    val it = iterator()
    while (it.hasNext()) if (!predicate(it.next())) return false
    return true
}

inline fun ByteArrayList.all(predicate: (Byte) -> Boolean): Boolean {
    val data = elementData
    val s = size
    for (i in 0 until s) if (!predicate(data[i])) return false
    return true
}

inline fun ByteCollection.filter(predicate: (Byte) -> Boolean): ByteArrayList {
    val dest = ByteArrayList()
    val it = iterator()
    while (it.hasNext()) {
        val e = it.next()
        if (predicate(e)) dest.add(e)
    }
    return dest
}

inline fun ByteArrayList.filter(predicate: (Byte) -> Boolean): ByteArrayList {
    val dest = ByteArrayList(size)
    val data = elementData
    val s = size
    for (i in 0 until s) {
        val e = data[i]
        if (predicate(e)) dest.add(e)
    }
    return dest
}

inline fun <R> ByteCollection.map(transform: (Byte) -> R): List<R> {
    val dest = ArrayList<R>(size)
    val it = iterator()
    while (it.hasNext()) dest.add(transform(it.next()))
    return dest
}

inline fun <R> ByteArrayList.map(transform: (Byte) -> R): List<R> {
    val dest = ArrayList<R>(size)
    val data = elementData
    val s = size
    for (i in 0 until s) dest.add(transform(data[i]))
    return dest
}

fun ByteArray.toByteArrayList(): ByteArrayList = ByteArrayList(this)

fun byteArrayListOf(vararg values: Byte): ByteArrayList = ByteArrayList(values)

