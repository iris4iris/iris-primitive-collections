package iris.collections

class CharArrayList private constructor(
    initial: CharArray,
    initialSize: Int,
    @Suppress("UNUSED_PARAMETER") unused: Boolean,
) : CharMutableList {

    constructor(initialCapacity: Int = DEFAULT_CAPACITY) : this(
        when {
            initialCapacity > 0 -> CharArray(initialCapacity)
            initialCapacity == 0 -> EMPTY
            else -> throw IllegalArgumentException("Illegal Capacity: $initialCapacity")
        },
        0,
        true,
    )

    constructor(source: CharArrayList) : this(source.elementData.copyOf(source.size), source.size, true)

    constructor(source: CharArray) : this(source.copyOf(), source.size, true)

    constructor(source: Collection<Char>) : this(maxOf(source.size, 0)) {
        addAll(source)
    }

    constructor(source: CharCollection) : this(source.size) {
        addAll(source)
    }

    @PublishedApi internal var elementData: CharArray = initial
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
            CharArray(maxOf(DEFAULT_CAPACITY, minCapacity))
        }
    }

    private fun growOne() {
        grow(size + 1)
    }

    override operator fun contains(element: Char): Boolean = indexOf(element) >= 0

    override fun containsAll(elements: CharCollection): Boolean {
        val it = elements.iterator()
        while (it.hasNext()) if (!contains(it.next())) return false
        return true
    }

    override fun containsAll(elements: Collection<Char>): Boolean {
        for (e in elements) if (!contains(e)) return false
        return true
    }

    override fun containsAny(elements: CharCollection): Boolean {
        val it = elements.iterator()
        while (it.hasNext()) if (contains(it.next())) return true
        return false
    }

    override fun containsAny(elements: Collection<Char>): Boolean {
        for (e in elements) if (contains(e)) return true
        return false
    }

    override fun indexOf(element: Char): Int = indexOfRange(element, 0, size)

    override fun lastIndexOf(element: Char): Int = lastIndexOfRange(element, 0, size)

    override fun indexOfRange(element: Char, start: Int, end: Int): Int {
        checkRange(start, end, size)
        val es = elementData
        for (i in start until end) if (element == es[i]) return i
        return -1
    }

    override fun lastIndexOfRange(element: Char, start: Int, end: Int): Int {
        checkRange(start, end, size)
        val es = elementData
        for (i in end - 1 downTo start) if (element == es[i]) return i
        return -1
    }

    override fun clone(): CharArrayList = CharArrayList(this)

    override fun toArray(): CharArray = elementData.copyOf(size)

    override fun toArray(destination: CharArray): CharArray {
        if (destination.size < size) return elementData.copyOf(size)
        elementData.copyInto(destination, 0, 0, size)
        return destination
    }

    override operator fun get(index: Int): Char {
        checkIndex(index, size)
        return elementData[index]
    }

    override operator fun set(index: Int, element: Char): Char {
        checkIndex(index, size)
        val old = elementData[index]
        elementData[index] = element
        return old
    }

    override fun add(element: Char): Boolean {
        if (size == elementData.size) growOne()
        elementData[size] = element
        size++
        modCount++
        return true
    }

    override fun add(index: Int, element: Char) {
        checkPositionIndex(index, size)
        if (size == elementData.size) growOne()
        if (index < size) {
            elementData.copyInto(elementData, index + 1, index, size)
        }
        elementData[index] = element
        size++
        modCount++
    }

    override fun removeAt(index: Int): Char {
        checkIndex(index, size)
        val es = elementData
        val old = es[index]
        val newSize = size - 1
        if (index < newSize) es.copyInto(es, index, index + 1, size)
        size = newSize
        modCount++
        return old
    }

    override fun removeElement(element: Char): Boolean {
        val i = indexOf(element)
        if (i < 0) return false
        removeAt(i)
        return true
    }

    override fun clear() {
        size = 0
        modCount++
    }

    override fun addAll(elements: CharCollection): Boolean {
        if (elements.isEmpty()) return false
        if (elements is CharArrayList) return addAll(elements.elementData, elements.size)
        ensureCapacity(size + elements.size)
        val it = elements.iterator()
        while (it.hasNext()) add(it.next())
        return true
    }

    override fun addAll(elements: CharArray): Boolean = addAll(elements, elements.size)

    private fun addAll(elements: CharArray, count: Int): Boolean {
        if (count == 0) return false
        ensureCapacity(size + count)
        elements.copyInto(elementData, size, 0, count)
        size += count
        modCount++
        return true
    }

    override fun addAll(elements: Collection<Char>): Boolean {
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

    override fun addAll(index: Int, elements: CharCollection): Boolean {
        checkPositionIndex(index, size)
        if (elements.isEmpty()) return false
        if (elements is CharArrayList) return addAll(index, elements.elementData, elements.size)
        val tmp = elements.toArray()
        return addAll(index, tmp, tmp.size)
    }

    override fun addAll(index: Int, elements: CharArray): Boolean = addAll(index, elements, elements.size)

    private fun addAll(index: Int, elements: CharArray, count: Int): Boolean {
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

    override fun removeAll(elements: CharCollection): Boolean {
        return batchRemove(elements, complement = false)
    }

    override fun removeAll(elements: Collection<Char>): Boolean {
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

    override fun retainAll(elements: CharCollection): Boolean {
        return batchRemove(elements, complement = true)
    }

    override fun retainAll(elements: Collection<Char>): Boolean {
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

    private fun batchRemove(elements: CharCollection, complement: Boolean): Boolean {
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

    override fun removeIf(predicate: (Char) -> Boolean): Boolean {
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

    override fun equalsRange(other: CharList, from: Int, to: Int): Boolean {
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

    override fun binarySearch(element: Char): Int {
        return primitiveBinarySearch(size) { elementData[it].compareTo(element) }
    }

    override fun subList(fromIndex: Int, toIndex: Int): CharArrayList {
        checkRange(fromIndex, toIndex, size)
        val n = toIndex - fromIndex
        val copy = CharArray(n)
        elementData.copyInto(copy, 0, fromIndex, toIndex)
        return CharArrayList(copy, n, true)
    }

    override fun iterator(): PrimitiveCharIterator = Itr()

    private inner class Itr : PrimitiveCharIterator {
        private var cursor = 0
        private var lastRet = -1
        private var expectedModCount = modCount

        override fun hasNext(): Boolean = cursor != size

        override fun next(): Char {
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

    override fun asMutableCollection(): MutableCollection<Char> = GenericCollection()
    override fun asMutableList(): MutableList<Char> = GenericList()

    private open inner class GenericCollection : MutableCollection<Char> {
        override val size: Int get() = this@CharArrayList.size
        override fun isEmpty(): Boolean = this@CharArrayList.isEmpty()
        override fun contains(element: Char): Boolean = this@CharArrayList.contains(element)
        override fun containsAll(elements: Collection<Char>): Boolean = this@CharArrayList.containsAll(elements)
        override fun add(element: Char): Boolean = this@CharArrayList.add(element)
        override fun addAll(elements: Collection<Char>): Boolean = this@CharArrayList.addAll(elements)
        override fun clear() = this@CharArrayList.clear()
        override fun iterator(): MutableIterator<Char> = BoxedItr()
        override fun remove(element: Char): Boolean = removeElement(element)
        override fun removeAll(elements: Collection<Char>): Boolean = this@CharArrayList.removeAll(elements)
        override fun retainAll(elements: Collection<Char>): Boolean = this@CharArrayList.retainAll(elements)
    }

    private inner class GenericList : GenericCollection(), MutableList<Char> {
        override fun get(index: Int): Char = this@CharArrayList[index]
        override fun set(index: Int, element: Char): Char = this@CharArrayList.set(index, element)
        override fun add(index: Int, element: Char) = this@CharArrayList.add(index, element)
        override fun addAll(index: Int, elements: Collection<Char>): Boolean {
            checkPositionIndex(index, size)
            if (elements.isEmpty()) return false
            val tmp = CharArray(elements.size)
            var i = 0
            for (e in elements) tmp[i++] = e
            return this@CharArrayList.addAll(index, tmp)
        }
        override fun indexOf(element: Char): Int = this@CharArrayList.indexOf(element)
        override fun lastIndexOf(element: Char): Int = this@CharArrayList.lastIndexOf(element)
        override fun removeAt(index: Int): Char = this@CharArrayList.removeAt(index)
        override fun listIterator(): MutableListIterator<Char> = listIterator(0)
        override fun listIterator(index: Int): MutableListIterator<Char> = BoxedListItr(index)
        override fun subList(fromIndex: Int, toIndex: Int): MutableList<Char> =
            this@CharArrayList.subList(fromIndex, toIndex).asMutableList()
    }

    private inner class BoxedItr : MutableIterator<Char> {
        private val delegate = this@CharArrayList.iterator()
        override fun hasNext(): Boolean = delegate.hasNext()
        override fun next(): Char = delegate.next()
        override fun remove() = delegate.remove()
    }

    private inner class BoxedListItr(index: Int) : MutableListIterator<Char> {
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

        override fun next(): Char {
            if (expectedModCount != modCount) throw ConcurrentModificationException()
            if (cursor >= size) throw NoSuchElementException()
            lastRet = cursor
            return elementData[cursor++]
        }

        override fun previous(): Char {
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

        override fun set(element: Char) {
            check(lastRet >= 0)
            if (expectedModCount != modCount) throw ConcurrentModificationException()
            this@CharArrayList[lastRet] = element
        }

        override fun add(element: Char) {
            if (expectedModCount != modCount) throw ConcurrentModificationException()
            this@CharArrayList.add(cursor, element)
            cursor++
            lastRet = -1
            expectedModCount = modCount
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CharList) return false
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
            result = 31 * result + (element.code)
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
        transform: ((Char) -> CharSequence)?,
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
        private val EMPTY: CharArray = charArrayOf()

        fun empty(): CharArrayList = CharArrayList(0)

        fun init(size: Int, init: (index: Int) -> Char): CharArrayList {
            require(size >= 0) { "Illegal size: $size" }
            val data = CharArray(size)
            for (i in 0 until size) data[i] = init(i)
            return CharArrayList(data, size, true)
        }
    }
}


fun CharList.minOrNull(): Char? {
    if (isEmpty()) return null
    var m = this[0]
    for (i in 1 until size) if (this[i] < m) m = this[i]
    return m
}

fun CharList.maxOrNull(): Char? {
    if (isEmpty()) return null
    var m = this[0]
    for (i in 1 until size) if (this[i] > m) m = this[i]
    return m
}

fun CharList.first(): Char {
    if (isEmpty()) throw NoSuchElementException("Empty CharList")
    return this[0]
}

fun CharList.last(): Char {
    if (isEmpty()) throw NoSuchElementException("Empty CharList")
    return this[lastIndex]
}

fun CharList.firstOrNull(): Char? = if (isEmpty()) null else this[0]
fun CharList.lastOrNull(): Char? = if (isEmpty()) null else this[lastIndex]

inline fun CharCollection.forEach(action: (Char) -> Unit) {
    val it = iterator()
    while (it.hasNext()) action(it.next())
}

inline fun CharArrayList.forEach(action: (Char) -> Unit) {
    val data = elementData
    val s = size
    for (i in 0 until s) action(data[i])
}

inline fun CharArrayList.forEachIndexed(action: (index: Int, element: Char) -> Unit) {
    val data = elementData
    val s = size
    for (i in 0 until s) action(i, data[i])
}

inline fun CharArrayList.forEachUntil(action: (Char) -> Boolean) {
    val data = elementData
    val s = size
    for (i in 0 until s) if (!action(data[i])) return
}

inline fun CharCollection.any(predicate: (Char) -> Boolean): Boolean {
    val it = iterator()
    while (it.hasNext()) if (predicate(it.next())) return true
    return false
}

inline fun CharArrayList.any(predicate: (Char) -> Boolean): Boolean {
    val data = elementData
    val s = size
    for (i in 0 until s) if (predicate(data[i])) return true
    return false
}

inline fun CharCollection.all(predicate: (Char) -> Boolean): Boolean {
    val it = iterator()
    while (it.hasNext()) if (!predicate(it.next())) return false
    return true
}

inline fun CharArrayList.all(predicate: (Char) -> Boolean): Boolean {
    val data = elementData
    val s = size
    for (i in 0 until s) if (!predicate(data[i])) return false
    return true
}

inline fun CharCollection.filter(predicate: (Char) -> Boolean): CharArrayList {
    val dest = CharArrayList()
    val it = iterator()
    while (it.hasNext()) {
        val e = it.next()
        if (predicate(e)) dest.add(e)
    }
    return dest
}

inline fun CharArrayList.filter(predicate: (Char) -> Boolean): CharArrayList {
    val dest = CharArrayList(size)
    val data = elementData
    val s = size
    for (i in 0 until s) {
        val e = data[i]
        if (predicate(e)) dest.add(e)
    }
    return dest
}

inline fun <R> CharCollection.map(transform: (Char) -> R): List<R> {
    val dest = ArrayList<R>(size)
    val it = iterator()
    while (it.hasNext()) dest.add(transform(it.next()))
    return dest
}

inline fun <R> CharArrayList.map(transform: (Char) -> R): List<R> {
    val dest = ArrayList<R>(size)
    val data = elementData
    val s = size
    for (i in 0 until s) dest.add(transform(data[i]))
    return dest
}

fun CharArray.toCharArrayList(): CharArrayList = CharArrayList(this)

fun charArrayListOf(vararg values: Char): CharArrayList = CharArrayList(values)

