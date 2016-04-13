package io.github.notsyncing.lightfur;

import io.vertx.core.Vertx;
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

    private DatabaseManager()
    {
        vertx = Vertx.vertx();
    }

    public static DatabaseManager getInstance()
    {
        return instance;
    }

    public void init(String host, int port, String username, String password)
    {
        JsonObject configs = new JsonObject()
                .put("host", host)
                .put("port", port)
                .put("username", username)
                .put("password", password);

        client = PostgreSQLClient.createNonShared(vertx, configs);
    }

    public void init(String username, String password)
    {
        init("localhost", 3306, username, password);
    }

    public void init()
    {
        init("postgres", "123456");
    }

    public CompletableFuture<SQLConnection> getConnection()
    {
        CompletableFuture<SQLConnection> future = new CompletableFuture<>();

        client.getConnection(r -> {
            if (r.succeeded()) {
                future.complete(r.result());
            } else {
                future.completeExceptionally(r.cause());
            }
        });

        return future;
    }
}
