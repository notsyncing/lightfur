package io.github.notsyncing.lightfur.integration.jdbc.tests;

import io.github.notsyncing.lightfur.core.DatabaseManager;
import io.github.notsyncing.lightfur.integration.jdbc.JdbcPostgreSQLDriver;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@RunWith(VertxUnitRunner.class)
public class DatabaseManagerTest
{
    private DatabaseManager db;
    private static final String TEST_DB = "lightfur_test_db";

    @Before
    public void setUp()
    {
        DatabaseManager.setDriver(new JdbcPostgreSQLDriver());

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
                    Connection c = (Connection) cc;

                    context.assertNotNull(c);

                    try {
                        c.close();
                        async.complete();
                    } catch (Exception e) {
                        context.fail(e);
                    }
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
                    try (Connection c = (Connection) cc;
                         Statement s = c.createStatement()) {
                        try (ResultSet r = s.executeQuery("SELECT 1 FROM pg_database WHERE datname = '" + TEST_DB + "'")) {
                            if (r == null) {
                                context.fail("No such database " + TEST_DB);
                            } else {
                                context.assertTrue(r.next());
                                context.assertEquals(1, r.getInt(1));
                                context.assertFalse(r.next());
                            }

                            async.complete();
                        }
                    } catch (SQLException e) {
                        context.fail(e);
                    }
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
                    Connection c = (Connection) cc;

                    try (Statement s = c.createStatement()) {
                        s.execute("CREATE DATABASE " + TEST_DB);
                    } catch (SQLException e) {
                        context.fail(e);
                    }

                    db.dropDatabase(TEST_DB)
                            .thenCompose(r -> db.getConnection())
                            .thenAccept(c2 -> {
                                try (Connection cc2 = (Connection) c2;
                                     Statement s = cc2.createStatement()) {
                                    try (ResultSet rs = s.executeQuery("SELECT 1 FROM pg_database WHERE datname = '" + TEST_DB + "'")) {
                                        context.assertFalse(rs.next());
                                    }
                                } catch (SQLException e) {
                                    context.fail(e);
                                }

                                async.complete();
                            })
                            .exceptionally(ex -> {
                                context.fail(ex);
                                async.complete();
                                return null;
                            });
                })
                .exceptionally(ex -> {
                    context.fail(ex);
                    async.complete();
                    return null;
                });
    }
}
