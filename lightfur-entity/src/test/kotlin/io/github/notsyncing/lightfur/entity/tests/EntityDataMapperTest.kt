package io.github.notsyncing.lightfur.entity.tests

import io.github.notsyncing.lightfur.entity.EntityDataMapper
import io.github.notsyncing.lightfur.entity.EntityModel
import io.github.notsyncing.lightfur.tests.DataMapperTest
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.sql.ResultSet
import org.junit.Assert
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class EntityDataMapperTest : DataMapperTest(EntityDataMapper::class.java) {
    class TestObject : EntityModel(table = "") {
        var id: Int by field(column = "id")

        var name: String? by field(column = "username")

        var date: LocalDateTime? by field(column = "date")

        var type: TestEnum? by field(column = "type")

        var list: IntArray? by field(column = "list")

        var list2: IntArray? by field(column = "list2")

        var complex: TestInnerObject? by field(column = "complex")

        var complexArray: Array<TestInnerObject>? by field(column = "complex2")

        var longNumber: BigDecimal? by field(column = "long_number")
    }

    class TestLongObject : EntityModel(table = "") {
        var id: Long by field(column = "id")

        var list: LongArray? by field(column = "list")
    }

    @Test
    override fun testMap() {
        val r = ResultSet()
        r.columnNames = Arrays.asList("id", "username", "date", "type", "list", "list2", "complex", "complex2", "long_number")

        val arr = JsonArray()
        arr.add(1)
        arr.add("test")
        arr.add("2015-04-03T11:35:29.384")
        arr.add(TestEnum.TypeB.ordinal)
        arr.add("{1,2,3}")
        arr.add(JsonArray("[4,5,6]"))
        arr.add(JsonObject("{\"a\":7,\"b\":8}"))
        arr.add("[{\"a\":9,\"b\":0},{\"a\":1,\"b\":2}]")
        arr.add("92375947293472934923794729345345345433.2345345345")
        r.results = Arrays.asList(arr)

        val o = dataMapper.map(TestObject::class.java, r)
        Assert.assertNotNull(o)
        Assert.assertEquals(1, o.id.toLong())
        Assert.assertEquals("test", o.name)

        val t = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")

        Assert.assertEquals("2015-04-03T11:35:29.384", t.format(o.date))
        Assert.assertEquals(TestEnum.TypeB, o.type)
        Assert.assertArrayEquals(intArrayOf(1, 2, 3), o.list)
        Assert.assertArrayEquals(intArrayOf(4, 5, 6), o.list2)

        Assert.assertNotNull(o.complex)
        Assert.assertEquals(7, o.complex?.a)
        Assert.assertEquals(8, o.complex?.b)

        Assert.assertNotNull(o.complexArray)
        Assert.assertEquals(2, o.complexArray?.size)
        Assert.assertNotNull(o.complexArray!![0])
        Assert.assertEquals(9, o.complexArray!![0].a)
        Assert.assertEquals(0, o.complexArray!![0].b)
        Assert.assertNotNull(o.complexArray!![1])
        Assert.assertEquals(1, o.complexArray!![1].a)
        Assert.assertEquals(2, o.complexArray!![1].b)

        Assert.assertTrue(BigDecimal("92375947293472934923794729345345345433.2345345345") == o.longNumber)
    }

    @Test
    override fun testMapLong() {
        val r = ResultSet()
        r.columnNames = Arrays.asList("id", "list")

        val arr = JsonArray()
        arr.add(19839L)
        arr.add("{78998,325345,3678346}")
        r.results = Arrays.asList(arr)

        val o = dataMapper.map(TestLongObject::class.java, r)
        Assert.assertNotNull(o)
        Assert.assertEquals(19839L, o.id)
        Assert.assertArrayEquals(longArrayOf(78998L, 325345L, 3678346L), o.list)
    }
}