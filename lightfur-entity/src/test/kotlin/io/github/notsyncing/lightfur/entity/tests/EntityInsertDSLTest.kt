package io.github.notsyncing.lightfur.entity.tests

import io.github.notsyncing.lightfur.entity.EntityGlobal
import io.github.notsyncing.lightfur.entity.dsl.EntityInsertDSL
import io.github.notsyncing.lightfur.entity.eq
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
        val h = m.hashCode()

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
        val h = m.hashCode()

        val expected = """INSERT INTO "test_table" ("flag")
VALUES (?)
RETURNING "id", "name""""

        Assert.assertEquals(expected, s)
        Assert.assertArrayEquals(arrayOf(3), p.toTypedArray())
    }

    @Test
    fun testInsertWithSkippedColumns() {
        val m = TestModel()
        m.id = 3
        m.flag = 4
        m.name = "skip"

        val q = EntityInsertDSL(m)
                .values(skips = listOf(m::flag))
        val s = q.toSQL()
        val p = q.toSQLParameters()
        val h = m.hashCode()

        val expected = """INSERT INTO "test_table" ("name")
VALUES (?)
RETURNING "id""""

        Assert.assertEquals(expected, s)
        Assert.assertArrayEquals(arrayOf("skip"), p.toTypedArray())
    }

    @Test
    fun testUpsert() {
        val m = TestModel()
        m.id = 3
        m.flag = 4
        m.name = "skip"

        val q = EntityInsertDSL(m)
                .values()
                .updateWhenExists(m.F(m::id)) {
                    it.set(m.F(m::flag), 5)
                            .where { m.F(m::id) eq 3 }
                }

        val s = q.toSQL()
        val p = q.toSQLParameters()
        val h = m.hashCode()

        val expected = """INSERT INTO "test_table" AS "TestModel_$h" ("flag", "name")
VALUES (?, ?)
ON CONFLICT ("id") DO UPDATE
SET "flag" = (?)
WHERE (("TestModel_$h"."id" = ?))
RETURNING "id""""

        Assert.assertEquals(expected, s)
        Assert.assertArrayEquals(arrayOf(4, "skip", 5, 3), p.toTypedArray())
    }
}