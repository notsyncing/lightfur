package io.github.notsyncing.lightfur.ql.tests.performance

import io.github.notsyncing.lightfur.ql.QueryParser
import org.junit.Assert.assertEquals
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import java.util.concurrent.TimeUnit

open class QueryParserPerformanceTest {
    @State(Scope.Benchmark)
    open class BenchmarkStates {
        val deepQuery = javaClass.getResourceAsStream("/testDeep.json").bufferedReader().use { it.readText() }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val opts = OptionsBuilder().include(".*" + QueryParserPerformanceTest::class.java.simpleName + ".*")
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
    fun queryParserPerformanceTest(states: BenchmarkStates) {
        val parser = QueryParser()
        val parsedQueries = parser.parse(states.deepQuery)

        assertEquals(1, parsedQueries.size)
    }
}