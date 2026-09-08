package iris.collections

class IntMap<V>(
    expectedSize: Int = 0,
    val nulled: Int = Int.MIN_VALUE,
) {
    @PublishedApi internal var keys = allocKeys(hashCapacityFor(expectedSize))
    @PublishedApi internal var values = arrayOfNulls<Any?>(keys.size)
    @PublishedApi internal var state = ByteArray(keys.size)
    private var deleted = 0
    var size: Int = 0
        private set
    private var modCount = 0

    constructor(source: IntMap<V>) : this(source.size, source.nulled) {
        source.forEach { k, v -> put(k, v) }
    }

    private fun allocKeys(n: Int): IntArray {
        val a = IntArray(n)
        if (nulled != 0) a.fill(nulled)
        return a
    }

    fun isEmpty(): Boolean = size == 0
    fun isNotEmpty(): Boolean = size != 0

    fun containsKey(key: Int): Boolean = key != nulled && indexOf(key) >= 0

    operator fun get(key: Int): V? {
        val i = indexOf(key)
        if (i < 0) return null
        @Suppress("UNCHECKED_CAST")
        return values[i] as V?
    }

    fun getOrDefault(key: Int, default: V): V {
        if (!containsKey(key)) return default
        @Suppress("UNCHECKED_CAST")
        return get(key) as V
    }

    inline fun getOrPut(key: Int, defaultValue: () -> V): V {
        if (containsKey(key)) {
            @Suppress("UNCHECKED_CAST")
            return get(key) as V
        }
        val value = defaultValue()
        put(key, value)
        return value
    }

    operator fun set(key: Int, value: V) {
        put(key, value)
    }

    fun put(key: Int, value: V): V? {
        require(key != nulled) { "Key $key is reserved as empty-slot sentinel (nulled)" }
        if (hashNeedsGrow(size, deleted, keys.size)) rehash(keys.size shl 1)
        val i = findSlot(key)
        if (state[i] == HASH_FULL) {
            @Suppress("UNCHECKED_CAST")
            val old = values[i] as V?
            values[i] = value
            return old
        }
        if (state[i] == HASH_DELETED) deleted--
        keys[i] = key
        values[i] = value
        state[i] = HASH_FULL
        size++
        modCount++
        return null
    }

    fun remove(key: Int): V? {
        if (key == nulled) return null
        val i = indexOf(key)
        if (i < 0) return null
        @Suppress("UNCHECKED_CAST")
        val old = values[i] as V?
        state[i] = HASH_DELETED
        keys[i] = nulled
        values[i] = null
        deleted++
        size--
        modCount++
        return old
    }

    fun clear() {
        state.fill(HASH_EMPTY)
        keys.fill(nulled)
        values.fill(null)
        size = 0
        deleted = 0
        modCount++
    }

    fun ensureCapacity(minSize: Int) {
        val cap = hashCapacityFor(minSize)
        if (cap > keys.size) rehash(cap)
    }

    fun keys(): IntArray {
        val out = IntArray(size)
        var w = 0
        val s = state
        val k = keys
        for (i in s.indices) if (s[i] == HASH_FULL) out[w++] = k[i]
        return out
    }

    fun values(): List<V> {
        val out = ArrayList<V>(size)
        val s = state
        val v = values
        for (i in s.indices) if (s[i] == HASH_FULL) {
            @Suppress("UNCHECKED_CAST")
            out.add(v[i] as V)
        }
        return out
    }

    inline fun forEach(action: (key: Int, value: V) -> Unit) {
        val s = state
        val k = keys
        val v = values
        for (i in s.indices) if (s[i] == HASH_FULL) {
            @Suppress("UNCHECKED_CAST")
            action(k[i], v[i] as V)
        }
    }

    fun clone(): IntMap<V> = IntMap(this)

    fun asMutableMap(): MutableMap<Int, V> = GenericMap()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IntMap<*>) return false
        if (size != other.size) return false
        forEach { k, v ->
            if (!other.containsKey(k) || other[k] != v) return false
        }
        return true
    }

    override fun hashCode(): Int {
        var h = 0
        forEach { k, v -> h += k xor (v?.hashCode() ?: 0) }
        return h
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append('{')
        var first = true
        forEach { k, v ->
            if (!first) sb.append(", ")
            first = false
            sb.append(k).append('=').append(v)
        }
        sb.append('}')
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
        val oldValues = values
        val oldState = state
        keys = allocKeys(newCap)
        values = arrayOfNulls(newCap)
        state = ByteArray(newCap)
        deleted = 0
        size = 0
        for (i in oldState.indices) {
            if (oldState[i] == HASH_FULL) {
                @Suppress("UNCHECKED_CAST")
                put(oldKeys[i], oldValues[i] as V)
            }
        }
    }

    private inner class GenericMap : MutableMap<Int, V> {
        override val size: Int get() = this@IntMap.size
        override fun isEmpty(): Boolean = this@IntMap.isEmpty()
        override fun containsKey(key: Int): Boolean = this@IntMap.containsKey(key)
        override fun containsValue(value: V): Boolean {
            var found = false
            forEach { _, v -> if (!found && v == value) found = true }
            return found
        }
        override fun get(key: Int): V? = this@IntMap[key]
        override fun put(key: Int, value: V): V? = this@IntMap.put(key, value)
        override fun remove(key: Int): V? = this@IntMap.remove(key)
        override fun putAll(from: Map<out Int, V>) {
            for ((k, v) in from) this@IntMap.put(k, v)
        }
        override fun clear() = this@IntMap.clear()
        override val keys: MutableSet<Int> get() = this@IntMap.keys().toMutableSet()
        override val values: MutableCollection<V> get() = this@IntMap.values().toMutableList()
        override val entries: MutableSet<MutableMap.MutableEntry<Int, V>>
            get() {
                val set = LinkedHashSet<MutableMap.MutableEntry<Int, V>>()
                forEach { k, v -> set.add(Entry(k, v)) }
                return set
            }
    }

    private inner class Entry(override val key: Int, override var value: V) : MutableMap.MutableEntry<Int, V> {
        override fun setValue(newValue: V): V {
            val old = value
            value = newValue
            put(key, newValue)
            return old
        }
    }

    companion object {
        fun <V> empty(): IntMap<V> = IntMap(0)
    }
}
