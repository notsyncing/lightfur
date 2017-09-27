package io.github.notsyncing.lightfur.integration.vertx;

import io.github.notsyncing.lightfur.DatabaseDriver;
import io.github.notsyncing.lightfur.common.LightfurConfig;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.PostgreSQLClient;
import io.vertx.ext.sql.SQLConnection;

import java.util.concurrent.CompletableFuture;

public class VertxPostgreSQLDriver extends DatabaseDriver<SQLConnection> {
    private Vertx vertx;
    private AsyncSQLClient client;

    private JsonObject toVertxConfig(LightfurConfig config) {
        return new JsonObject()
                .put("host", config.getHost())
                .put("port", config.getPort())
                .put("username", config.getUsername())
                .put("password", config.getPassword())
                .put("database", config.getDatabase())
                .put("maxPoolSize", config.getMaxPoolSize());
    }

    @Override
    public void init(LightfurConfig config) {
        VertxOptions opts = new VertxOptions()
                .setBlockedThreadCheckInterval(60 * 60 * 1000);

        vertx = Vertx.vertx(opts);

        client = PostgreSQLClient.createNonShared(vertx, toVertxConfig(config));
    }

    @Override
    public CompletableFuture<Void> close() {
        CompletableFuture<Void> f = new CompletableFuture<>();

        client.close(r -> {
            if (r.failed()) {
                f.completeExceptionally(r.cause());
                return;
            }

            vertx.close(r2 -> {
                if (r2.failed()) {
                    f.completeExceptionally(r2.cause());
                    return;
                }

                f.complete(r2.result());
            });
        });

        return f;
    }

    @Override
    public CompletableFuture<Void> recreate(LightfurConfig configs) {
        CompletableFuture<Void> f = new CompletableFuture<>();

        if (client != null) {
            client.close(r -> {
                if (!r.succeeded()) {
                    f.completeExceptionally(r.cause());
                    return;
                }

                f.complete(r.result());
            });
        } else {
            f.complete(null);
        }

        return f.thenAccept(r -> {
            client = PostgreSQLClient.createNonShared(vertx, toVertxConfig(configs));
        });
    }

    @Override
    public CompletableFuture<SQLConnection> getConnection() {
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

    @Override
    public CompletableFuture<Void> createDatabase(String name) {
        CompletableFuture<Void> f = new CompletableFuture<>();

        client.getConnection(r -> {
            if (!r.succeeded()) {
                f.completeExceptionally(r.cause());
                return;
            }

            SQLConnection c = r.result();
            c.execute("CREATE DATABASE \"" + name + "\"", r2 -> {
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

    @Override
    public CompletableFuture<Void> dropDatabase(String name, boolean ifExists) {
        CompletableFuture<Void> f = new CompletableFuture<>();

        client.getConnection(r -> {
            if (!r.succeeded()) {
                f.completeExceptionally(r.cause());
                return;
            }

            SQLConnection c = r.result();

            String sql = "DROP DATABASE";

            if (ifExists) {
                sql += " IF EXISTS";
            }

            c.execute(sql + " \"" + name + "\"", r2 -> {
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
}
