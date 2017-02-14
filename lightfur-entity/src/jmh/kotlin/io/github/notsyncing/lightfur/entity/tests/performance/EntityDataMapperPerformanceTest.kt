package io.github.notsyncing.lightfur.entity.tests.performance

import io.github.notsyncing.lightfur.entity.EntityDataMapper
import io.github.notsyncing.lightfur.entity.EntityModel
import io.vertx.core.json.JsonArray
import io.vertx.ext.sql.ResultSet
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import org.spf4j.stackmonitor.JmhFlightRecorderProfiler
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit

open class EntityDataMapperPerformanceTest {
    enum class TestEnum {
        TypeA,
        TypeB
    }

    class TestObject : EntityModel(table = "") {
        var id: Int by field(column = "id")

        var name: String? by field(column = "username")

        var date: LocalDateTime? by field(column = "date")

        var type: TestEnum? by field(column = "type")

        var list: IntArray? by field(column = "list")

        var list2: IntArray? by field(column = "list2")

        var longNumber: BigDecimal? by field(column = "long_number")
    }

    @State(Scope.Benchmark)
    open class BenchmarkStates {
        var dataMapper = EntityDataMapper()
        val r = ResultSet()

        init {
            r.columnNames = Arrays.asList("id", "username", "date", "type", "list", "list2", "long_number")

            val arr = JsonArray()
            arr.add(1)
            arr.add("test")
            arr.add("2015-04-03T11:35:29.384")
            arr.add(TestEnum.TypeB.ordinal)
            arr.add("{1,2,3}")
            arr.add(JsonArray("[4,5,6]"))
            arr.add("92375947293472934923794729345345345433.2345345345")
            r.results = Arrays.asList(arr)
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val opts = OptionsBuilder().include(".*" + EntityDataMapperPerformanceTest::class.java.simpleName + ".*")
                    .addProfiler(JmhFlightRecorderProfiler::class.java)
                    .build()

            Runner(opts).run()
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.All)
    @Warmup(iterations = 10)
    @Measurement(iterations = 10)
    @Fork(1)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    fun entityDataMapperTest(states: BenchmarkStates) {
        val o = states.dataMapper.map(TestObject::class.java, states.r)
    }
}