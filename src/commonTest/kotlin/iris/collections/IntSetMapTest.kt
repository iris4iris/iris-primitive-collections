package iris.collections

import kotlin.test.*

class IntSetTest {
    @Test
    fun addContainsRemove() {
        val set = IntSet()
        assertTrue(set.add(1))
        assertTrue(set.add(2))
        assertFalse(set.add(1))
        assertEquals(2, set.size)
        assertTrue(set.contains(1))
        assertTrue(set.remove(1))
        assertFalse(set.contains(1))
        assertEquals(1, set.size)
    }

    @Test
    fun growsAndKeepsUniqueness() {
        val set = IntSet()
        repeat(200) { set += it }
        repeat(200) { set += it }
        assertEquals(200, set.size)
        assertTrue(set.contains(0))
        assertTrue(set.contains(199))
        assertFalse(set.contains(200))
    }

    @Test
    fun equalsIgnoresOrder() {
        val a = intSetOf(1, 2, 3)
        val b = intSetOf(3, 1, 2)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun iteratorRemove() {
        val set = intSetOf(1, 2, 3)
        val it = set.iterator()
        while (it.hasNext()) {
            if (it.next() == 2) it.remove()
        }
        assertEquals(intSetOf(1, 3), set)
    }

    @Test
    fun nulledKeyRejected() {
        val set = IntSet(nulled = Int.MIN_VALUE)
        assertFailsWith<IllegalArgumentException> { set.add(Int.MIN_VALUE) }
        assertFalse(set.contains(Int.MIN_VALUE))
    }
}

class LongSetTest {
    @Test
    fun basic() {
        val set = longSetOf(1L, 2L, -1L, Long.MAX_VALUE)
        assertEquals(4, set.size)
        assertTrue(set.contains(Long.MAX_VALUE))
        assertTrue(set.remove(2L))
        assertFalse(set.contains(2L))
    }
}

class IntMapTest {
    @Test
    fun putGetRemove() {
        val map = IntMap<String>()
        assertNull(map.put(1, "a"))
        assertEquals("a", map[1])
        assertEquals("a", map.put(1, "b"))
        assertEquals("b", map[1])
        assertEquals("b", map.remove(1))
        assertNull(map[1])
        assertEquals(0, map.size)
    }

    @Test
    fun getOrPutAndDefault() {
        val map = IntMap<String>()
        assertEquals("x", map.getOrPut(5) { "x" })
        assertEquals("x", map.getOrPut(5) { "y" })
        assertEquals("z", map.getOrDefault(9, "z"))
    }

    @Test
    fun grows() {
        val map = IntMap<Int>()
        repeat(300) { map[it] = it * 10 }
        assertEquals(300, map.size)
        assertEquals(0, map[0])
        assertEquals(2990, map[299])
        assertNull(map[300])
    }

    @Test
    fun equals() {
        val a = IntMap<String>()
        val b = IntMap<String>()
        a[1] = "a"; a[2] = "b"
        b[2] = "b"; b[1] = "a"
        assertEquals(a, b)
    }

    @Test
    fun nulledKeyRejected() {
        val map = IntMap<String>(nulled = Int.MIN_VALUE)
        assertFailsWith<IllegalArgumentException> { map[Int.MIN_VALUE] = "min" }
        assertFalse(map.containsKey(Int.MIN_VALUE))
        assertNull(map[Int.MIN_VALUE])
    }
}

class LongMapTest {
    @Test
    fun basic() {
        val map = LongMap<String>()
        map[Long.MAX_VALUE] = "max"
        map[0L] = "zero"
        assertEquals("max", map[Long.MAX_VALUE])
        assertEquals("zero", map.remove(0L))
        assertEquals(1, map.size)
    }
}
