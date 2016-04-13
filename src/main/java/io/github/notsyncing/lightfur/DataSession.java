package io.github.notsyncing.lightfur;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class DataSession
{
    private DatabaseManager mgr;
    private SQLConnection conn = null;
    private CompletableFuture<Void> connFuture;

    public DataSession()
    {
        mgr = DatabaseManager.getInstance();
        connFuture = mgr.getConnection().thenAccept(c -> conn = c);
    }

    private CompletableFuture<SQLConnection> ensureConnection()
    {
        if (connFuture.isDone()) {
            return CompletableFuture.completedFuture(conn);
        } else {
            return connFuture.thenApply(o -> conn);
        }
    }

    private CompletableFuture<Void> setAutoCommit(boolean autoCommit)
    {
        return ensureConnection().thenCompose(c -> {
            CompletableFuture<Void> f = new CompletableFuture<>();

            c.setAutoCommit(autoCommit, r -> {
                if (r.succeeded()) {
                    f.complete(r.result());
                } else {
                    f.completeExceptionally(r.cause());
                }
            });

            return f;
        });
    }

    public CompletableFuture beginTransaction()
    {
        return setAutoCommit(false);
    }

    private CompletableFuture<Void> commitCore()
    {
        return ensureConnection().thenCompose(c -> {
            CompletableFuture<Void> f = new CompletableFuture<>();

            c.commit(r -> {
                if (r.succeeded()) {
                    f.complete(r.result());
                } else {
                    f.completeExceptionally(r.cause());
                }
            });

            return f;
        });
    }

    public CompletableFuture<Void> commit(boolean endTransaction)
    {
        return commitCore().thenCompose(o -> {
            if (endTransaction) {
                return setAutoCommit(true);
            } else {
                return CompletableFuture.completedFuture(o);
            }
        });
    }

    public CompletableFuture commit()
    {
        return commit(true);
    }

    private CompletableFuture<Void> rollbackCore()
    {
        return ensureConnection().thenCompose(c -> {
            CompletableFuture<Void> f = new CompletableFuture<>();

            c.rollback(r -> {
                if (r.succeeded()) {
                    f.complete(r.result());
                } else {
                    f.completeExceptionally(r.cause());
                }
            });

            return f;
        });
    }

    public CompletableFuture<Void> rollback(boolean endTransaction)
    {
        return rollbackCore().thenCompose(o -> {
            if (endTransaction) {
                return setAutoCommit(true);
            } else {
                return CompletableFuture.completedFuture(o);
            }
        });
    }

    public CompletableFuture<Void> rollback()
    {
        return rollback(true);
    }

    public CompletableFuture<UpdateResult> execute(String sql, JsonArray params)
    {
        return ensureConnection().thenCompose(c -> {
            CompletableFuture<UpdateResult> f = new CompletableFuture<>();

            c.updateWithParams(sql, params, r -> {
                if (r.succeeded()) {
                    f.complete(r.result());
                } else {
                    f.completeExceptionally(r.cause());
                }
            });

            return f;
        });
    }

    public CompletableFuture<ResultSet> query(String sql, JsonArray params)
    {
        return ensureConnection().thenCompose(c -> {
            CompletableFuture<ResultSet> f = new CompletableFuture<>();

            c.queryWithParams(sql, params, r -> {
                if (r.succeeded()) {
                    f.complete(r.result());
                } else {
                    f.completeExceptionally(r.cause());
                }
            });

            return f;
        });
    }

    public void end()
    {
        conn.close();
    }
}
