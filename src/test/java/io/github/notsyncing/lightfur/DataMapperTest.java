package io.github.notsyncing.lightfur;

import io.github.notsyncing.lightfur.annotations.entity.Column;
import io.github.notsyncing.lightfur.entity.DataMapper;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
    }

    @Test
    public void testMap() throws InstantiationException, IllegalAccessException, ParseException
    {
        ResultSet r = new ResultSet();
        r.setColumnNames(Arrays.asList("id", "username", "date", "type"));

        JsonArray arr = new JsonArray();
        arr.add(1);
        arr.add("test");
        arr.add("2015-04-03T11:35:29.384");
        arr.add(TestEnum.TypeB.ordinal());
        r.setResults(Arrays.asList(arr));

        TestObject o = DataMapper.map(TestObject.class, r);
        assertNotNull(o);
        assertEquals(1, o.id);
        assertEquals("test", o.name);
        assertEquals(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S").parse("2015-04-03T11:35:29.384").toInstant().getEpochSecond(),
                o.date.getEpochSecond());
        assertEquals(TestEnum.TypeB, o.type);
    }
}
