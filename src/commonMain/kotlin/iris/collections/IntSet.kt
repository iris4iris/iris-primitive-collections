package iris.collections

class IntSet(
    expectedSize: Int = 0,
    val nulled: Int = Int.MIN_VALUE,
) {
    @PublishedApi internal var keys = allocKeys(hashCapacityFor(expectedSize))
    @PublishedApi internal var state = ByteArray(keys.size)
    private var deleted = 0
    var size: Int = 0
        private set
    private var modCount = 0

    constructor(source: IntArray, nulled: Int = Int.MIN_VALUE) : this(source.size, nulled) {
        for (key in source) add(key)
    }

    constructor(source: IntSet) : this(source.size, source.nulled) {
        source.forEach { add(it) }
    }

    private fun allocKeys(n: Int): IntArray {
        val a = IntArray(n)
        if (nulled != 0) a.fill(nulled)
        return a
    }

    fun isEmpty(): Boolean = size == 0
    fun isNotEmpty(): Boolean = size != 0

    fun contains(key: Int): Boolean = key != nulled && indexOf(key) >= 0

    fun add(key: Int): Boolean {
        require(key != nulled) { "Key $key is reserved as empty-slot sentinel (nulled)" }
        if (hashNeedsGrow(size, deleted, keys.size)) rehash(keys.size shl 1)
        val i = findSlot(key)
        if (state[i] == HASH_FULL) return false
        if (state[i] == HASH_DELETED) deleted--
        keys[i] = key
        state[i] = HASH_FULL
        size++
        modCount++
        return true
    }

    operator fun plusAssign(key: Int) {
        add(key)
    }

    fun addAll(elements: IntArray): Boolean {
        var changed = false
        for (key in elements) if (add(key)) changed = true
        return changed
    }

    fun addAll(elements: IntSet): Boolean {
        var changed = false
        elements.forEach { if (add(it)) changed = true }
        return changed
    }

    fun addAll(elements: Collection<Int>): Boolean {
        var changed = false
        for (key in elements) if (add(key)) changed = true
        return changed
    }

    fun remove(key: Int): Boolean {
        if (key == nulled) return false
        val i = indexOf(key)
        if (i < 0) return false
        state[i] = HASH_DELETED
        keys[i] = nulled
        deleted++
        size--
        modCount++
        return true
    }

    fun clear() {
        state.fill(HASH_EMPTY)
        keys.fill(nulled)
        size = 0
        deleted = 0
        modCount++
    }

    fun ensureCapacity(minSize: Int) {
        val cap = hashCapacityFor(minSize)
        if (cap > keys.size) rehash(cap)
    }

    fun toArray(): IntArray {
        val out = IntArray(size)
        var w = 0
        val s = state
        val k = keys
        for (i in s.indices) if (s[i] == HASH_FULL) out[w++] = k[i]
        return out
    }

    fun iterator(): PrimitiveIntIterator = Itr()

    inline fun forEach(action: (Int) -> Unit) {
        val s = state
        val k = keys
        for (i in s.indices) if (s[i] == HASH_FULL) action(k[i])
    }

    fun clone(): IntSet = IntSet(this)

    fun asMutableSet(): MutableSet<Int> = GenericSet()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IntSet) return false
        if (size != other.size) return false
        forEach { if (!other.contains(it)) return false }
        return true
    }

    override fun hashCode(): Int {
        var h = 0
        forEach { h += it }
        return h
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append('[')
        var first = true
        forEach {
            if (!first) sb.append(", ")
            first = false
            sb.append(it)
        }
        sb.append(']')
        return sb.toString()
    }

    private fun indexOf(key: Int): Int {
        val mask = keys.size - 1
        var i = mixInt(key) and mask
        val s = state
        val k = keys
        var probes = 0
        while (probes <= mask) {
            when (s[i]) {
                HASH_EMPTY -> return -1
                HASH_FULL -> if (k[i] == key) return i
            }
            i = (i + 1) and mask
            probes++
        }
        return -1
    }

    private fun findSlot(key: Int): Int {
        val mask = keys.size - 1
        var i = mixInt(key) and mask
        val s = state
        val k = keys
        var firstDeleted = -1
        var probes = 0
        while (probes <= mask) {
            when (s[i]) {
                HASH_EMPTY -> return if (firstDeleted >= 0) firstDeleted else i
                HASH_FULL -> if (k[i] == key) return i
                HASH_DELETED -> if (firstDeleted < 0) firstDeleted = i
            }
            i = (i + 1) and mask
            probes++
        }
        if (firstDeleted >= 0) return firstDeleted
        rehash(keys.size shl 1)
        return findSlot(key)
    }

    private fun rehash(newCap: Int) {
        val oldKeys = keys
        val oldState = state
        keys = allocKeys(newCap)
        state = ByteArray(newCap)
        deleted = 0
        size = 0
        for (i in oldState.indices) {
            if (oldState[i] == HASH_FULL) add(oldKeys[i])
        }
    }

    private inner class Itr : PrimitiveIntIterator {
        private var index = 0
        private var last = -1
        private var remaining = size
        private var expectedModCount = modCount

        override fun hasNext(): Boolean = remaining > 0

        override fun next(): Int {
            if (expectedModCount != modCount) throw ConcurrentModificationException()
            if (remaining <= 0) throw NoSuchElementException()
            val s = state
            while (index < s.size && s[index] != HASH_FULL) index++
            last = index
            remaining--
            return keys[index++]
        }

        override fun remove() {
            check(last >= 0)
            if (expectedModCount != modCount) throw ConcurrentModificationException()
            state[last] = HASH_DELETED
            keys[last] = nulled
            deleted++
            size--
            last = -1
            modCount++
            expectedModCount = modCount
        }
    }

    private inner class GenericSet : MutableSet<Int> {
        override val size: Int get() = this@IntSet.size
        override fun isEmpty(): Boolean = this@IntSet.isEmpty()
        override fun contains(element: Int): Boolean = this@IntSet.contains(element)
        override fun containsAll(elements: Collection<Int>): Boolean {
            for (e in elements) if (!contains(e)) return false
            return true
        }
        override fun add(element: Int): Boolean = this@IntSet.add(element)
        override fun addAll(elements: Collection<Int>): Boolean = this@IntSet.addAll(elements)
        override fun clear() = this@IntSet.clear()
        override fun iterator(): MutableIterator<Int> = BoxedItr()
        override fun remove(element: Int): Boolean = this@IntSet.remove(element)
        override fun removeAll(elements: Collection<Int>): Boolean {
            var changed = false
            for (e in elements) if (remove(e)) changed = true
            return changed
        }
        override fun retainAll(elements: Collection<Int>): Boolean {
            var changed = false
            val it = this@IntSet.iterator()
            while (it.hasNext()) {
                if (it.next() !in elements) {
                    it.remove()
                    changed = true
                }
            }
            return changed
        }
    }

    private inner class BoxedItr : MutableIterator<Int> {
        private val delegate = Itr()
        override fun hasNext(): Boolean = delegate.hasNext()
        override fun next(): Int = delegate.next()
        override fun remove() = delegate.remove()
    }

    companion object {
        fun empty(): IntSet = IntSet(0)
    }
}

fun intSetOf(vararg values: Int): IntSet = IntSet(values)
