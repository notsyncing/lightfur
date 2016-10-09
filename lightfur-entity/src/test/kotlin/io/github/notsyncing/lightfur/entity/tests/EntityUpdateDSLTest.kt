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

        val s = EntityUpdateDSL(m)
                .toSQL()

        val h = m.hashCode()
        val expected = """UPDATE "test_table" AS "TestModel_$h"
SET "flag" = (4)"""

        Assert.assertEquals(expected, s)
    }

    @Test
    fun testSimpleFullUpdate() {
        val m = TestModel()
        m.id = 2
        m.flag = 3
        m.name = "test"
        m.assumeAllChanged()

        val s = EntityUpdateDSL(m)
                .toSQL()

        val h = m.hashCode()
        val expected = """UPDATE "test_table" AS "TestModel_$h"
SET "flag" = (3), "name" = ('test')"""

        Assert.assertEquals(expected, s)
    }
}