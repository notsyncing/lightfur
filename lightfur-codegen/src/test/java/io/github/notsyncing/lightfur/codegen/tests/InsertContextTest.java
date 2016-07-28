package io.github.notsyncing.lightfur.codegen.tests;

import io.github.notsyncing.lightfur.DatabaseManager;
import io.github.notsyncing.lightfur.codegen.contexts.InsertContext;
import io.github.notsyncing.lightfur.codegen.contexts.UpdateContext;
import io.github.notsyncing.lightfur.codegen.tests.toys.TestModel;
import io.github.notsyncing.lightfur.dsl.Query;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class InsertContextTest
{
    private DatabaseManager db;

    @Before
    public void setUp()
    {
        db = DatabaseManager.getInstance();
    }

    @Test
    public void testInsertSimpleData()
    {
        String expected = "INSERT INTO \"test_table\" (\"name\", \"flag\")\n" +
                "VALUES (?, ?)\n" +
                "RETURNING \"test_table\".\"id\"";

        InsertContext<TestModel> q = (InsertContext<TestModel>) Query.add(TestModel.class, "simpleData");
        assertEquals(expected, q.getSql());
    }

    @Test
    public void testInsertSimpleDataWithIgnore()
    {
        String expected = "INSERT INTO \"test_table\" (\"flag\")\n" +
                "VALUES (?)\n" +
                "RETURNING \"test_table\".\"id\"";

        InsertContext<TestModel> q = (InsertContext<TestModel>) Query.add(TestModel.class, "simpleData_ignore");
        assertEquals(expected, q.getSql());
    }
}
