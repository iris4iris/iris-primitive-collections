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
open class IntSetBenchmark {

    @Param("16", "1024", "100000")
    var size: Int = 0

    private lateinit var boxed: HashSet<Int>
    private lateinit var primitive: IntSet
    private var lastKey = 0
    private var sink = 0

    @Setup
    fun setup() {
        boxed = HashSet(size)
        primitive = IntSet(size)
        var i = 0
        while (i < size) {
            boxed.add(i + 128)
            primitive.add(i + 128)
            i++
        }
        lastKey = size - 1 + 128
    }

    @Benchmark
    fun addBoxed(): Int {
        val set = HashSet<Int>()
        var i = 0
        while (i < size) {
            set.add(i + 128)
            i++
        }
        return set.size
    }

    @Benchmark
    fun addPrimitive(): Int {
        val set = IntSet()
        var i = 0
        while (i < size) {
            set.add(i + 128)
            i++
        }
        return set.size
    }

    @Benchmark
    fun addPresizedBoxed(): Int {
        val set = HashSet<Int>(size)
        var i = 0
        while (i < size) {
            set.add(i + 128)
            i++
        }
        return set.size
    }

    @Benchmark
    fun addPresizedPrimitive(): Int {
        val set = IntSet(size)
        var i = 0
        while (i < size) {
            set.add(i + 128)
            i++
        }
        return set.size
    }

    @Benchmark
    fun containsBoxed(): Boolean = boxed.contains(lastKey)

    @Benchmark
    fun containsPrimitive(): Boolean = primitive.contains(lastKey)

    @Benchmark
    fun forEachBoxed(): Int {
        var n = 0
        boxed.forEach { n++ }
        sink = n
        return n
    }

    @Benchmark
    fun forEachPrimitive(): Int {
        var n = 0
        primitive.forEach { n++ }
        sink = n
        return n
    }
}
