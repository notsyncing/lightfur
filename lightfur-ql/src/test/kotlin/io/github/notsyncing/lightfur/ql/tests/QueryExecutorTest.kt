package io.github.notsyncing.lightfur.ql.tests

import io.github.notsyncing.lightfur.entity.dsl.EntitySelectDSL
import io.github.notsyncing.lightfur.ql.QueryExecutor
import io.github.notsyncing.lightfur.ql.tests.toys.UserContactDetailsModel
import io.github.notsyncing.lightfur.ql.tests.toys.UserContactInfoModel
import io.github.notsyncing.lightfur.ql.tests.toys.UserModel
import io.vertx.core.json.JsonArray
import io.vertx.kotlin.ext.sql.ResultSet
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.CompletableFuture

class QueryExecutorTest {
    private val simpleQuery: String
    private val nestedQuery: String
    private val deepQuery: String

    init {
        simpleQuery = javaClass.getResourceAsStream("/testSimple.json").bufferedReader().use { it.readText() }
        nestedQuery = javaClass.getResourceAsStream("/testNested.json").bufferedReader().use { it.readText() }
        deepQuery = javaClass.getResourceAsStream("/testDeep.json").bufferedReader().use { it.readText() }
    }

    @Test
    fun testExecuteSimpleQuery() {
        val q = QueryExecutor()
        val m = UserModel()
        q.parser.modelMap[UserModel::class.java.name + "_user"] = m
        val h = m.hashCode()
        val modelPrefix = UserModel::class.java.simpleName + "_${h}"

        q.javaClass.getDeclaredField("_queryFunction").apply { this.isAccessible = true }.set(q) { _: EntitySelectDSL<*> ->
            val columnNames = listOf("${modelPrefix}_id",
                    "${modelPrefix}_username",
                    "${modelPrefix}_mobile",
                    "${modelPrefix}_lastLoginTime",
                    "${modelPrefix}_status")

            val data = mutableListOf<JsonArray>()
            data.add(JsonArray(listOf(4, "133test1", "12345", "2017-01-01", 2)))
            data.add(JsonArray(listOf(5, "133test2", "23456", "2017-01-02", 3)))
            data.add(JsonArray(listOf(6, "133test3", "34567", "2017-01-03", 5)))

            CompletableFuture.completedFuture(ResultSet(columnNames, null, null, data))
        }

        val data = q.execute(simpleQuery).get()
        val expected = """{"user":[{"lastLogin":"2017-01-01","mobile":"12345","id":4,"username":"133test1","status":2},{"lastLogin":"2017-01-02","mobile":"23456","id":5,"username":"133test2","status":3},{"lastLogin":"2017-01-03","mobile":"34567","id":6,"username":"133test3","status":5}]}"""

        assertEquals(expected, data.toJSONString())
    }

    @Test
    fun testExecuteNestedQuery() {
        val q = QueryExecutor()
        val m1 = UserModel()
        val m2 = UserContactInfoModel()
        q.parser.modelMap[UserModel::class.java.name + "_user"] = m1
        q.parser.modelMap[UserContactInfoModel::class.java.name + "_user.contacts"] = m2
        val h1 = m1.hashCode()
        val h2 = m2.hashCode()
        val model1Prefix = UserModel::class.java.simpleName + "_${h1}"
        val model2Prefix = UserContactInfoModel::class.java.simpleName + "_${h2}"

        q.javaClass.getDeclaredField("_queryFunction").apply { this.isAccessible = true }.set(q) { _: EntitySelectDSL<*> ->
            val columnNames = listOf("${model1Prefix}_id",
                    "${model1Prefix}_username",
                    "${model1Prefix}_mobile",
                    "${model1Prefix}_lastLoginTime",
                    "${model1Prefix}_status",
                    "${model2Prefix}_userId",
                    "${model2Prefix}_id",
                    "${model2Prefix}_mobile",
                    "${model2Prefix}_default")

            val data = mutableListOf<JsonArray>()
            data.add(JsonArray(listOf(4, "133test1", "12345", "2017-01-01", 2, 4, 1, "54321", true)))
            data.add(JsonArray(listOf(4, "133test1", "12345", "2017-01-01", 2, 4, 2, "65432", true)))
            data.add(JsonArray(listOf(5, "133test2", "23456", "2017-01-02", 3, 5, 3, "76543", true)))
            data.add(JsonArray(listOf(5, "133test2", "23456", "2017-01-02", 3, 5, 4, "87654", true)))
            data.add(JsonArray(listOf(5, "133test2", "23456", "2017-01-02", 3, 5, 5, "98765", true)))
            data.add(JsonArray(listOf(6, "133test3", "34567", "2017-01-03", 5, 6, 6, "09876", true)))

            CompletableFuture.completedFuture(ResultSet(columnNames, null, null, data))
        }

        val data = q.execute(nestedQuery).get()
        val expected = """{"user":[{"lastLogin":"2017-01-01","mobile":"12345","id":4,"username":"133test1","status":2,"contacts":[{"default":true,"mobile":"54321","id":1,"userId":4},{"default":true,"mobile":"65432","id":2,"userId":4}]},{"lastLogin":"2017-01-02","mobile":"23456","id":5,"username":"133test2","status":3,"contacts":[{"default":true,"mobile":"76543","id":3,"userId":5},{"default":true,"mobile":"87654","id":4,"userId":5},{"default":true,"mobile":"98765","id":5,"userId":5}]},{"lastLogin":"2017-01-03","mobile":"34567","id":6,"username":"133test3","status":5,"contacts":[{"default":true,"mobile":"09876","id":6,"userId":6}]}]}"""

        assertEquals(expected, data.toJSONString())
    }

    @Test
    fun testExecuteDeepQuery() {
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

        val data = q.execute(deepQuery).get()
        val expected = """{"user":[{"lastLogin":"2017-01-01","mobile":"12345","id":4,"username":"133test1","status":2,"contacts":[{"default":true,"mobile":"54321","id":1,"userId":4,"details":[{"contactInfoId":1,"comment":"testA","id":10},{"contactInfoId":1,"comment":"testB","id":11}]},{"default":true,"mobile":"65432","id":2,"userId":4,"details":[{"contactInfoId":2,"comment":"testC","id":12},{"contactInfoId":2,"comment":"testD","id":13},{"contactInfoId":2,"comment":"testE","id":14}]}]},{"lastLogin":"2017-01-02","mobile":"23456","id":5,"username":"133test2","status":3,"contacts":[{"default":true,"mobile":"76543","id":3,"userId":5,"details":[{"contactInfoId":3,"comment":"testF","id":15},{"contactInfoId":3,"comment":"testG","id":16},{"contactInfoId":3,"comment":"testH","id":17}]},{"default":true,"mobile":"87654","id":4,"userId":5,"details":[{"contactInfoId":4,"comment":"testI","id":18}]},{"default":true,"mobile":"98765","id":5,"userId":5,"details":[{"contactInfoId":5,"comment":"testJ","id":19}]}]},{"lastLogin":"2017-01-03","mobile":"34567","id":6,"username":"133test3","status":5,"contacts":[{"default":true,"mobile":"09876","id":6,"userId":6,"details":[{"contactInfoId":6,"comment":"testK","id":20},{"contactInfoId":6,"comment":"testL","id":21}]}]}]}"""

        assertEquals(expected, data.toJSONString())
    }
}