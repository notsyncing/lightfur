package io.github.notsyncing.lightfur.entity.tests

import io.github.notsyncing.lightfur.entity.EntityGlobal
import io.github.notsyncing.lightfur.entity.dsl.EntityInsertDSL
import io.github.notsyncing.lightfur.entity.tests.toys.TestModel
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class EntityInsertDSLTest {
    @Before
    fun setUp() {
        EntityGlobal.reset()
    }

    @Test
    fun testSimpleInsert() {
        val m = TestModel()
        m.id = 2
        m.flag = 3
        m.name = "test"

        val s = EntityInsertDSL(m).values()
                .toSQL()

        val expected = """INSERT INTO "test_table" ("flag", "name")
VALUES ('3', 'test')"""

        Assert.assertEquals(expected, s)
    }
}