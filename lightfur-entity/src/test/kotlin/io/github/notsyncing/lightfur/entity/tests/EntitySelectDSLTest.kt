package io.github.notsyncing.lightfur.entity.tests

import io.github.notsyncing.lightfur.entity.EntityGlobal
import io.github.notsyncing.lightfur.entity.dsl.EntitySelectDSL
import io.github.notsyncing.lightfur.entity.gt
import io.github.notsyncing.lightfur.entity.minus
import io.github.notsyncing.lightfur.entity.plus
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
        val s = EntitySelectDSL(m)
                .from()
                .toSQL()

        val h = m.hashCode()
        val expected = """SELECT "TestModel_$h"."flag", "TestModel_$h"."name", "TestModel_$h"."id"
FROM "test_table" AS "TestModel_$h""""

        Assert.assertEquals(expected, s)
    }

    @Test
    fun testSelectWithMap() {
        val ms = TestModel()
        val mt = TestModel()
        val s = EntitySelectDSL(mt)
                .from(ms)
                .map(ms.F(ms::name) + "_test", mt::name)
                .map(ms.F(ms::id) - 2, mt::id)
                .map(ms.F(ms::flag), mt::flag)
                .toSQL()

        val hs = ms.hashCode()
        val ht = mt.hashCode()
        val expected = """SELECT (("TestModel_$hs"."name" + '_test')) AS "name", (("TestModel_$hs"."id" - 2)) AS "id", ("TestModel_$hs"."flag") AS "flag"
FROM "test_table" AS "TestModel_$hs""""

        Assert.assertEquals(expected, s)
    }

    @Test
    fun testSelectWithSimpleConditions() {
        val m = TestModel()
        val s = EntitySelectDSL(m)
                .from()
                .where { m.F(m::id) gt 4 }
                .toSQL()

        val h = m.hashCode()
        val expected = """SELECT "TestModel_$h"."flag", "TestModel_$h"."name", "TestModel_$h"."id"
FROM "test_table" AS "TestModel_$h"
WHERE (("TestModel_$h"."id" > 4))"""

        Assert.assertEquals(expected, s)
    }
}
