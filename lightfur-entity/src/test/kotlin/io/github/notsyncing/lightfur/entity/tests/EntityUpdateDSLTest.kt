package io.github.notsyncing.lightfur.entity.tests

import io.github.notsyncing.lightfur.entity.EntityGlobal
import io.github.notsyncing.lightfur.entity.dsl.EntityUpdateDSL
import io.github.notsyncing.lightfur.entity.gt
import io.github.notsyncing.lightfur.entity.tests.toys.TestModel
import io.github.notsyncing.lightfur.entity.tests.toys.TestModelMultiPK
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class EntityUpdateDSLTest {
    @Before
    fun setUp() {
        EntityGlobal.reset()
    }

    @Test
    fun testSimpleUpdate() {
        val m = TestModel()
        m.id = 2
        m.flag = 3
        m.name = "test"
        m.assumeNoChange()

        m.flag = 4

        val q = EntityUpdateDSL(m)
                .set()
        val s = q.toSQL()
        val p = q.toSQLParameters()

        val h = m.hashCode()
        val expected = """UPDATE "test_table" AS "TestModel_$h"
SET "flag" = (?)
WHERE ("TestModel_$h"."id" = ?)"""

        Assert.assertEquals(expected, s)
        Assert.assertArrayEquals(arrayOf(4, 2), p.toTypedArray())
    }

    @Test
    fun testSimpleFullUpdate() {
        val m = TestModel()
        m.id = 2
        m.flag = 3
        m.name = "test"
        m.assumeAllChanged()

        val q = EntityUpdateDSL(m)
                .set()
        val s = q.toSQL()
        val p = q.toSQLParameters()

        val h = m.hashCode()
        val expected = """UPDATE "test_table" AS "TestModel_$h"
SET "flag" = (?), "name" = (?)
WHERE ("TestModel_$h"."id" = ?)"""

        Assert.assertEquals(expected, s)
        Assert.assertArrayEquals(arrayOf(3, "test", 2), p.toTypedArray())
    }

    @Test
    fun testSimpleUpdateWithConditions() {
        val m = TestModel()
        m.id = 2
        m.flag = 3
        m.name = "test"
        m.assumeNoChange()

        m.flag = 4

        val q = EntityUpdateDSL(m)
                .set()
                .where { m.F(m::flag) gt 6 }
        val s = q.toSQL()
        val p = q.toSQLParameters()

        val h = m.hashCode()
        val expected = """UPDATE "test_table" AS "TestModel_$h"
SET "flag" = (?)
WHERE (("TestModel_$h"."flag" > ?))"""

        Assert.assertEquals(expected, s)
        Assert.assertArrayEquals(arrayOf(4, 6), p.toTypedArray())
    }

    @Test
    fun testSimpleUpdateWithMultiplePrimaryKeys() {
        val m = TestModelMultiPK()
        m.id = 2
        m.flag = 3
        m.name = "test"
        m.assumeNoChange()

        m.flag = 4

        val q = EntityUpdateDSL(m)
                .set()
        val s = q.toSQL()
        val p = q.toSQLParameters()

        val h = m.hashCode()
        val expected = """UPDATE "test_table" AS "TestModelMultiPK_$h"
SET "flag" = (?)
WHERE ("TestModelMultiPK_$h"."id" = ?) AND ("TestModelMultiPK_$h"."name" = ?)"""

        Assert.assertEquals(expected, s)
        Assert.assertArrayEquals(arrayOf(4, 2, "test"), p.toTypedArray())
    }
}