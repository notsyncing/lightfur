package io.github.notsyncing.lightfur.codegen.tests;

import io.github.notsyncing.lightfur.DatabaseManager;
import io.github.notsyncing.lightfur.codegen.contexts.QueryContext;
import io.github.notsyncing.lightfur.codegen.tests.toys.TestModel;
import io.github.notsyncing.lightfur.dsl.Query;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class QueryContextTest
{
    private DatabaseManager db;

    @Before
    public void setUp()
    {
        db = DatabaseManager.getInstance();
    }

    @Test
    public void testQuerySimpleData()
    {
        String expected = "SELECT \"test_table\".\"id\", \"test_table\".\"name\"\n" +
                "FROM \"test_table\"\n" +
                "LIMIT 1";

        QueryContext<TestModel> q = (QueryContext<TestModel>)Query.get(TestModel.class, "simpleData");
        assertEquals(expected, q.getSql());
    }
}