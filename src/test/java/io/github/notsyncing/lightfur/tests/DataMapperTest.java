package io.github.notsyncing.lightfur.tests;

import io.github.notsyncing.lightfur.annotations.entity.Column;
import io.github.notsyncing.lightfur.entity.DataMapper;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

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
    }

    public static class TestInnerObject
    {
        public int a;
        public int b;
    }

    @Test
    public void testMap() throws InstantiationException, IllegalAccessException, ParseException
    {
        ResultSet r = new ResultSet();
        r.setColumnNames(Arrays.asList("id", "username", "date", "type", "list", "list2", "complex"));

        JsonArray arr = new JsonArray();
        arr.add(1);
        arr.add("test");
        arr.add("2015-04-03T11:35:29.384");
        arr.add(TestEnum.TypeB.ordinal());
        arr.add("{1,2,3}");
        arr.add(new JsonArray("[4,5,6]"));
        arr.add(new JsonObject("{\"a\":7,\"b\":8}"));
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
    }
}
