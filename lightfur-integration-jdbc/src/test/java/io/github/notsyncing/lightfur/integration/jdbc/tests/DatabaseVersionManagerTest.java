package io.github.notsyncing.lightfur.integration.jdbc.tests;

import io.github.notsyncing.lightfur.DataSession;
import io.github.notsyncing.lightfur.DatabaseManager;
import io.github.notsyncing.lightfur.common.LightfurConfig;
import io.github.notsyncing.lightfur.common.LightfurConfigBuilder;
import io.github.notsyncing.lightfur.integration.jdbc.JdbcDataSession;
import io.github.notsyncing.lightfur.integration.jdbc.JdbcPostgreSQLDriver;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
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
        DatabaseManager.setDriver(new JdbcPostgreSQLDriver());
        DataSession.setCreator(() -> new JdbcDataSession());

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
                    try {
                        Connection c = (Connection) cc;
                        Statement s = c.createStatement();

                        ResultSet data = s.executeQuery("SELECT 1 FROM pg_database WHERE datname = '" + TEST_DB + "'");

                        context.assertTrue(data.next());
                        context.assertEquals(1, data.getInt(1));
                        context.assertFalse(data.next());

                        data.close();

                        ResultSet data2 = s.executeQuery("SELECT column_name FROM information_schema.columns WHERE table_name = 'test' AND table_catalog = '" + TEST_DB + "'");
                        
                        List<String> columns = new ArrayList<>();
                        
                        while (data2.next()) {
                            columns.add(data2.getString("column_name"));
                        }

                        context.assertEquals(5, columns.size());
                        context.assertTrue(columns.contains("id"));
                        context.assertTrue(columns.contains("name"));
                        context.assertTrue(columns.contains("flag"));
                        context.assertTrue(columns.contains("last_date"));
                        context.assertTrue(columns.contains("details"));
                        
                        data2.close();

                        ResultSet data3 = s.executeQuery("SELECT data::text FROM lightfur.version_data");
                        
                        data3.next();
                        
                        String dataString = data3.getString("data");
                        JsonObject d = new JsonObject(dataString);

                        context.assertTrue(d.containsKey("lightfur.test"));
                        context.assertEquals(2, d.getJsonObject("lightfur.test").getInteger("version"));
                        
                        data3.close();

                        s.close();
                        c.close();
                    } catch (Exception e) {
                        context.fail(e);
                    }
                    
                    async.complete();
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
                .thenAccept(cc -> {
                    try {
                        Connection c = (Connection) cc;
                        Statement s = c.createStatement();

                        String sql = "CREATE SCHEMA lightfur;\n" +
                                "CREATE TABLE lightfur.version_data (data JSONB);\n" +
                                "INSERT INTO lightfur.version_data (data) VALUES ('{\"lightfur.test\":{\"version\":1}}');\n" +
                                "CREATE TABLE test (id SERIAL);";

                        s.execute(sql);
                        
                        s.close();
                        c.close();
                    } catch (Exception e) {
                        throw new CompletionException(e);
                    }
                })
                .thenCompose(r -> db.close())
                .thenCompose(r -> {
                    config.setDatabase(TEST_DB);
                    config.setEnableDatabaseVersioning(true);

                    return db.init(config);
                })
                .thenCompose(r -> db.getConnection())
                .thenAccept(cc -> {
                    try {
                        Connection c = (Connection) cc;
                        Statement s = c.createStatement();

                        ResultSet data = s.executeQuery("SELECT 1 FROM pg_database WHERE datname = '" + TEST_DB + "'");

                        context.assertTrue(data.next());
                        context.assertEquals(1, data.getInt(1));
                        context.assertFalse(data.next());
                        
                        data.close();

                        ResultSet data2 = s.executeQuery("SELECT column_name FROM information_schema.columns WHERE table_name = 'test' AND table_catalog = '" + TEST_DB + "'");

                        List<String> columns = new ArrayList<>();

                        while (data2.next()) {
                            columns.add(data2.getString("column_name"));
                        }
                        
                        data2.close();

                        context.assertEquals(2, columns.size());
                        context.assertTrue(columns.contains("id"));
                        context.assertTrue(columns.contains("details"));

                        ResultSet data3 = s.executeQuery("SELECT data::text FROM lightfur.version_data");
                        data3.next();
                        
                        String dataString = data3.getString("data");
                        JsonObject d = new JsonObject(dataString);

                        context.assertTrue(d.containsKey("lightfur.test"));
                        context.assertEquals(2, d.getJsonObject("lightfur.test").getInteger("version"));

                        data3.close();
                        s.close();
                        c.close();
                    } catch (Exception e) {
                        context.fail(e);
                    }
                    
                    async.complete();
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
                    try {
                        Connection c = (Connection) cc;
                        Statement s = c.createStatement();

                        ResultSet data = s.executeQuery("SELECT 1 FROM pg_database WHERE datname = '" + TEST_DB + "'");

                        context.assertTrue(data.next());
                        context.assertEquals(1, data.getInt(1));
                        context.assertFalse(data.next());

                        data.close();

                        ResultSet data2 = s.executeQuery("SELECT column_name FROM information_schema.columns WHERE table_name = 'test_full' AND table_catalog = '" + TEST_DB + "'");

                        List<String> columns = new ArrayList<>();

                        while (data2.next()) {
                            columns.add(data2.getString("column_name"));
                        }

                        data2.close();

                        context.assertEquals(5, columns.size());
                        context.assertTrue(columns.contains("id"));
                        context.assertTrue(columns.contains("name"));
                        context.assertTrue(columns.contains("flag"));
                        context.assertTrue(columns.contains("last_date"));
                        context.assertTrue(columns.contains("details"));

                        ResultSet data3 = s.executeQuery("SELECT data::text FROM lightfur.version_data");
                        data3.next();

                        String dataString = data3.getString("data");
                        JsonObject d = new JsonObject(dataString);

                        context.assertTrue(d.containsKey("lightfur.test_full"));
                        context.assertEquals(2, d.getJsonObject("lightfur.test_full").getInteger("version"));

                        data3.close();
                        s.close();
                        c.close();
                    } catch (Exception e) {
                        context.fail(e);
                    }
                    
                    async.complete();
                })
                .exceptionally(ex -> {
                    context.fail(ex);
                    async.complete();
                    return null;
                });
    }
}
