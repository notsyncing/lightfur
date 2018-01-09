package io.github.notsyncing.lightfur.entity.tests

import io.github.notsyncing.lightfur.DataSession
import io.github.notsyncing.lightfur.entity.EntityModel
import io.github.notsyncing.lightfur.entity.EntityQueryExecutor
import io.github.notsyncing.lightfur.entity.dsl.EntityBaseDSL
import io.github.notsyncing.lightfur.entity.tests.toys.A
import io.github.notsyncing.lightfur.entity.tests.toys.A2
import io.github.notsyncing.lightfur.entity.tests.toys.B
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CompletableFuture

class EntityModelReferenceTest {
    @Before
    fun setUp() {
    }

    @Test
    fun testLoadOneToOneReference() {
        val a = A()
        a.id = 2L

        val db = mockk<DataSession<*, *, *>>()
        val dslSlot = slot<EntityBaseDSL<*>>()
        val queryExecutor = mockk<EntityQueryExecutor<Any, Any, Any>>()

        every {
            queryExecutor.execute(capture(dslSlot), any())
        } answers {
            CompletableFuture.completedFuture(Pair(listOf<Any>(B()), 1))
        }

        EntityBaseDSL.setQueryExecutor(queryExecutor)

        val r = a.load(db, a::b).get()

        assertNotNull(a.b)
        assertEquals(r.data, a.b)

        val h = r._intermediateModel.hashCode()

        val expectedSql = """
            SELECT "B_$h"."a_id", "B_$h"."id"
            FROM "b" AS "B_$h"
            WHERE (("B_$h"."a_id" = ?))
            """.trimIndent()

        assertEquals(expectedSql, dslSlot.captured.toSQL())

        val expectedParams = arrayOf(2L)

        assertArrayEquals(expectedParams, dslSlot.captured.toSQLParameters().toTypedArray())
    }

    @Test
    fun testLoadOneToManyReference() {
        val a = A2()
        a.id = 3L

        val db = mockk<DataSession<*, *, *>>()
        val dslSlot = slot<EntityBaseDSL<*>>()
        val queryExecutor = mockk<EntityQueryExecutor<Any, Any, Any>>()

        val bList = listOf(B(), B(), B())

        every {
            queryExecutor.execute(capture(dslSlot), any())
        } answers {
            CompletableFuture.completedFuture(Pair(bList, bList.size))
        }

        EntityBaseDSL.setQueryExecutor(queryExecutor)

        val r = a.load(db, a::b).get()

        assertNotNull(a.b)
        assertEquals(r.data, a.b)
        assertEquals(3, a.b.size)
        assertEquals(bList[0], a.b[0])
        assertEquals(bList[1], a.b[1])
        assertEquals(bList[2], a.b[2])

        val h = r._intermediateModel.hashCode()

        val expectedSql = """
            SELECT "B_$h"."a_id", "B_$h"."id"
            FROM "b" AS "B_$h"
            WHERE (("B_$h"."a_id" = ?))
            """.trimIndent()

        assertEquals(expectedSql, dslSlot.captured.toSQL())

        val expectedParams = arrayOf(3L)

        assertArrayEquals(expectedParams, dslSlot.captured.toSQLParameters().toTypedArray())
    }

    @Test
    fun testSaveOneToOneReference() {
        val a = A()
        a.id = 2L

        val b = B()
        b.id = 3L

        a.b = b

        val db = mockk<DataSession<*, *, *>>()
        val dslSlot = slot<EntityBaseDSL<*>>()
        val queryExecutor = mockk<EntityQueryExecutor<Any, Any, Any>>()

        every {
            queryExecutor.execute(capture(dslSlot), any())
        } answers {
            CompletableFuture.completedFuture(Pair(listOf<EntityModel>(b), 1))
        }

        EntityBaseDSL.setQueryExecutor(queryExecutor)

        val r = a.save(db, a::b).get()

        assertNotNull(r._intermediateModel)
        assertEquals(1, r.count)
        assertEquals(1, r.list.size)
        assertNotNull(r.list[0])

        assertEquals(3L, (r.list[0] as B).id)
        assertEquals(2L, (r.list[0] as B).aId)

        val expectedSql = """
            INSERT INTO "b" ("a_id", "id")
            VALUES (?, ?)
            ON CONFLICT ("id") DO UPDATE
            SET "a_id" = (?)
            RETURNING "id"
            """.trimIndent()

        assertEquals(expectedSql, dslSlot.captured.toSQL())

        val expectedParams = arrayOf(2L, 3L, 2L)

        assertArrayEquals(expectedParams, dslSlot.captured.toSQLParameters().toTypedArray())
    }

    @Test
    fun testSaveOneToManyReferenceAllNew() {
        val a = A2()
        a.id = 2L

        val b1 = B()
        b1.id = 3L

        val b2 = B()
        b2.id = 4L

        val b3 = B()
        b3.id = 5L

        a.b.add(b1)
        a.b.add(b2)
        a.b.add(b3)

        val db = mockk<DataSession<*, *, *>>()
        val dslSlot = mutableListOf<EntityBaseDSL<*>>()
        val queryExecutor = mockk<EntityQueryExecutor<Any, Any, Any>>()

        every {
            queryExecutor.execute(capture(dslSlot), any())
        } answers {
            CompletableFuture.completedFuture(Pair(listOf<EntityModel>(a), 1))
        }

        EntityBaseDSL.setQueryExecutor(queryExecutor)

        val r = a.save(db, a::b).get()

        val h = r._intermediateModel!!.hashCode()

        assertEquals(5, dslSlot.size)

        val expectedSql0 = """
            INSERT INTO "c" ("id")
            VALUES (?)
            ON CONFLICT ("id") DO NOTHING
            RETURNING "id"
            """.trimIndent()

        assertEquals(expectedSql0, dslSlot[0].toSQL())
        assertArrayEquals(arrayOf(2L), dslSlot[0].toSQLParameters().toTypedArray())

        val expectedSql1 = """
            INSERT INTO "b" ("a_id", "id")
            VALUES (?, ?)
            ON CONFLICT ("id") DO UPDATE
            SET "a_id" = (?)
            RETURNING "id"
            """.trimIndent()

        assertEquals(expectedSql1, dslSlot[1].toSQL())
        assertArrayEquals(arrayOf(2L, 3L, 2L), dslSlot[1].toSQLParameters().toTypedArray())

        assertEquals(expectedSql1, dslSlot[2].toSQL())
        assertArrayEquals(arrayOf(2L, 4L, 2L), dslSlot[2].toSQLParameters().toTypedArray())

        assertEquals(expectedSql1, dslSlot[3].toSQL())
        assertArrayEquals(arrayOf(2L, 5L, 2L), dslSlot[3].toSQLParameters().toTypedArray())

        val expectedSql2 = """
            DELETE FROM "b" AS "B_$h"
            WHERE (("B_$h"."a_id" = ?) AND ("B_$h"."id") NOT  IN ((3), (4), (5)))
            """.trimIndent()

        assertEquals(expectedSql2, dslSlot[4].toSQL())
        assertArrayEquals(arrayOf(2L), dslSlot[4].toSQLParameters().toTypedArray())
    }

    @Test
    fun testSaveOneToManyReferenceAllDelete() {
        val a = A2()
        a.id = 2L

        a.referenceMap[a::b.name]!!.changed = true

        val db = mockk<DataSession<*, *, *>>()
        val dslSlot = mutableListOf<EntityBaseDSL<*>>()
        val queryExecutor = mockk<EntityQueryExecutor<Any, Any, Any>>()

        every {
            queryExecutor.execute(capture(dslSlot), any())
        } answers {
            CompletableFuture.completedFuture(Pair(listOf<EntityModel>(a), 1))
        }

        EntityBaseDSL.setQueryExecutor(queryExecutor)

        val r = a.save(db, a::b).get()

        val h = r._intermediateModel!!.hashCode()

        assertEquals(2, dslSlot.size)

        val expectedSql0 = """
            INSERT INTO "c" ("id")
            VALUES (?)
            ON CONFLICT ("id") DO NOTHING
            RETURNING "id"
            """.trimIndent()

        assertEquals(expectedSql0, dslSlot[0].toSQL())
        assertArrayEquals(arrayOf(2L), dslSlot[0].toSQLParameters().toTypedArray())

        val expectedSql1 = """
            DELETE FROM "b" AS "B_$h"
            WHERE (("B_$h"."a_id" = ?) AND true)
            """.trimIndent()

        assertEquals(expectedSql1, dslSlot[1].toSQL())
        assertArrayEquals(arrayOf(2L), dslSlot[1].toSQLParameters().toTypedArray())
    }
}