package io.github.notsyncing.lightfur.entity.tests

import io.github.notsyncing.lightfur.entity.EntityGlobal
import io.github.notsyncing.lightfur.entity.dsl.EntityInsertDSL
import io.github.notsyncing.lightfur.entity.tests.toys.TestModel
import io.github.notsyncing.lightfur.entity.tests.toys.TestModelMultiPK
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

        val q = EntityInsertDSL(m).values()
        val s = q.toSQL()
        val p = q.toSQLParameters()

        val expected = """INSERT INTO "test_table" ("flag", "name")
VALUES (?, ?)
RETURNING "id""""

        Assert.assertEquals(expected, s)
        Assert.assertArrayEquals(arrayOf(3, "test"), p.toTypedArray())
    }

    @Test
    fun testInsertWithMultipleAutoGeneratedColumns() {
        val m = TestModelMultiPK()
        m.id = 2
        m.flag = 3
        m.name = "test"

        val q = EntityInsertDSL(m).values()
        val s = q.toSQL()
        val p = q.toSQLParameters()

        val expected = """INSERT INTO "test_table" ("flag")
VALUES (?)
RETURNING "id", "name""""

        Assert.assertEquals(expected, s)
        Assert.assertArrayEquals(arrayOf(3), p.toTypedArray())
    }
}