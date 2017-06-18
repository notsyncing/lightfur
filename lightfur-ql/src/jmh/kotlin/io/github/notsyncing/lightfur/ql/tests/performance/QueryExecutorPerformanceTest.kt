package io.github.notsyncing.lightfur.ql.tests.performance

import io.github.notsyncing.lightfur.entity.dsl.EntitySelectDSL
import io.github.notsyncing.lightfur.ql.QueryExecutor
import io.github.notsyncing.lightfur.ql.tests.toys.UserContactDetailsModel
import io.github.notsyncing.lightfur.ql.tests.toys.UserContactInfoModel
import io.github.notsyncing.lightfur.ql.tests.toys.UserModel
import io.vertx.core.json.JsonArray
import io.vertx.kotlin.ext.sql.ResultSet
import org.junit.Assert.assertNotNull
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

open class QueryExecutorPerformanceTest {
    @State(Scope.Benchmark)
    open class BenchmarkStates {
        val deepQuery = javaClass.getResourceAsStream("/testDeep.json").bufferedReader().use { it.readText() }
        val executor: QueryExecutor

        init {
            val q = QueryExecutor()
            val m1 = UserModel()
            val m2 = UserContactInfoModel()
            val m3 = UserContactDetailsModel()
            q.parser.modelMap[UserModel::class.java.name + "_user"] = m1
            q.parser.modelMap[UserContactInfoModel::class.java.name + "_user.contacts"] = m2
            q.parser.modelMap[UserContactDetailsModel::class.java.name + "_user.contacts.details"] = m3
            val h1 = m1.hashCode()
            val h2 = m2.hashCode()
            val h3 = m3.hashCode()
            val model1Prefix = UserModel::class.java.simpleName + "_${h1}"
            val model2Prefix = UserContactInfoModel::class.java.simpleName + "_${h2}"
            val model3Prefix = UserContactDetailsModel::class.java.simpleName + "_${h3}"

            q.javaClass.getDeclaredField("_queryFunction").apply { this.isAccessible = true }.set(q) { _: EntitySelectDSL<*> ->
                val columnNames = listOf("${model1Prefix}_id",
                        "${model1Prefix}_username",
                        "${model1Prefix}_mobile",
                        "${model1Prefix}_lastLoginTime",
                        "${model1Prefix}_status",
                        "${model2Prefix}_userId",
                        "${model2Prefix}_id",
                        "${model2Prefix}_mobile",
                        "${model2Prefix}_default",
                        "${model3Prefix}_id",
                        "${model3Prefix}_contactInfoId",
                        "${model3Prefix}_comment")

                val data = mutableListOf<JsonArray>()
                data.add(JsonArray(listOf(4, "133test1", "12345", "2017-01-01", 2, 4, 1, "54321", true, 10, 1, "testA")))
                data.add(JsonArray(listOf(4, "133test1", "12345", "2017-01-01", 2, 4, 1, "54321", true, 11, 1, "testB")))
                data.add(JsonArray(listOf(4, "133test1", "12345", "2017-01-01", 2, 4, 2, "65432", true, 12, 2, "testC")))
                data.add(JsonArray(listOf(4, "133test1", "12345", "2017-01-01", 2, 4, 2, "65432", true, 13, 2, "testD")))
                data.add(JsonArray(listOf(4, "133test1", "12345", "2017-01-01", 2, 4, 2, "65432", true, 14, 2, "testE")))
                data.add(JsonArray(listOf(5, "133test2", "23456", "2017-01-02", 3, 5, 3, "76543", true, 15, 3, "testF")))
                data.add(JsonArray(listOf(5, "133test2", "23456", "2017-01-02", 3, 5, 3, "76543", true, 16, 3, "testG")))
                data.add(JsonArray(listOf(5, "133test2", "23456", "2017-01-02", 3, 5, 3, "76543", true, 17, 3, "testH")))
                data.add(JsonArray(listOf(5, "133test2", "23456", "2017-01-02", 3, 5, 4, "87654", true, 18, 4, "testI")))
                data.add(JsonArray(listOf(5, "133test2", "23456", "2017-01-02", 3, 5, 5, "98765", true, 19, 5, "testJ")))
                data.add(JsonArray(listOf(6, "133test3", "34567", "2017-01-03", 5, 6, 6, "09876", true, 20, 6, "testK")))
                data.add(JsonArray(listOf(6, "133test3", "34567", "2017-01-03", 5, 6, 6, "09876", true, 21, 6, "testL")))

                CompletableFuture.completedFuture(ResultSet(columnNames, null, null, data))
            }

            executor = q
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val opts = OptionsBuilder().include(".*" + QueryExecutorPerformanceTest::class.java.simpleName + ".*")
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
    fun queryExecutorPerformanceTest(states: BenchmarkStates) {
        val data = states.executor.execute(states.deepQuery).get()
        assertNotNull(data)
    }
}