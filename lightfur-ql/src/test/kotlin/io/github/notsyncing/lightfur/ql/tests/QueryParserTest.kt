package io.github.notsyncing.lightfur.ql.tests

import io.github.notsyncing.lightfur.ql.QueryParser
import io.github.notsyncing.lightfur.ql.tests.toys.UserContactDetailsModel
import io.github.notsyncing.lightfur.ql.tests.toys.UserContactInfoModel
import io.github.notsyncing.lightfur.ql.tests.toys.UserModel
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class QueryParserTest {
    private val simpleQuery: String
    private val nestedQuery: String
    private val deepQuery: String

    init {
        simpleQuery = javaClass.getResourceAsStream("/testSimple.json").bufferedReader().use { it.readText() }
        nestedQuery = javaClass.getResourceAsStream("/testNested.json").bufferedReader().use { it.readText() }
        deepQuery = javaClass.getResourceAsStream("/testDeep.json").bufferedReader().use { it.readText() }
    }

    @Test
    fun testParseSimpleQuery() {
        val parser = QueryParser()
        val queries = parser.parse(simpleQuery)
        assertEquals(1, queries.size)

        val parsedQuery = queries.values.first()
        val s = parsedQuery.toSQL()
        val p = parsedQuery.toSQLParameters()
        val m = parser.modelMap.values.first()

        val h = m.hashCode()
        val expected = """SELECT "UserModel_${h}"."last_login_time" AS "UserModel_${h}_lastLoginTime", "UserModel_${h}"."mobile" AS "UserModel_${h}_mobile", "UserModel_${h}"."id" AS "UserModel_${h}_id", "UserModel_${h}"."username" AS "UserModel_${h}_username", "UserModel_${h}"."status" AS "UserModel_${h}_status"
WHERE ("UserModel_${h}"."id" > ? AND "UserModel_${h}"."username" LIKE ? AND "UserModel_${h}"."status" <> ? AND "UserModel_${h}"."status" = ANY(?) OR "UserModel_${h}"."status" = ?)
ORDER BY "UserModel_${h}"."id", "UserModel_${h}"."username" DESC
LIMIT ?
OFFSET ?"""

        assertEquals(expected, s)
        assertArrayEquals(arrayOf(2, "133", 4, arrayOf(2, 3, 5), 10, 10, 0), p.toTypedArray())
    }

    @Test
    fun testParseNestedQuery() {
        val parser = QueryParser()
        val queries = parser.parse(nestedQuery)
        assertEquals(1, queries.size)

        val parsedQuery = queries.values.first()
        val s = parsedQuery.toSQL()
        val p = parsedQuery.toSQLParameters()
        val m1 = parser.modelMap[UserModel::class.java.name + "_user"]!!
        val m2 = parser.modelMap[UserContactInfoModel::class.java.name + "_user.contacts"]!!

        val h1 = m1.hashCode()
        val h2 = m2.hashCode()

        val expected = """SELECT "UserModel_${h1}"."last_login_time" AS "UserModel_${h1}_lastLoginTime", "UserModel_${h1}"."mobile" AS "UserModel_${h1}_mobile", "UserModel_${h1}"."id" AS "UserModel_${h1}_id", "UserModel_${h1}"."username" AS "UserModel_${h1}_username", "UserModel_${h1}"."status" AS "UserModel_${h1}_status", "UserContactInfoModel_${h2}"."default" AS "UserContactInfoModel_${h2}_default", "UserContactInfoModel_${h2}"."mobile" AS "UserContactInfoModel_${h2}_mobile", "UserContactInfoModel_${h2}"."id" AS "UserContactInfoModel_${h2}_id", "UserContactInfoModel_${h2}"."user_id" AS "UserContactInfoModel_${h2}_userId"
INNER JOIN ("user_contact_infos" AS "UserContactInfoModel_${h2}") ON ("UserModel_${h1}"."id" = "UserContactInfoModel_${h2}"."user_id")
WHERE ("UserModel_${h1}"."id" > ? AND "UserModel_${h1}"."username" LIKE ? AND "UserModel_${h1}"."status" <> ? AND "UserContactInfoModel_${h2}"."default" = ? AND "UserModel_${h1}"."status" = ANY(?) OR "UserModel_${h1}"."status" = ?)
ORDER BY "UserModel_${h1}"."id", "UserModel_${h1}"."username" DESC
LIMIT ?
OFFSET ?"""

        assertEquals(expected, s)
        assertArrayEquals(arrayOf(2, "133", 4, true, arrayOf(2, 3, 5), 10, 10, 0), p.toTypedArray())
    }

    @Test
    fun testParseDeepQuery() {
        val parser = QueryParser()
        val queries = parser.parse(deepQuery)
        assertEquals(1, queries.size)

        val parsedQuery = queries.values.first()
        val s = parsedQuery.toSQL()
        val p = parsedQuery.toSQLParameters()
        val m1 = parser.modelMap[UserModel::class.java.name + "_user"]!!
        val m2 = parser.modelMap[UserContactInfoModel::class.java.name + "_user.contacts"]!!
        val m3 = parser.modelMap[UserContactDetailsModel::class.java.name + "_user.contacts.details"]!!

        val h1 = m1.hashCode()
        val h2 = m2.hashCode()
        val h3 = m3.hashCode()

        val expected = """SELECT "UserModel_${h1}"."last_login_time" AS "UserModel_${h1}_lastLoginTime", "UserModel_${h1}"."mobile" AS "UserModel_${h1}_mobile", "UserModel_${h1}"."id" AS "UserModel_${h1}_id", "UserModel_${h1}"."username" AS "UserModel_${h1}_username", "UserModel_${h1}"."status" AS "UserModel_${h1}_status", "UserContactInfoModel_${h2}"."default" AS "UserContactInfoModel_${h2}_default", "UserContactInfoModel_${h2}"."mobile" AS "UserContactInfoModel_${h2}_mobile", "UserContactInfoModel_${h2}"."id" AS "UserContactInfoModel_${h2}_id", "UserContactInfoModel_${h2}"."user_id" AS "UserContactInfoModel_${h2}_userId", "UserContactDetailsModel_${h3}"."contact_info_id" AS "UserContactDetailsModel_${h3}_contactInfoId", "UserContactDetailsModel_${h3}"."comment" AS "UserContactDetailsModel_${h3}_comment", "UserContactDetailsModel_${h3}"."id" AS "UserContactDetailsModel_${h3}_id"
INNER JOIN ("user_contact_infos" AS "UserContactInfoModel_${h2}") ON ("UserModel_${h1}"."id" = "UserContactInfoModel_${h2}"."user_id")
INNER JOIN ("user_contact_info_details" AS "UserContactDetailsModel_${h3}") ON ("UserContactInfoModel_${h2}"."id" = "UserContactDetailsModel_${h3}"."contact_info_id")
WHERE ("UserModel_${h1}"."id" > ? AND "UserModel_${h1}"."username" LIKE ? AND "UserModel_${h1}"."status" <> ? AND "UserContactInfoModel_${h2}"."default" = ? AND "UserModel_${h1}"."status" = ANY(?) OR "UserModel_${h1}"."status" = ? OR "UserContactDetailsModel_${h3}"."comment" IS  NOT NULL)
ORDER BY "UserModel_${h1}"."id", "UserModel_${h1}"."username" DESC
LIMIT ?
OFFSET ?"""

        assertEquals(expected, s)
        assertArrayEquals(arrayOf(2, "133", 4, true, arrayOf(2, 3, 5), 10, 10, 0), p.toTypedArray())
    }
}