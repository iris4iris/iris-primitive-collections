package iris.collections

class LongMap<V>(
    expectedSize: Int = 0,
    val nulled: Long = Long.MIN_VALUE,
) {
    @PublishedApi internal var keys = allocKeys(hashCapacityFor(expectedSize))
    @PublishedApi internal var values = arrayOfNulls<Any?>(keys.size)
    @PublishedApi internal var state = ByteArray(keys.size)
    private var deleted = 0
    var size: Int = 0
        private set
    private var modCount = 0

    constructor(source: LongMap<V>) : this(source.size, source.nulled) {
        source.forEach { k, v -> put(k, v) }
    }

    private fun allocKeys(n: Int): LongArray {
        val a = LongArray(n)
        if (nulled != 0L) a.fill(nulled)
        return a
    }

    fun isEmpty(): Boolean = size == 0
    fun isNotEmpty(): Boolean = size != 0

    fun containsKey(key: Long): Boolean = key != nulled && indexOf(key) >= 0

    operator fun get(key: Long): V? {
        val i = indexOf(key)
        if (i < 0) return null
        @Suppress("UNCHECKED_CAST")
        return values[i] as V?
    }

    fun getOrDefault(key: Long, default: V): V {
        if (!containsKey(key)) return default
        @Suppress("UNCHECKED_CAST")
        return get(key) as V
    }

    inline fun getOrPut(key: Long, defaultValue: () -> V): V {
        if (containsKey(key)) {
            @Suppress("UNCHECKED_CAST")
            return get(key) as V
        }
        val value = defaultValue()
        put(key, value)
        return value
    }

    operator fun set(key: Long, value: V) {
        put(key, value)
    }

    fun put(key: Long, value: V): V? {
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

    fun remove(key: Long): V? {
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

    fun keys(): LongArray {
        val out = LongArray(size)
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

    inline fun forEach(action: (key: Long, value: V) -> Unit) {
        val s = state
        val k = keys
        val v = values
        for (i in s.indices) if (s[i] == HASH_FULL) {
            @Suppress("UNCHECKED_CAST")
            action(k[i], v[i] as V)
        }
    }

    fun clone(): LongMap<V> = LongMap(this)

    fun asMutableMap(): MutableMap<Long, V> = GenericMap()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LongMap<*>) return false
        if (size != other.size) return false
        forEach { k, v ->
            if (!other.containsKey(k) || other[k] != v) return false
        }
        return true
    }

    override fun hashCode(): Int {
        var h = 0
        forEach { k, v -> h += k.hashCode() xor (v?.hashCode() ?: 0) }
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

    private fun indexOf(key: Long): Int {
        val mask = keys.size - 1
        var i = mixLong(key) and mask
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

    private fun findSlot(key: Long): Int {
        val mask = keys.size - 1
        var i = mixLong(key) and mask
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

    private inner class GenericMap : MutableMap<Long, V> {
        override val size: Int get() = this@LongMap.size
        override fun isEmpty(): Boolean = this@LongMap.isEmpty()
        override fun containsKey(key: Long): Boolean = this@LongMap.containsKey(key)
        override fun containsValue(value: V): Boolean {
            var found = false
            forEach { _, v -> if (!found && v == value) found = true }
            return found
        }
        override fun get(key: Long): V? = this@LongMap[key]
        override fun put(key: Long, value: V): V? = this@LongMap.put(key, value)
        override fun remove(key: Long): V? = this@LongMap.remove(key)
        override fun putAll(from: Map<out Long, V>) {
            for ((k, v) in from) this@LongMap.put(k, v)
        }
        override fun clear() = this@LongMap.clear()
        override val keys: MutableSet<Long> get() = this@LongMap.keys().toMutableSet()
        override val values: MutableCollection<V> get() = this@LongMap.values().toMutableList()
        override val entries: MutableSet<MutableMap.MutableEntry<Long, V>>
            get() {
                val set = LinkedHashSet<MutableMap.MutableEntry<Long, V>>()
                forEach { k, v -> set.add(Entry(k, v)) }
                return set
            }
    }

    private inner class Entry(override val key: Long, override var value: V) : MutableMap.MutableEntry<Long, V> {
        override fun setValue(newValue: V): V {
            val old = value
            value = newValue
            put(key, newValue)
            return old
        }
    }

    companion object {
        fun <V> empty(): LongMap<V> = LongMap(0)
    }
}
