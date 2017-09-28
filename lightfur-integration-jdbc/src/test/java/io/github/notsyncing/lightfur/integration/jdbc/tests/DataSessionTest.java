package io.github.notsyncing.lightfur.integration.jdbc.tests;

import io.github.notsyncing.lightfur.DataSession;
import io.github.notsyncing.lightfur.DatabaseManager;
import io.github.notsyncing.lightfur.integration.jdbc.JdbcDataSession;
import io.github.notsyncing.lightfur.integration.jdbc.JdbcPostgreSQLDriver;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;

@RunWith(VertxUnitRunner.class)
public class DataSessionTest
{
    private DatabaseManager db;
    private static final String TEST_DB = "lightfur_test_db";

    @Before
    public void setUp(TestContext context)
    {
        DatabaseManager.setDriver(new JdbcPostgreSQLDriver());
        DataSession.setCreator(() -> new JdbcDataSession());

        Async async = context.async();

        db = DatabaseManager.getInstance();
        db.init("postgres");
        db.dropDatabase(TEST_DB, true)
                .thenCompose(r -> db.createDatabase(TEST_DB, true))
                .thenCompose(r -> db.getConnection())
                .thenAccept(cc -> {
                    try {
                        Connection c = (Connection) cc;
                        Statement s = c.createStatement();

                        s.execute("CREATE TABLE test (id SERIAL PRIMARY KEY, message TEXT, flag INTEGER, arr INTEGER[], text_arr TEXT[], price NUMERIC(38,6))");

                        s.close();
                        c.close();
                    } catch (Exception e) {
                        throw new CompletionException(e);
                    }
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

        JdbcDataSession session = DataSession.start();
        session.executeWithReturning("INSERT INTO test (message, flag) VALUES (?, ?) RETURNING id", "test", 1)
                .thenAccept(r -> {
                    try {
                        context.assertTrue(r.next());
                        context.assertEquals(1, r.getMetaData().getColumnCount());
                        context.assertEquals("id", r.getMetaData().getColumnLabel(1));
                        context.assertEquals(1, r.getInt(1));
                        context.assertFalse(r.next());
                    } catch (Exception e) {
                        context.fail(e);
                    }

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

        JdbcDataSession session = DataSession.start();
        session.execute("INSERT INTO test (message, flag, arr, price) VALUES (?, ?, ?, ?)", "test2", 2, new int[] { 1, 2, 3 }, new BigDecimal("23445345343.000000"))
                .thenAccept(r -> context.assertEquals(1L, r.getUpdated()))
                .thenCompose(r -> session.query("SELECT message, flag, arr, price FROM test"))
                .thenAccept(r -> {
                    try {
                        context.assertTrue(r.next());
                        context.assertEquals(4, r.getMetaData().getColumnCount());
                        context.assertEquals("test2", r.getString(1));
                        context.assertEquals(2, r.getInt(2));
                        Assert.assertArrayEquals(new Integer[] { 1, 2, 3 }, (Object[])r.getArray(3).getArray());
                        context.assertEquals(new BigDecimal("23445345343.000000"), r.getBigDecimal(4));
                        context.assertFalse(r.next());
                    } catch (Exception e) {
                        context.fail(e);
                    }

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

        JdbcDataSession session = DataSession.start();
        session.execute("INSERT INTO test (message, flag, arr) VALUES (?, ?, ?)", "test2", 2, new int[] { 1, 2, 3 })
                .thenAccept(r -> context.assertEquals(1L, r.getUpdated()))
                .thenCompose(r -> session.query("SELECT message, flag, arr FROM test WHERE flag = ANY(?)", new Object[] { new int[] { 1, 2, 3 } }))
                .thenAccept(r -> {
                    try {
                        context.assertTrue(r.next());
                        context.assertEquals(3, r.getMetaData().getColumnCount());
                        context.assertEquals("test2", r.getString(1));
                        context.assertEquals(2, r.getInt(2));
                        Assert.assertArrayEquals(new Integer[] { 1, 2, 3 }, (Object[]) r.getArray(3).getArray());
                        context.assertFalse(r.next());
                    } catch (Exception e) {
                        context.fail(e);
                    }

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
    public void testQueryWithArray(TestContext context)
    {
        Async async = context.async();

        JdbcDataSession session = DataSession.start();
        session.execute("INSERT INTO test (message, flag, text_arr) VALUES (?, ?, ?)", "test2", 2, new String[] { "b" })
                .thenAccept(r -> context.assertEquals(1L, r.getUpdated()))
                .thenCompose(r -> session.query("SELECT message, flag, text_arr FROM test WHERE text_arr && ?::text[]", new Object[] { new String[] { "b" } }))
                .thenAccept(r -> {
                    try {
                        context.assertTrue(r.next());
                        context.assertEquals(3, r.getMetaData().getColumnCount());
                        context.assertEquals("test2", r.getString(1));
                        context.assertEquals(2, r.getInt(2));
                        Assert.assertArrayEquals(new String[] {"b"}, (String[]) r.getArray(3).getArray());
                        context.assertFalse(r.next());
                    } catch (Exception e) {
                        context.fail(e);
                    }

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
