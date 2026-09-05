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

/**
 * IntArrayList vs boxing ArrayList<Int>.
 *
 * Run:
 *   ./gradlew benchmark
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.NANOSECONDS)
open class IntArrayListBenchmark {

    @Param("16", "1024", "100000")
    var size: Int = 0

    private lateinit var boxed: ArrayList<Int>
    private lateinit var primitive: IntArrayList
    private var sink = 0

    @Setup
    fun setup() {
        boxed = ArrayList(size)
        primitive = IntArrayList(size)
        for (i in 0 until size) {
            boxed.add(i)
            primitive.add(i)
        }
    }

    @Benchmark
    fun addBoxed(): Int {
        val list = ArrayList<Int>()
        var i = 0
        while (i < size) {
            list.add(i)
            i++
        }
        return list.size
    }

    @Benchmark
    fun addPrimitive(): Int {
        val list = IntArrayList()
        var i = 0
        while (i < size) {
            list.add(i)
            i++
        }
        return list.size
    }

    @Benchmark
    fun addPresizedBoxed(): Int {
        val list = ArrayList<Int>(size)
        var i = 0
        while (i < size) {
            list.add(i)
            i++
        }
        return list.size
    }

    @Benchmark
    fun addPresizedPrimitive(): Int {
        val list = IntArrayList(size)
        var i = 0
        while (i < size) {
            list.add(i)
            i++
        }
        return list.size
    }

    @Benchmark
    fun getBoxed(): Int {
        val list = boxed
        var sum = 0
        var i = 0
        val n = list.size
        while (i < n) {
            sum += list[i]
            i++
        }
        sink = sum
        return sum
    }

    @Benchmark
    fun getPrimitive(): Int {
        val list = primitive
        var sum = 0
        var i = 0
        val n = list.size
        while (i < n) {
            sum += list[i]
            i++
        }
        sink = sum
        return sum
    }

    @Benchmark
    fun forEachBoxed(): Int {
        var sum = 0
        boxed.forEach { sum += it }
        sink = sum
        return sum
    }

    @Benchmark
    fun forEachPrimitive(): Int {
        var sum = 0
        primitive.forEach { sum += it }
        sink = sum
        return sum
    }

    @Benchmark
    fun sumPrimitive(): Int {
        val s = primitive.sum()
        sink = s
        return s
    }

    @Benchmark
    fun containsBoxed(): Boolean {
        return boxed.contains(size - 1)
    }

    @Benchmark
    fun containsPrimitive(): Boolean {
        return primitive.contains(size - 1)
    }

    @Benchmark
    fun iteratorBoxed(): Int {
        var sum = 0
        val it = boxed.iterator()
        while (it.hasNext()) sum += it.next()
        sink = sum
        return sum
    }

    @Benchmark
    fun iteratorPrimitive(): Int {
        var sum = 0
        val it = primitive.iterator()
        while (it.hasNext()) sum += it.next()
        sink = sum
        return sum
    }
}
