package io.github.notsyncing.lightfur.entity.tests

import io.github.notsyncing.lightfur.entity.dsl.sql
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class SQLTest {
    @Test
    fun testSimpleSql() {
        val (actual, params) = sql { """
            SELECT * FROM table WHERE id > ${p(3)}
        """.trimIndent()
        }

        val expected = "SELECT * FROM table WHERE id > ?"

        assertEquals(expected, actual)
        assertArrayEquals(arrayOf(3), params.toTypedArray())
    }
}