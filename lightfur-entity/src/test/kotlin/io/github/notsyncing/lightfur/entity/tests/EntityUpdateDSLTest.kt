package io.github.notsyncing.lightfur.entity.tests

import io.github.notsyncing.lightfur.entity.EntityGlobal
import io.github.notsyncing.lightfur.entity.dsl.EntityUpdateDSL
import io.github.notsyncing.lightfur.entity.tests.toys.TestModel
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
        val s = q.toSQL()
        val p = q.toSQLParameters()

        val h = m.hashCode()
        val expected = """UPDATE "test_table" AS "TestModel_$h"
SET "flag" = (?)"""

        Assert.assertEquals(expected, s)
        Assert.assertArrayEquals(arrayOf(4), p.toTypedArray())
    }

    @Test
    fun testSimpleFullUpdate() {
        val m = TestModel()
        m.id = 2
        m.flag = 3
        m.name = "test"
        m.assumeAllChanged()

        val q = EntityUpdateDSL(m)
        val s = q.toSQL()
        val p = q.toSQLParameters()

        val h = m.hashCode()
        val expected = """UPDATE "test_table" AS "TestModel_$h"
SET "flag" = (?), "name" = (?)"""

        Assert.assertEquals(expected, s)
        Assert.assertArrayEquals(arrayOf(3, "test"), p.toTypedArray())
    }
}