package io.github.notsyncing.lightfur;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.PostgreSQLClient;
import io.vertx.ext.sql.SQLConnection;

import java.util.concurrent.CompletableFuture;

public class DatabaseManager
{
    private static DatabaseManager instance = new DatabaseManager();

    private Vertx vertx;
    private AsyncSQLClient client;
    private JsonObject configs;

    private DatabaseManager()
    {
        VertxOptions opts = new VertxOptions()
                .setBlockedThreadCheckInterval(60 * 60 * 1000);

        vertx = Vertx.vertx(opts);
    }

    public static DatabaseManager getInstance()
    {
        return instance;
    }

    public void init(String host, int port, String username, String password, String databaseName)
    {
        configs = new JsonObject()
                .put("host", host)
                .put("port", port)
                .put("username", username)
                .put("password", password)
                .put("database", databaseName);

        client = PostgreSQLClient.createNonShared(vertx, configs);
    }

    public void init(String username, String password, String databaseName)
    {
        init("localhost", 5432, username, password, databaseName);
    }

    public void init(String databaseName)
    {
        init("postgres", null, databaseName);
    }

    public CompletableFuture<SQLConnection> getConnection()
    {
        CompletableFuture<SQLConnection> future = new CompletableFuture<>();

        client.getConnection(r -> {
            if (r.succeeded()) {
                future.complete(r.result());
            } else {
                r.cause().printStackTrace();
                future.completeExceptionally(r.cause());
            }
        });

        return future;
    }

    public CompletableFuture<Void> createDatabase(String databaseName, boolean switchTo)
    {
        CompletableFuture<Void> f = new CompletableFuture<>();

        client.getConnection(r -> {
            if (!r.succeeded()) {
                f.completeExceptionally(r.cause());
                return;
            }

            SQLConnection c = r.result();
            c.execute("CREATE DATABASE \"" + databaseName + "\"", r2 -> {
                c.close();

                if (!r2.succeeded()) {
                    f.completeExceptionally(r2.cause());
                    return;
                }

                if (switchTo) {
                    setDatabase(databaseName);
                }

                f.complete(r2.result());
            });
        });

        return f;
    }

    public CompletableFuture<Void> dropDatabase(String databaseName)
    {
        setDatabase("postgres");

        CompletableFuture<Void> f = new CompletableFuture<>();

        client.getConnection(r -> {
            if (!r.succeeded()) {
                f.completeExceptionally(r.cause());
                return;
            }

            SQLConnection c = r.result();
            c.execute("DROP DATABASE \"" + databaseName + "\"", r2 -> {
                c.close();

                if (!r2.succeeded()) {
                    f.completeExceptionally(r2.cause());
                    return;
                }

                f.complete(r2.result());
            });
        });

        return f;
    }

    public void setDatabase(String databaseName)
    {
        configs.put("database", databaseName);
        client = PostgreSQLClient.createNonShared(vertx, configs);
    }
}
