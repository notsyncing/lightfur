package io.github.notsyncing.lightfur.integration.vertx.tests;

import io.github.notsyncing.lightfur.DatabaseManager;
import io.github.notsyncing.lightfur.integration.vertx.VertxPostgreSQLDriver;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class DatabaseManagerTest
{
    private DatabaseManager db;
    private static final String TEST_DB = "lightfur_test_db";

    @Before
    public void setUp()
    {
        DatabaseManager.setDriver(new VertxPostgreSQLDriver());

        db = DatabaseManager.getInstance();
        db.init("postgres");
    }

    @After
    public void tearDown(TestContext context)
    {
        Async async = context.async();

        db.dropDatabase(TEST_DB, true)
                .thenCompose(r -> db.close())
                .thenAccept(r -> async.complete())
                .exceptionally(ex -> {
                    context.fail((Throwable)ex);
                    async.complete();
                    return null;
                });
    }

    @Test
    public void testGetConnection(TestContext context)
    {
        Async async = context.async();

        db.getConnection()
                .thenAccept(cc -> {
                    SQLConnection c = (SQLConnection) cc;

                    context.assertNotNull(c);
                    c.close();
                    async.complete();
                })
                .exceptionally(ex -> {
                    context.fail(ex);
                    async.complete();
                    return null;
                });
    }

    @Test
    public void testCreateDatabase(TestContext context)
    {
        Async async = context.async();

        db.createDatabase(TEST_DB, false)
                .thenCompose(r -> db.getConnection())
                .thenAccept(cc -> {
                    SQLConnection c = (SQLConnection) cc;

                    c.query("SELECT 1 FROM pg_database WHERE datname = '" + TEST_DB + "'", result -> {
                        c.close();

                        if (result.failed()) {
                            context.fail(result.cause());
                        } else {
                            ResultSet data = result.result();
                            context.assertEquals(1, data.getNumRows());
                            context.assertEquals(1, data.getResults().get(0).getInteger(0));
                        }

                        async.complete();
                    });
                })
                .exceptionally(ex -> {
                    context.fail(ex);
                    async.complete();
                    return null;
                });
    }

    @Test
    public void testDropDatabase(TestContext context)
    {
        Async async = context.async();

        db.getConnection()
                .thenAccept(cc -> {
                    SQLConnection c = (SQLConnection) cc;

                    c.execute("CREATE DATABASE " + TEST_DB, h -> {
                        if (h.failed()) {
                            c.close();
                            context.fail(h.cause());
                            return;
                        }

                        db.dropDatabase(TEST_DB)
                                .thenAccept(r -> c.query("SELECT 1 FROM pg_database WHERE datname = '" + TEST_DB + "'", result -> {
                                    c.close();

                                    if (result.failed()) {
                                        context.fail(result.cause());
                                    } else {
                                        ResultSet data = result.result();
                                        context.assertEquals(0, data.getNumRows());
                                    }

                                    async.complete();
                                }))
                                .exceptionally(ex -> {
                                    context.fail(ex);
                                    async.complete();
                                    return null;
                                });
                    });
                })
                .exceptionally(ex -> {
                    context.fail(ex);
                    async.complete();
                    return null;
                });
    }
}
