package io.github.notsyncing.lightfur.codegen.tests;

import io.github.notsyncing.lightfur.DatabaseManager;
import io.github.notsyncing.lightfur.codegen.contexts.DeleteContext;
import io.github.notsyncing.lightfur.codegen.tests.toys.TestModel;
import io.github.notsyncing.lightfur.dsl.Query;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DeleteContextTest
{
    private DatabaseManager db;

    @Before
    public void setUp()
    {
        db = DatabaseManager.getInstance();
    }

    @Test
    public void testDeleteSimpleData()
    {
        String expected = "DELETE FROM \"test_table\"\n" +
                "WHERE (\"test_table\".\"id\" > 1)\n" +
                "RETURNING \"test_table\".\"id\"";

        DeleteContext<TestModel> q = (DeleteContext<TestModel>) Query.remove(TestModel.class, "simpleData");
        assertEquals(expected, q.getSql());
    }
}
