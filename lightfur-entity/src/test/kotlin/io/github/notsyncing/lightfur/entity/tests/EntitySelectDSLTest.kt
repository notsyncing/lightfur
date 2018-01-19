package io.github.notsyncing.lightfur.entity.tests

import io.github.notsyncing.lightfur.entity.*
import io.github.notsyncing.lightfur.entity.dsl.EntitySelectDSL
import io.github.notsyncing.lightfur.entity.tests.toys.TestModel
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class EntitySelectDSLTest {
    @Before
    fun setUp() {
        EntityGlobal.reset()
    }

    @Test
    fun testSimpleSelect() {
        val m = TestModel()
        val q = EntitySelectDSL(m)
                .from()
        val s = q.toSQL()
        val p = q.toSQLParameters()

        val h = m.hashCode()
        val expected = """SELECT "TestModel_$h"."flag", "TestModel_$h"."id", "TestModel_$h"."name"
FROM "test_table" AS "TestModel_$h""""

        Assert.assertEquals(expected, s)
    }

    @Test
    fun testSelectWithMap() {
        val ms = TestModel()
        val mt = TestModel()
        val q = EntitySelectDSL(mt)
                .from(ms)
                .map(ms.F(ms::name) + "_test", mt::name)
                .map(ms.F(ms::id) - 2, mt::id)
                .map(ms.F(ms::flag), mt::flag)
        val s = q.toSQL()
        val p = q.toSQLParameters()

        val hs = ms.hashCode()
        val ht = mt.hashCode()
        val expected = """SELECT (("TestModel_$hs"."name" + ?)) AS "name", (("TestModel_$hs"."id" - ?)) AS "id", ("TestModel_$hs"."flag") AS "flag"
FROM "test_table" AS "TestModel_$hs""""

        Assert.assertEquals(expected, s)
        Assert.assertArrayEquals(arrayOf("_test", 2), p?.toTypedArray())
    }

    @Test
    fun testSelectWithSimpleConditions() {
        val m = TestModel()
        val q = EntitySelectDSL(m)
                .from()
                .where { m.F(m::id) gt 4 }
        val s = q.toSQL()
        val p = q.toSQLParameters()

        val h = m.hashCode()
        val expected = """SELECT "TestModel_$h"."flag", "TestModel_$h"."id", "TestModel_$h"."name"
FROM "test_table" AS "TestModel_$h"
WHERE (("TestModel_$h"."id" > ?))"""

        Assert.assertEquals(expected, s)
        Assert.assertArrayEquals(arrayOf(4), p?.toTypedArray())
    }

    @Test
    fun testSelectWithNull() {
        val m = TestModel()
        val q = EntitySelectDSL(m)
                .from()
                .where { m.F(m::id) eq null }
        val s = q.toSQL()

        val h = m.hashCode()
        val expected = """SELECT "TestModel_$h"."flag", "TestModel_$h"."id", "TestModel_$h"."name"
FROM "test_table" AS "TestModel_$h"
WHERE (("TestModel_$h"."id" IS NULL))"""

        Assert.assertEquals(expected, s)
    }
}
