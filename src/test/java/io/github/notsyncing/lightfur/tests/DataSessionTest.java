package io.github.notsyncing.lightfur.tests;

import io.github.notsyncing.lightfur.DataSession;
import io.github.notsyncing.lightfur.DatabaseManager;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@RunWith(VertxUnitRunner.class)
public class DataSessionTest
{
    private DatabaseManager db;
    private static final String TEST_DB = "lightfur_test_db";

    @Before
    public void setUp(TestContext context)
    {
        Async async = context.async();

        db = DatabaseManager.getInstance();
        db.init("postgres");
        db.dropDatabase(TEST_DB, true)
                .thenCompose(r -> db.createDatabase(TEST_DB, true))
                .thenCompose(r -> db.getConnection())
                .thenCompose(c -> {
                    CompletableFuture f = new CompletableFuture();

                    c.execute("CREATE TABLE test (id SERIAL PRIMARY KEY, message TEXT, flag INTEGER, arr INTEGER[])", h -> {
                        c.close();

                        if (h.succeeded()) {
                            f.complete(h);
                        } else {
                            f.completeExceptionally(h.cause());
                        }
                    });

                    return f;
                })
                .thenAccept(r -> async.complete())
                .exceptionally(ex -> {
                    context.fail((Throwable) ex);
                    async.complete();
                    return null;
                });
    }

    @After
    public void tearDown(TestContext context)
    {
        Async async = context.async();

        db.close()
                .thenCompose((Function) r -> {
                    db.init("postgres");
                    return db.dropDatabase(TEST_DB, true);
                })
                .thenCompose((Function) r -> db.close())
                .thenAccept(r -> async.complete())
                .exceptionally(ex -> {
                    context.fail((Throwable) ex);
                    async.complete();
                    return null;
                });
    }

    @Test
    public void testExecuteWithReturning(TestContext context)
    {
        Async async = context.async();

        DataSession session = new DataSession();
        session.executeWithReturning("INSERT INTO test (message, flag) VALUES (?, ?) RETURNING id", "test", 1)
                .thenAccept(r -> {
                    context.assertEquals(1, r.getNumRows());
                    context.assertEquals(1, r.getNumColumns());
                    context.assertEquals("id", r.getColumnNames().get(0));
                    context.assertEquals(1, r.getResults().get(0).getInteger(0));
                    async.complete();

                    session.end();
                })
                .exceptionally(ex -> {
                    session.end();
                    context.fail((Throwable) ex);
                    async.complete();
                    return null;
                });
    }

    @Test
    public void testExecute(TestContext context)
    {
        Async async = context.async();

        DataSession session = new DataSession();
        session.execute("INSERT INTO test (message, flag, arr) VALUES (?, ?, ?)", "test2", 2, new int[] { 1, 2, 3 })
                .thenAccept(r -> context.assertEquals(1, r.getUpdated()))
                .thenCompose(r -> session.query("SELECT message, flag, arr::text FROM test"))
                .thenAccept(r -> {
                    context.assertEquals(1, r.getNumRows());
                    context.assertEquals(3, r.getNumColumns());
                    context.assertEquals("test2", r.getResults().get(0).getString(0));
                    context.assertEquals(2, r.getResults().get(0).getInteger(1));
                    context.assertEquals("{1,2,3}", r.getResults().get(0).getString(2));

                    async.complete();

                    session.end();
                })
                .exceptionally(ex -> {
                    session.end();
                    context.fail((Throwable) ex);
                    async.complete();
                    return null;
                });
    }

    @Test
    public void testQuery(TestContext context)
    {
        Async async = context.async();

        DataSession session = new DataSession();
        session.execute("INSERT INTO test (message, flag, arr) VALUES (?, ?, ?)", "test2", 2, new int[] { 1, 2, 3 })
                .thenAccept(r -> context.assertEquals(1, r.getUpdated()))
                .thenCompose(r -> session.query("SELECT message, flag, arr::text FROM test WHERE flag = ANY(?)", new Object[] { new int[] { 1, 2, 3 } }))
                .thenAccept(r -> {
                    context.assertEquals(1, r.getNumRows());
                    context.assertEquals(3, r.getNumColumns());
                    context.assertEquals("test2", r.getResults().get(0).getString(0));
                    context.assertEquals(2, r.getResults().get(0).getInteger(1));
                    context.assertEquals("{1,2,3}", r.getResults().get(0).getString(2));

                    async.complete();

                    session.end();
                })
                .exceptionally(ex -> {
                    session.end();
                    context.fail((Throwable) ex);
                    async.complete();
                    return null;
                });
    }
}
