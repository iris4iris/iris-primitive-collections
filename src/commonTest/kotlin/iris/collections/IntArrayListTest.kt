package iris.collections

import kotlin.test.*

class IntArrayListTest {

    @Test
    fun addGetSize() {
        val list = IntArrayList(0)
        assertEquals(0, list.size)
        list += 1
        list += 2
        list.add(3)
        assertEquals(3, list.size)
        assertEquals(1, list[0])
        assertEquals(3, list[2])
    }

    @Test
    fun constructorDoesNotInflateRequestedCapacity() {
        val list = IntArrayList(3)
        repeat(3) { list += it }
        // internal capacity is not observable; behaviour: 3 adds without crash
        assertEquals(3, list.size)
    }

    @Test
    fun fromArrayCopies() {
        val src = intArrayOf(1, 2, 3)
        val list = IntArrayList(src)
        src[0] = 99
        assertEquals(1, list[0])
    }

    @Test
    fun containsAllSemantics() {
        val list = intArrayListOf(1, 2, 3, 4)
        assertTrue(list.containsAll(intArrayListOf(2, 4)))
        assertFalse(list.containsAll(intArrayListOf(2, 9)))
        assertTrue(list.containsAny(intArrayListOf(9, 3)))
        assertFalse(list.containsAny(intArrayListOf(9, 8)))
    }

    @Test
    fun toArrayDestinationContract() {
        val list = intArrayListOf(1, 2, 3)
        val small = IntArray(1)
        val grown = list.toArray(small)
        assertEquals(3, grown.size)
        assertContentEquals(intArrayOf(1, 2, 3), grown)

        val big = IntArray(5) { -1 }
        val same = list.toArray(big)
        assertSame(big, same)
        assertEquals(1, same[0])
        assertEquals(3, same[2])
    }

    @Test
    fun insertAndRemoveAt() {
        val list = intArrayListOf(1, 3)
        list.add(1, 2)
        assertContentEquals(intArrayOf(1, 2, 3), list.toArray())
        assertEquals(2, list.removeAt(1))
        assertContentEquals(intArrayOf(1, 3), list.toArray())
    }

    @Test
    fun setReturnsOld() {
        val list = intArrayListOf(10, 20)
        assertEquals(20, list.set(1, 30))
        assertEquals(30, list[1])
    }

    @Test
    fun removeIfAndRemoveElement() {
        val list = intArrayListOf(1, 2, 3, 4, 5)
        assertTrue(list.removeIf { it % 2 == 0 })
        assertContentEquals(intArrayOf(1, 3, 5), list.toArray())
        assertTrue(list.removeElement(3))
        assertFalse(list.removeElement(2))
        assertContentEquals(intArrayOf(1, 5), list.toArray())
    }

    @Test
    fun retainAndRemoveAll() {
        val list = intArrayListOf(1, 2, 3, 4)
        assertTrue(list.retainAll(intArrayListOf(2, 4, 9)))
        assertContentEquals(intArrayOf(2, 4), list.toArray())
        assertTrue(list.removeAll(intArrayListOf(4)))
        assertContentEquals(intArrayOf(2), list.toArray())
    }

    @Test
    fun equalsAndHashCodeMatchListContract() {
        val a = intArrayListOf(1, 2, 3)
        val b = IntArrayList(intArrayOf(1, 2, 3))
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        var expected = 1
        for (e in listOf(1, 2, 3)) expected = 31 * expected + e
        assertEquals(expected, a.hashCode())
        assertNotEquals(a, intArrayListOf(1, 2))
    }

    @Test
    fun emptyFactoryIsIndependent() {
        val a = IntArrayList.empty()
        val b = IntArrayList.empty()
        a += 1
        assertTrue(b.isEmpty())
        assertEquals(1, a.size)
    }

    @Test
    fun sortBinarySearchSum() {
        val list = intArrayListOf(3, 1, 2)
        list.sort()
        assertContentEquals(intArrayOf(1, 2, 3), list.toArray())
        assertEquals(1, list.binarySearch(2))
        assertEquals(6, list.sum())
        assertEquals(1, list.minOrElse(0))
        assertEquals(3, list.maxOrElse(0))
        assertEquals(1, list.first())
        assertEquals(3, list.last())
        assertEquals(-1, IntArrayList().firstOrElse(-1))
        assertEquals(-1, IntArrayList().minOrElse(-1))
        list.sortDescending()
        assertContentEquals(intArrayOf(3, 2, 1), list.toArray())
    }

    @Test
    fun iteratorFailFastAndRemove() {
        val list = intArrayListOf(1, 2, 3)
        val it = list.iterator()
        assertEquals(1, it.next())
        it.remove()
        assertContentEquals(intArrayOf(2, 3), list.toArray())
        list.add(4)
        assertFailsWith<ConcurrentModificationException> { it.next() }
    }

    @Test
    fun genericAdapter() {
        val list = intArrayListOf(1, 2, 3)
        val boxed: MutableList<Int> = list.asMutableList()
        boxed.add(1, 9)
        assertEquals(9, list[1])
        boxed.removeAt(1)
        assertEquals(2, list[1])
        boxed.removeAll(listOf(3))
        assertContentEquals(intArrayOf(1, 2), list.toArray())
    }

    @Test
    fun inlineFilterMapAny() {
        val list = intArrayListOf(1, 2, 3, 4)
        assertEquals(intArrayListOf(2, 4), list.filter { it % 2 == 0 })
        assertEquals(listOf(2, 4, 6, 8), list.map { it * 2 })
        assertTrue(list.any { it == 3 })
        assertTrue(list.all { it > 0 })
    }

    @Test
    fun initConstructor() {
        val list = IntArrayList.init(4) { it * 10 }
        assertContentEquals(intArrayOf(0, 10, 20, 30), list.toArray())
    }

    @Test
    fun bounds() {
        val list = intArrayListOf(1)
        assertFailsWith<IndexOutOfBoundsException> { list[1] }
        assertFailsWith<IndexOutOfBoundsException> { list.add(2, 0) }
        assertFailsWith<IllegalArgumentException> { IntArrayList(-1) }
    }
}

class LongArrayListTest {
    @Test
    fun basic() {
        val list = longArrayListOf(1L, 2L, 3L)
        assertEquals(6L, list.sum())
        assertTrue(list.containsAll(longArrayListOf(1L, 3L)))
        assertFalse(list.containsAll(longArrayListOf(1L, 9L)))
    }
}

class FloatArrayListTest {
    @Test
    fun nanEqualsUsesBits() {
        val a = floatArrayListOf(Float.NaN)
        val b = floatArrayListOf(Float.NaN)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }
}
