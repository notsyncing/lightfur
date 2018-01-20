package io.github.notsyncing.lightfur.integration.vertx;

import com.alibaba.fastjson.JSONObject;
import io.github.notsyncing.lightfur.core.DataSession;
import io.github.notsyncing.lightfur.core.models.ExecutionResult;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class VertxDataSession extends DataSession<SQLConnection, ResultSet, UpdateResult>
{
    public VertxDataSession() {
        this(new Exception("Vertx data session started here"));
    }

    public VertxDataSession(Exception createStack)
    {
        this(new ReflectDataMapper(), createStack);
    }

    public VertxDataSession(VertxDataMapper dataMapper, Exception createStack)
    {
        super(dataMapper, createStack);
    }

    protected CompletableFuture<Void> setAutoCommit(boolean autoCommit)
    {
        return ensureConnection().thenCompose(c -> {
            CompletableFuture<Void> f = new CompletableFuture<>();

            c.setAutoCommit(autoCommit, r -> {
                if (r.succeeded()) {
                    super.setAutoCommit(autoCommit)
                            .thenAccept(r2 -> f.complete(r.result()));
                } else {
                    r.cause().printStackTrace();
                    f.completeExceptionally(r.cause());
                }
            });

            return f;
        });
    }

    @Override
    protected CompletableFuture<Void> _commit()
    {
        return ensureConnection().thenCompose(c -> {
            CompletableFuture<Void> f = new CompletableFuture<>();

            c.commit(r -> {
                if (r.succeeded()) {
                    f.complete(r.result());
                } else {
                    r.cause().printStackTrace();
                    f.completeExceptionally(r.cause());
                }
            });

            return f;
        });
    }

    @Override
    protected CompletableFuture<Void> _rollback()
    {
        return ensureConnection().thenCompose(c -> {
            CompletableFuture<Void> f = new CompletableFuture<>();

            c.rollback(r -> {
                if (r.succeeded()) {
                    f.complete(r.result());
                } else {
                    r.cause().printStackTrace();
                    f.completeExceptionally(r.cause());
                }
            });

            return f;
        });
    }

    private CompletableFuture<UpdateResult> execute(String sql, JsonArray params)
    {
        Exception ex = new Exception();
        setLastQuery(sql);

        return ensureConnection().thenCompose(c -> {
            CompletableFuture<UpdateResult> f = new CompletableFuture<>();

            c.updateWithParams(sql, params, r -> {
                if (r.succeeded()) {
                    f.complete(r.result());
                } else {
                    log.warning("Error occured when executing SQL: " + sql + " (" + params + ")");

                    ex.initCause(r.cause());

                    ex.printStackTrace();
                    f.completeExceptionally(ex);
                }
            });

            return f;
        });
    }

    @Override
    public CompletableFuture<UpdateResult> executeWithoutPreparing(String sql)
    {
        Exception ex = new Exception();
        setLastQuery(sql);

        return ensureConnection().thenCompose(c -> {
            CompletableFuture<UpdateResult> f = new CompletableFuture<>();

            c.update(sql, r -> {
                if (r.succeeded()) {
                    f.complete(r.result());
                } else {
                    log.warning("Error occured when executing SQL: " + sql);

                    ex.initCause(r.cause());

                    ex.printStackTrace();
                    f.completeExceptionally(ex);
                }
            });

            return f;
        });
    }

    @Override
    public CompletableFuture<ExecutionResult> updateWithoutPreparing(String sql) {
        return executeWithoutPreparing(sql)
                .thenApply(u -> new ExecutionResult(u.getUpdated()));
    }

    @Override
    public CompletableFuture<ExecutionResult> update(String sql, Object... params) {
        return execute(sql, params)
                .thenApply(u -> new ExecutionResult(u.getUpdated()));
    }

    @Override
    public CompletableFuture<UpdateResult> execute(String sql, Object... params)
    {
        return execute(sql, objectsToJsonArray(params));
    }

    @Override
    public CompletableFuture<ResultSet> executeWithReturning(String sql, Object... params)
    {
        return query(sql, objectsToJsonArray(params));
    }

    private JsonArray objectsToJsonArray(Object[] params)
    {
        JsonArray arr = new JsonArray();

        if (params != null) {
            for (Object o : params) {
                if (o == null) {
                    arr.addNull();
                } else {
                    if (o instanceof Enum) {
                        arr.add(((Enum)o).ordinal());
                    } else if (o.getClass().isArray()) {
                        JsonArray a = new JsonArray();

                        for (int i = 0; i < Array.getLength(o); i++) {
                            Object item = Array.get(o, i);

                            if (item.getClass().isEnum()) {
                                a.add(((Enum)item).ordinal());
                            } else {
                                a.add(item);
                            }
                        }

                        arr.add(a);
                    } else if (o instanceof BigDecimal) {
                        arr.add(((BigDecimal) o).toPlainString());
                    } else if (o instanceof Temporal) {
                        arr.add(o.toString());
                    } else {
                        arr.add(o);
                    }
                }
            }
        }

        return arr;
    }

    private CompletableFuture<ResultSet> query(String sql, JsonArray params)
    {
        Exception ex = new Exception();
        setLastQuery(sql);

        return ensureConnection().thenCompose(c -> {
            CompletableFuture<ResultSet> f = new CompletableFuture<>();

            try {
                c.queryWithParams(sql, params, r -> {
                    if (r.succeeded()) {
                        f.complete(r.result());
                    } else {
                        log.warning("Error occured when querying SQL: " + sql + " (" + params + ")");

                        ex.initCause(r.cause());

                        ex.printStackTrace();
                        f.completeExceptionally(ex);
                    }
                });
            } catch (Exception e) {
                log.warning("Error occured when querying SQL: " + sql + " (" + params + ")");
                ex.initCause(e);
                ex.printStackTrace();
                f.completeExceptionally(ex);
            }

            return f;
        });
    }

    @Override
    public CompletableFuture<ResultSet> query(String sql, Object... params)
    {
        return query(sql, objectsToJsonArray(params));
    }

    @Override
    public CompletableFuture<List<JSONObject>> queryJson(String sql, Object... params) {
        return query(sql, params)
                .thenApply(r -> r.getRows().stream()
                        .map(row -> JSONObject.parseObject(row.encode()))
                        .collect(Collectors.toList()));
    }

    @Override
    protected CompletableFuture<Void> _end()
    {
        CompletableFuture<Void> f = new CompletableFuture<>();

        conn.setAutoCommit(false, r -> {
            if (r.failed()) {
                f.completeExceptionally(r.cause());
                return;
            }

            conn.close(r2 -> {
                if (r2.failed()) {
                    f.completeExceptionally(r2.cause());
                } else {
                    ended = true;
                    f.complete(r2.result());
                }
            });
        });

        return f;
    }

    @Override
    public CompletableFuture<Object> queryFirstValue(String sql, Object... params)
    {
        return query(sql, params).thenApply(r -> r.getNumRows() > 0 ? r.getResults().get(0).getValue(0) : null);
    }
}
