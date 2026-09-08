package iris.collections

import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.BenchmarkMode
import kotlinx.benchmark.BenchmarkTimeUnit
import kotlinx.benchmark.Mode
import kotlinx.benchmark.OutputTimeUnit
import kotlinx.benchmark.Param
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.NANOSECONDS)
open class LongMapBenchmark {

    @Param("16", "1024", "100000")
    var size: Int = 0

    private lateinit var boxed: HashMap<Long, String>
    private lateinit var primitive: LongMap<String>
    private var lastKey = 0L
    private var sink = 0

    @Setup
    fun setup() {
        boxed = HashMap(size)
        primitive = LongMap(size)
        var i = 0
        while (i < size) {
            val k = i.toLong()
            val v = i.toString()
            boxed[k] = v
            primitive[k] = v
            i++
        }
        lastKey = (size - 1).toLong()
    }

    @Benchmark
    fun putBoxed(): Int {
        val map = HashMap<Long, String>()
        var i = 0
        while (i < size) {
            map[i.toLong()] = "v"
            i++
        }
        return map.size
    }

    @Benchmark
    fun putPrimitive(): Int {
        val map = LongMap<String>()
        var i = 0
        while (i < size) {
            map[i.toLong()] = "v"
            i++
        }
        return map.size
    }

    @Benchmark
    fun putPresizedBoxed(): Int {
        val map = HashMap<Long, String>(size)
        var i = 0
        while (i < size) {
            map[i.toLong()] = "v"
            i++
        }
        return map.size
    }

    @Benchmark
    fun putPresizedPrimitive(): Int {
        val map = LongMap<String>(size)
        var i = 0
        while (i < size) {
            map[i.toLong()] = "v"
            i++
        }
        return map.size
    }

    @Benchmark
    fun getBoxed(): String? = boxed[lastKey]

    @Benchmark
    fun getPrimitive(): String? = primitive[lastKey]

    @Benchmark
    fun containsBoxed(): Boolean = boxed.containsKey(lastKey)

    @Benchmark
    fun containsPrimitive(): Boolean = primitive.containsKey(lastKey)

    @Benchmark
    fun forEachBoxed(): Int {
        var n = 0
        boxed.forEach { _, _ -> n++ }
        sink = n
        return n
    }

    @Benchmark
    fun forEachPrimitive(): Int {
        var n = 0
        primitive.forEach { _, _ -> n++ }
        sink = n
        return n
    }
}
