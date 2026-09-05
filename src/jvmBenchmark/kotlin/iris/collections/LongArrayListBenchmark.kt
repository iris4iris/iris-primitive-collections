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
 * LongArrayList vs boxing ArrayList<Long>.
 *
 * ./gradlew jvmBenchmarkShort30Benchmark
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.NANOSECONDS)
open class LongArrayListBenchmark {

    @Param("16", "1024", "100000")
    var size: Int = 0

    private lateinit var boxed: ArrayList<Long>
    private lateinit var primitive: LongArrayList
    private var sink = 0L

    @Setup
    fun setup() {
        boxed = ArrayList(size)
        primitive = LongArrayList(size)
        var i = 0
        while (i < size) {
            val v = i.toLong()
            boxed.add(v)
            primitive.add(v)
            i++
        }
    }

    @Benchmark
    fun addBoxed(): Int {
        val list = ArrayList<Long>()
        var i = 0
        while (i < size) {
            list.add(i.toLong())
            i++
        }
        return list.size
    }

    @Benchmark
    fun addPrimitive(): Int {
        val list = LongArrayList()
        var i = 0
        while (i < size) {
            list.add(i.toLong())
            i++
        }
        return list.size
    }

    @Benchmark
    fun addPresizedBoxed(): Int {
        val list = ArrayList<Long>(size)
        var i = 0
        while (i < size) {
            list.add(i.toLong())
            i++
        }
        return list.size
    }

    @Benchmark
    fun addPresizedPrimitive(): Int {
        val list = LongArrayList(size)
        var i = 0
        while (i < size) {
            list.add(i.toLong())
            i++
        }
        return list.size
    }

    @Benchmark
    fun getBoxed(): Long {
        val list = boxed
        var sum = 0L
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
    fun getPrimitive(): Long {
        val list = primitive
        var sum = 0L
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
    fun forEachBoxed(): Long {
        var sum = 0L
        boxed.forEach { sum += it }
        sink = sum
        return sum
    }

    @Benchmark
    fun forEachPrimitive(): Long {
        var sum = 0L
        primitive.forEach { sum += it }
        sink = sum
        return sum
    }

    @Benchmark
    fun sumPrimitive(): Long {
        val s = primitive.sum()
        sink = s
        return s
    }

    @Benchmark
    fun containsBoxed(): Boolean {
        return boxed.contains((size - 1).toLong())
    }

    @Benchmark
    fun containsPrimitive(): Boolean {
        return primitive.contains((size - 1).toLong())
    }

    @Benchmark
    fun iteratorBoxed(): Long {
        var sum = 0L
        val it = boxed.iterator()
        while (it.hasNext()) sum += it.next()
        sink = sum
        return sum
    }

    @Benchmark
    fun iteratorPrimitive(): Long {
        var sum = 0L
        val it = primitive.iterator()
        while (it.hasNext()) sum += it.next()
        sink = sum
        return sum
    }
}
