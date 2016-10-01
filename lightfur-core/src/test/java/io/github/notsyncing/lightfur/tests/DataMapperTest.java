package io.github.notsyncing.lightfur.tests;

import io.github.notsyncing.lightfur.annotations.entity.Column;
import io.github.notsyncing.lightfur.entity.DataMapper;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;

import static org.junit.Assert.*;

@RunWith(VertxUnitRunner.class)
public class DataMapperTest
{
    public enum TestEnum
    {
        TypeA,
        TypeB
    }

    public static class TestObject
    {
        @Column("id")
        public int id;

        @Column("username")
        public String name;

        @Column("date")
        public Instant date;

        @Column("type")
        public TestEnum type;

        @Column("list")
        public int[] list;

        @Column("list2")
        public int[] list2;

        @Column("complex")
        public TestInnerObject complex;

        @Column("complex2")
        public TestInnerObject[] complexArray;

        @Column("long_number")
        public BigDecimal longNumber;
    }

    public static class TestInnerObject
    {
        public int a;
        public int b;
    }

    public static class TestLongObject
    {
        @Column("id")
        public long id;

        @Column("list")
        public long[] list;
    }

    @Test
    public void testMap() throws InstantiationException, IllegalAccessException, ParseException
    {
        ResultSet r = new ResultSet();
        r.setColumnNames(Arrays.asList("id", "username", "date", "type", "list", "list2", "complex", "complex2", "long_number"));

        JsonArray arr = new JsonArray();
        arr.add(1);
        arr.add("test");
        arr.add("2015-04-03T11:35:29.384");
        arr.add(TestEnum.TypeB.ordinal());
        arr.add("{1,2,3}");
        arr.add(new JsonArray("[4,5,6]"));
        arr.add(new JsonObject("{\"a\":7,\"b\":8}"));
        arr.add("[{\"a\":9,\"b\":0},{\"a\":1,\"b\":2}]");
        arr.add("92375947293472934923794729345345345433.2345345345");
        r.setResults(Arrays.asList(arr));

        TestObject o = DataMapper.map(TestObject.class, r);
        assertNotNull(o);
        assertEquals(1, o.id);
        assertEquals("test", o.name);
        assertEquals(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S").parse("2015-04-03T11:35:29.384").toInstant().getEpochSecond(),
                o.date.getEpochSecond());
        assertEquals(TestEnum.TypeB, o.type);
        assertArrayEquals(new int[] { 1, 2, 3 }, o.list);
        assertArrayEquals(new int[] { 4, 5, 6 }, o.list2);

        assertNotNull(o.complex);
        assertEquals(7, o.complex.a);
        assertEquals(8, o.complex.b);

        assertNotNull(o.complexArray);
        assertEquals(2, o.complexArray.length);
        assertNotNull(o.complexArray[0]);
        assertEquals(9, o.complexArray[0].a);
        assertEquals(0, o.complexArray[0].b);
        assertNotNull(o.complexArray[1]);
        assertEquals(1, o.complexArray[1].a);
        assertEquals(2, o.complexArray[1].b);

        assertTrue(new BigDecimal("92375947293472934923794729345345345433.2345345345").equals(o.longNumber));
    }

    @Test
    public void testMapLong() throws InstantiationException, IllegalAccessException
    {
        ResultSet r = new ResultSet();
        r.setColumnNames(Arrays.asList("id", "list"));

        JsonArray arr = new JsonArray();
        arr.add(19839L);
        arr.add("{78998,325345,3678346}");
        r.setResults(Arrays.asList(arr));

        TestLongObject o = DataMapper.map(TestLongObject.class, r);
        assertNotNull(o);
        assertEquals(19839L, o.id);
        assertArrayEquals(new long[] { 78998L, 325345L, 3678346L }, o.list);
    }
}
