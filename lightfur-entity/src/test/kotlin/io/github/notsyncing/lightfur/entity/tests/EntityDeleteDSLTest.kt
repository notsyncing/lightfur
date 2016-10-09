package io.github.notsyncing.lightfur.entity.tests

import io.github.notsyncing.lightfur.entity.EntityGlobal
import io.github.notsyncing.lightfur.entity.dsl.EntityDeleteDSL
import io.github.notsyncing.lightfur.entity.eq
import io.github.notsyncing.lightfur.entity.tests.toys.TestModel
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class EntityDeleteDSLTest {
    @Before
    fun setUp() {
        EntityGlobal.reset()
    }

    @Test
    fun testSimpleDelete() {
        val m = TestModel()
        m.id = 2
        m.flag = 3
        m.name = "test"

        val s = EntityDeleteDSL(m)
                .where { m.F(m::id) eq 2 }
                .toSQL()

        val h = m.hashCode()
        val expected = """DELETE FROM "test_table" AS "TestModel_$h"
WHERE (("TestModel_$h"."id" = 2))"""

        Assert.assertEquals(expected, s)
    }
}