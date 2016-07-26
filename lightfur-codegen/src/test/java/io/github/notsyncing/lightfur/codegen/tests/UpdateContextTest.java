package io.github.notsyncing.lightfur.codegen.tests;

import io.github.notsyncing.lightfur.DatabaseManager;
import io.github.notsyncing.lightfur.codegen.contexts.UpdateContext;
import io.github.notsyncing.lightfur.codegen.tests.toys.TestModel;
import io.github.notsyncing.lightfur.dsl.Query;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class UpdateContextTest
{
    private DatabaseManager db;

    @Before
    public void setUp()
    {
        db = DatabaseManager.getInstance();
    }

    @Test
    public void testUpdateSimpleData() throws ExecutionException, InterruptedException
    {
        String expected = "UPDATE \"test_table\"\n" +
                "SET \"name\" = ('tested')\n" +
                "WHERE (\"test_table\".\"id\" > 1)\n" +
                "RETURNING \"test_table\".\"id\"";

        UpdateContext<TestModel> q = (UpdateContext<TestModel>) Query.update(TestModel.class, "simpleData");
        assertEquals(expected, q.getSql());
    }

    @Test
    public void testUpdateSimpleDataWithOuterVariable() throws ExecutionException, InterruptedException
    {
        String expected = "UPDATE \"test_table\"\n" +
                "SET \"name\" = (?)\n" +
                "WHERE (\"test_table\".\"id\" > 1)\n" +
                "RETURNING \"test_table\".\"id\"";

        UpdateContext<TestModel> q = (UpdateContext<TestModel>) Query.update(TestModel.class, "simpleData_outVar");
        assertEquals(expected, q.getSql());


    }
}
