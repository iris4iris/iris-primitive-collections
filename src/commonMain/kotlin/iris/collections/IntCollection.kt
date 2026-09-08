package iris.collections

interface IntCollection : PrimitiveCollection {
    operator fun contains(element: Int): Boolean
    fun containsAll(elements: IntCollection): Boolean
    fun containsAll(elements: Collection<Int>): Boolean
    fun containsAny(elements: IntCollection): Boolean
    fun containsAny(elements: Collection<Int>): Boolean
    fun iterator(): PrimitiveIntIterator
    fun toArray(): IntArray
    fun toArray(destination: IntArray): IntArray
    fun add(element: Int): Boolean
    operator fun plusAssign(element: Int) { add(element) }
    fun addAll(elements: IntCollection): Boolean
    fun addAll(elements: IntArray): Boolean
    fun addAll(elements: Collection<Int>): Boolean
    fun removeElement(element: Int): Boolean
    fun removeAll(elements: IntCollection): Boolean
    fun removeAll(elements: Collection<Int>): Boolean
    fun retainAll(elements: IntCollection): Boolean
    fun retainAll(elements: Collection<Int>): Boolean
    fun removeIf(predicate: (Int) -> Boolean): Boolean
    fun clone(): IntCollection
    fun asMutableCollection(): MutableCollection<Int>
    fun joinToString(
        separator: CharSequence = ", ",
        prefix: CharSequence = "",
        postfix: CharSequence = "",
        limit: Int = -1,
        truncated: CharSequence = "...",
        transform: ((Int) -> CharSequence)? = null,
    ): String

    fun sum(): Int {
        var s = 0
        val it = iterator()
        while (it.hasNext()) s += it.next()
        return s
    }

    fun average(): Double {
        if (isEmpty()) return Double.NaN
        return sum().toDouble() / size
    }

    fun first(): Int {
        val it = iterator()
        if (!it.hasNext()) throw NoSuchElementException("Empty IntCollection")
        return it.next()
    }

    fun last(): Int {
        val it = iterator()
        if (!it.hasNext()) throw NoSuchElementException("Empty IntCollection")
        var v = it.next()
        while (it.hasNext()) v = it.next()
        return v
    }

    fun firstOrElse(default: Int): Int {
        val it = iterator()
        return if (it.hasNext()) it.next() else default
    }

    fun lastOrElse(default: Int): Long {
        val it = iterator()
        if (!it.hasNext()) return default
        var v = it.next()
        while (it.hasNext()) v = it.next()
        return v
    }

    fun minOrElse(default: Int): Int {
        val it = iterator()
        if (!it.hasNext()) return default
        var m = it.next()
        while (it.hasNext()) {
            val v = it.next()
            if (v < m) m = v
        }
        return m
    }

    fun maxOrElse(default: Int): Int {
        val it = iterator()
        if (!it.hasNext()) return default
        var m = it.next()
        while (it.hasNext()) {
            val v = it.next()
            if (v > m) m = v
        }
        return m
    }
}
