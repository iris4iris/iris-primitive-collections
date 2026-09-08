package iris.collections

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimitiveExtTest {
    data class User(val id: Int, val name: String)

    private val users = listOf(User(1, "a"), User(2, "b"), User(2, "c"))

    @Test
    fun mapIntsPresizesAndTransforms() {
        val ids = users.mapInts { it.id }
        assertEquals(3, ids.size)
        assertEquals(1, ids[0])
        assertEquals(2, ids[2])
    }

    @Test
    fun associateIntsByKeepsLast() {
        val map = users.associateIntsBy { it.id }
        assertEquals(2, map.size)
        assertEquals("c", map[2]?.name)
    }

    @Test
    fun associateIntsSplitsKeyValue() {
        val map = users.associateInts({ it.id }, { it.name })
        assertEquals("a", map[1])
        assertEquals("c", map[2])
    }

    @Test
    fun toIntSetDropsDuplicates() {
        val set = users.toIntSet { it.id }
        assertEquals(2, set.size)
        assertTrue(1 in set)
        assertTrue(2 in set)
    }

    @Test
    fun iterableIntToSetAndList() {
        val src = listOf(3, 1, 3)
        assertEquals(3, src.toIntArrayList().size)
        assertEquals(2, src.toIntSet().size)
    }

    @Test
    fun groupIntsByBuckets() {
        val grouped = users.groupIntsBy { it.id }
        assertEquals(1, grouped[1]?.size)
        assertEquals(2, grouped[2]?.size)
    }

    @Test
    fun filterInts() {
        val even = listOf(1, 2, 3, 4).filterInts { it % 2 == 0 }
        assertEquals(2, even.size)
        assertEquals(2, even[0])
        assertEquals(4, even[1])
    }

    @Test
    fun arrayOverloads() {
        val arr = arrayOf(User(7, "x"), User(8, "y"))
        assertEquals(2, arr.mapLongs { it.id.toLong() }.size)
        assertEquals("y", arr.associateLongsBy { it.id.toLong() }[8L]?.name)
    }
}
