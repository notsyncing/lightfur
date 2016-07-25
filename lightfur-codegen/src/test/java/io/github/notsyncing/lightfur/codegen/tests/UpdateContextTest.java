package io.github.notsyncing.lightfur.codegen.tests;

import com.google.testing.compile.JavaFileObjects;
import io.github.notsyncing.lightfur.DataSession;
import io.github.notsyncing.lightfur.DatabaseManager;
import io.github.notsyncing.lightfur.codegen.QueryContext;
import io.github.notsyncing.lightfur.codegen.UpdateContext;
import io.github.notsyncing.lightfur.codegen.tests.toys.TestDataRepository;
import io.github.notsyncing.lightfur.codegen.tests.toys.TestModel;
import io.github.notsyncing.lightfur.dsl.Query;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

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
                "SET \"test_table\".\"name\" = ('tested')\n" +
                "WHERE (\"test_table\".\"id\" > 1)\n" +
                "RETURNING \"test_table\".\"id\"";

        UpdateContext<TestModel> q = (UpdateContext<TestModel>) Query.update(TestModel.class, "simpleData");
        assertEquals(expected, q.getSql());
    }
}
