package io.github.notsyncing.lightfur.integration.vertx.tests;

import io.github.notsyncing.lightfur.core.annotations.entity.Column;
import io.github.notsyncing.lightfur.core.tests.DataMapperTest;
import io.github.notsyncing.lightfur.integration.vertx.ReflectDataMapper;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ReflectDataMapperTest extends DataMapperTest
{
    public static class TestPrivateModel
    {
        @Column("id")
        private int id;

        @Column("name")
        private String name;

        public int getId()
        {
            return id;
        }

        public void setId(int id)
        {
            this.id = id;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }
    }

    public ReflectDataMapperTest() {
        super(ReflectDataMapper.class);
    }

    @Test
    public void testMapPrivateFields() throws InstantiationException, IllegalAccessException
    {
        JsonObject o = new JsonObject()
                .put("id", 2)
                .put("name", "test");

        TestPrivateModel m = new ReflectDataMapper().mapSingleRow(TestPrivateModel.class, o);

        assertNotNull(m);
        assertEquals(2, m.getId());
        assertEquals("test", m.getName());
    }
}
