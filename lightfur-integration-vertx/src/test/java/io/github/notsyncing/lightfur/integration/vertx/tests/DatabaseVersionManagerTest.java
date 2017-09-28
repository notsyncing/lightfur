package io.github.notsyncing.lightfur.integration.vertx.tests;

import io.github.notsyncing.lightfur.DataSession;
import io.github.notsyncing.lightfur.DatabaseManager;
import io.github.notsyncing.lightfur.common.LightfurConfig;
import io.github.notsyncing.lightfur.common.LightfurConfigBuilder;
import io.github.notsyncing.lightfur.integration.vertx.VertxDataSession;
import io.github.notsyncing.lightfur.integration.vertx.VertxPostgreSQLDriver;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RunWith(VertxUnitRunner.class)
public class DatabaseVersionManagerTest
{
    private DatabaseManager db;
    private static final String TEST_DB = "lightfur_upgrade_test";

    private LightfurConfig config = new LightfurConfigBuilder()
            .database("postgres")
            .username("postgres")
            .password(null)
            .host("localhost")
            .port(5432)
            .maxPoolSize(10)
            .databaseVersioning(true)
            .build();

    private LightfurConfig configTest = new LightfurConfigBuilder()
            .database(TEST_DB)
            .username("postgres")
            .password(null)
            .host("localhost")
            .port(5432)
            .maxPoolSize(10)
            .databaseVersioning(true)
            .build();

    @Before
    public void setUp(TestContext context)
    {
        DatabaseManager.setDriver(new VertxPostgreSQLDriver());
        DataSession.setCreator(() -> new VertxDataSession());

        Async async = context.async();

        db = DatabaseManager.getInstance();

        db.init(config).thenCompose(r -> db.dropDatabase(TEST_DB, true))
                .thenCompose(r -> db.close())
                .thenAccept(r -> async.complete())
                .exceptionally(ex -> {
                    context.fail((Throwable)ex);
                    return null;
                });
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
                    return null;
                });
    }

    @Test
    public void testUpdateDatabaseFromBase(TestContext context)
    {
        Async async = context.async();

        db = DatabaseManager.getInstance();

        db.init(configTest)
                .thenCompose(r -> db.getConnection())
                .thenAccept(cc -> {
                    SQLConnection c = (SQLConnection) cc;

                    c.query("SELECT 1 FROM pg_database WHERE datname = '" + TEST_DB + "'", result -> {
                        if (result.failed()) {
                            c.close();
                            context.fail(result.cause());
                            return;
                        }

                        ResultSet data = result.result();
                        context.assertEquals(1, data.getNumRows());
                        context.assertEquals(1, data.getResults().get(0).getInteger(0));

                        c.query("SELECT column_name FROM information_schema.columns WHERE table_name = 'test' AND table_catalog = '" + TEST_DB + "'", r -> {
                            if (r.failed()) {
                                c.close();
                                context.fail(r.cause());
                                return;
                            }

                            ResultSet data2 = r.result();
                            List<String> columns = data2.getRows().stream()
                                    .map(o -> o.getString("column_name"))
                                    .collect(Collectors.toList());

                            context.assertEquals(5, columns.size());
                            context.assertTrue(columns.contains("id"));
                            context.assertTrue(columns.contains("name"));
                            context.assertTrue(columns.contains("flag"));
                            context.assertTrue(columns.contains("last_date"));
                            context.assertTrue(columns.contains("details"));

                            c.query("SELECT data::text FROM lightfur.version_data", r2 -> {
                                if (r2.failed()) {
                                    c.close();
                                    context.fail(r2.cause());
                                    return;
                                }

                                String s = r2.result().getRows().get(0).getString("data");
                                JsonObject d = new JsonObject(s);

                                context.assertTrue(d.containsKey("lightfur.test"));
                                context.assertEquals(2, d.getJsonObject("lightfur.test").getInteger("version"));

                                c.close(h -> {
                                    if (h.failed()) {
                                        context.fail(h.cause());
                                    }

                                    async.complete();
                                });
                            });
                        });
                    });
                })
                .exceptionally(ex -> {
                    context.fail(ex);
                    async.complete();
                    return null;
                });
    }

    @Test
    public void testUpdateDatabaseFromVersion(TestContext context)
    {
        Async async = context.async();

        db = DatabaseManager.getInstance();

        db.init(config)
                .thenCompose(r -> db.createDatabase(TEST_DB, true))
                .thenCompose(r -> db.getConnection())
                .thenCompose(cc -> {
                    SQLConnection c = (SQLConnection) cc;

                    CompletableFuture<Void> f = new CompletableFuture<>();

                    String sql = "CREATE SCHEMA lightfur;\n" +
                            "CREATE TABLE lightfur.version_data (data JSONB);\n" +
                            "INSERT INTO lightfur.version_data (data) VALUES ('{\"lightfur.test\":{\"version\":1}}');\n" +
                            "CREATE TABLE test (id SERIAL);";

                    c.query(sql, r -> {
                        if (r.failed()) {
                            context.fail(r.cause());
                            f.completeExceptionally(r.cause());
                            return;
                        }

                        c.close();

                        f.complete(null);
                    });

                    return f;
                })
                .thenCompose(r -> db.close())
                .thenCompose(r -> {
                    config.setDatabase(TEST_DB);
                    config.setEnableDatabaseVersioning(true);

                    return db.init(config);
                })
                .thenCompose(r -> db.getConnection())
                .thenAccept(cc -> {
                    SQLConnection c = (SQLConnection) cc;

                    c.query("SELECT 1 FROM pg_database WHERE datname = '" + TEST_DB + "'", result -> {
                        if (result.failed()) {
                            c.close();
                            context.fail(result.cause());
                            return;
                        }

                        ResultSet data = result.result();
                        context.assertEquals(1, data.getNumRows());
                        context.assertEquals(1, data.getResults().get(0).getInteger(0));

                        c.query("SELECT column_name FROM information_schema.columns WHERE table_name = 'test' AND table_catalog = '" + TEST_DB + "'", r -> {
                            if (r.failed()) {
                                c.close();
                                context.fail(r.cause());
                                return;
                            }

                            ResultSet data2 = r.result();
                            List<String> columns = data2.getRows().stream()
                                    .map(o -> o.getString("column_name"))
                                    .collect(Collectors.toList());

                            context.assertEquals(2, columns.size());
                            context.assertTrue(columns.contains("id"));
                            context.assertTrue(columns.contains("details"));

                            c.query("SELECT data::text FROM lightfur.version_data", r2 -> {
                                if (r2.failed()) {
                                    c.close();
                                    context.fail(r2.cause());
                                    return;
                                }

                                String s = r2.result().getRows().get(0).getString("data");
                                JsonObject d = new JsonObject(s);

                                context.assertTrue(d.containsKey("lightfur.test"));
                                context.assertEquals(2, d.getJsonObject("lightfur.test").getInteger("version"));

                                c.close(h -> {
                                    if (h.failed()) {
                                        context.fail(h.cause());
                                    }

                                    async.complete();
                                });
                            });
                        });
                    });
                })
                .exceptionally(ex -> {
                    context.fail(ex);
                    async.complete();
                    return null;
                });
    }

    @Test
    public void testUpdateDatabaseFromBaseWithFullVersion(TestContext context)
    {
        Async async = context.async();

        db = DatabaseManager.getInstance();

        db.init(configTest)
                .thenCompose(r -> db.getConnection())
                .thenAccept(cc -> {
                    SQLConnection c = (SQLConnection) cc;

                    c.query("SELECT 1 FROM pg_database WHERE datname = '" + TEST_DB + "'", result -> {
                        if (result.failed()) {
                            c.close();
                            context.fail(result.cause());
                            return;
                        }

                        ResultSet data = result.result();
                        context.assertEquals(1, data.getNumRows());
                        context.assertEquals(1, data.getResults().get(0).getInteger(0));

                        c.query("SELECT column_name FROM information_schema.columns WHERE table_name = 'test_full' AND table_catalog = '" + TEST_DB + "'", r -> {
                            if (r.failed()) {
                                c.close();
                                context.fail(r.cause());
                                return;
                            }

                            ResultSet data2 = r.result();
                            List<String> columns = data2.getRows().stream()
                                    .map(o -> o.getString("column_name"))
                                    .collect(Collectors.toList());

                            context.assertEquals(5, columns.size());
                            context.assertTrue(columns.contains("id"));
                            context.assertTrue(columns.contains("name"));
                            context.assertTrue(columns.contains("flag"));
                            context.assertTrue(columns.contains("last_date"));
                            context.assertTrue(columns.contains("details"));

                            c.query("SELECT data::text FROM lightfur.version_data", r2 -> {
                                if (r2.failed()) {
                                    c.close();
                                    context.fail(r2.cause());
                                    return;
                                }

                                String s = r2.result().getRows().get(0).getString("data");
                                JsonObject d = new JsonObject(s);

                                context.assertTrue(d.containsKey("lightfur.test_full"));
                                context.assertEquals(2, d.getJsonObject("lightfur.test_full").getInteger("version"));

                                c.close(h -> {
                                    if (h.failed()) {
                                        context.fail(h.cause());
                                    }

                                    async.complete();
                                });
                            });
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
