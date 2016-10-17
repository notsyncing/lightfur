package io.github.notsyncing.lightfur;

import io.github.notsyncing.lightfur.entity.DataMapper;
import io.github.notsyncing.lightfur.entity.ReflectDataMapper;
import io.github.notsyncing.lightfur.models.PageResult;
import io.github.notsyncing.lightfur.utils.FutureUtils;
import io.github.notsyncing.lightfur.utils.PageUtils;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.security.InvalidParameterException;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 数据操作会话类
 */
public class DataSession
{
    private DatabaseManager mgr;
    private SQLConnection conn = null;
    private CompletableFuture<Void> connFuture;
    private boolean inTransaction = false;
    private DataMapper dataMapper;
    private boolean ended = false;

    /**
     * 实例化一个数据操作会话
     */
    public DataSession()
    {
        this(new ReflectDataMapper());
    }

    public DataSession(DataMapper dataMapper)
    {
        this.dataMapper = dataMapper;

        mgr = DatabaseManager.getInstance();
        connFuture = mgr.getConnection().thenAccept(c -> conn = c);
    }

    public boolean isInTransaction()
    {
        return inTransaction;
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
                    inTransaction = !autoCommit;
                    f.complete(r.result());
                } else {
                    r.cause().printStackTrace();
                    f.completeExceptionally(r.cause());
                }
            });

            return f;
        });
    }

    /**
     * 异步开始一个数据库事务
     * @return 指示事务是否已开始的 CompletableFuture 对象
     */
    public CompletableFuture<Void> beginTransaction()
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
                    r.cause().printStackTrace();
                    f.completeExceptionally(r.cause());
                }
            });

            return f;
        });
    }

    /**
     * 异步提交当前数据库事务
     * 应先调用 {@link DataSession#beginTransaction} 来开始一个事务
     * @param endTransaction 提交后是否结束事务
     * @return 指示提交是否已完成的 CompletableFuture 对象
     */
    public CompletableFuture<Void> commit(boolean endTransaction)
    {
        if (endTransaction) {
            return setAutoCommit(true);
        } else {
            return commitCore();
        }
    }

    /**
     * 异步提交当前数据库事务，并结束当前事务
     * 应先调用 {@link DataSession#beginTransaction} 来开始一个事务
     * @return 指示提交和结束事务是否已完成的 CompletableFuture 对象
     */
    public CompletableFuture<Void> commit()
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
                    r.cause().printStackTrace();
                    f.completeExceptionally(r.cause());
                }
            });

            return f;
        });
    }

    /**
     * 异步回滚当前数据库事务
     * 应先调用 {@link DataSession#beginTransaction} 来开始一个事务
     * @param endTransaction 回滚后是否结束事务
     * @return 指示回滚是否已完成的 CompletableFuture 对象
     */
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

    /**
     * 异步回滚当前数据库事务，并结束当前事务
     * 应先调用 {@link DataSession#beginTransaction} 来开始一个事务
     * @return 指示回滚和结束事务是否已完成的 CompletableFuture 对象
     */
    public CompletableFuture<Void> rollback()
    {
        return rollback(true);
    }

    /**
     * 异步执行一条 SQL 语句
     * @param sql 要执行的 SQL 语句
     * @param params 该语句的参数列表
     * @return 包含执行结果的 CompletableFuture 对象
     */
    public CompletableFuture<UpdateResult> execute(String sql, JsonArray params)
    {
        return ensureConnection().thenCompose(c -> {
            CompletableFuture<UpdateResult> f = new CompletableFuture<>();

            c.updateWithParams(sql, params, r -> {
                if (r.succeeded()) {
                    f.complete(r.result());
                } else {
                    System.out.println("Error occured when executing SQL: " + sql + " (" + params + ")");

                    r.cause().printStackTrace();
                    f.completeExceptionally(r.cause());
                }
            });

            return f;
        });
    }

    /**
     * 异步执行一条 SQL 语句
     * @param sql 要执行的 SQL 语句
     * @param params 该语句的参数列表
     * @return 包含执行结果的 CompletableFuture 对象
     */
    public CompletableFuture<UpdateResult> execute(String sql, Object... params)
    {
        return execute(sql, objectsToJsonArray(params));
    }

    /**
     * 异步执行一条 SQL 语句，并获取返回的列
     * @param sql 要执行的 SQL 语句
     * @param params 该语句的参数列表
     * @return 包含执行结果的 CompletableFuture 对象
     */
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

    /**
     * 异步执行一条 SQL 查询语句
     * @param sql 要执行的 SQL 查询语句
     * @param params 该语句的参数列表
     * @return 包含查询结果集的 CompletableFuture 对象
     */
    public CompletableFuture<ResultSet> query(String sql, JsonArray params)
    {
        return ensureConnection().thenCompose(c -> {
            CompletableFuture<ResultSet> f = new CompletableFuture<>();

            try {
                c.queryWithParams(sql, params, r -> {
                    if (r.succeeded()) {
                        f.complete(r.result());
                    } else {
                        System.out.println("Error occured when querying SQL: " + sql + " (" + params + ")");

                        r.cause().printStackTrace();
                        f.completeExceptionally(r.cause());
                    }
                });
            } catch (Exception e) {
                System.out.println("Error occured when querying SQL: " + sql + " (" + params + ")");
                e.printStackTrace();
                f.completeExceptionally(e);
            }

            return f;
        });
    }

    /**
     * 异步执行一条 SQL 查询语句
     * @param sql 要执行的 SQL 查询语句
     * @param params 该语句的参数列表
     * @return 包含查询结果集的 CompletableFuture 对象
     */
    public CompletableFuture<ResultSet> query(String sql, Object... params)
    {
        return query(sql, objectsToJsonArray(params));
    }

    /**
     * 结束当前数据会话，并关闭数据库连接
     * 在执行此函数之后，当前数据会话对象上的所有后续操作将失败
     * 要再次操作，请调用 {@link DataSession#DataSession()} 开始一个新的数据会话
     * @return 指示是否关闭连接的 CompletableFuture 对象
     */
    public CompletableFuture<Void> end()
    {
        if (ended) {
            return CompletableFuture.completedFuture(null);
        }

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

    /**
     * 异步执行一条 SQL 查询语句，并将第一条查询结果映射到指定类型的类/实体
     * @param clazz 指定的类/实体的类型
     * @param sql 要执行的 SQL 查询语句
     * @param params 该语句的参数列表
     * @param <T> 指定的类/实体的类型
     * @return 包含指定类型的，已实例化的，并根据结果集的第一行填充其字段的类/实体对象的 CompletableFuture 对象
     */
    public <T> CompletableFuture<T> queryFirst(Class<T> clazz, String sql, Object... params)
    {
        return query(sql, params).thenCompose(r -> {
            try {
                return CompletableFuture.completedFuture(dataMapper.map(clazz, r));
            } catch (Exception e) {
                CompletableFuture<T> f = new CompletableFuture<>();
                f.completeExceptionally(e);
                return f;
            }
        });
    }

    /**
     * 异步执行一条 SQL 查询语句，并将整个查询结果集映射到指定类型的类/实体列表上
     * @param clazz 指定的类/实体的类型
     * @param sql 要执行的 SQL 查询语句
     * @param params 该语句的参数列表
     * @param <T> 指定的类/实体的类型
     * @return 包含指定类型的，已实例化的，并根据结果集的每一行填充其字段的类/实体对象的列表的 CompletableFuture 对象
     */
    public <T> CompletableFuture<List<T>> queryList(Class<T> clazz, String sql, Object... params)
    {
        return query(sql, params).thenCompose(r -> {
            try {
                return CompletableFuture.completedFuture(dataMapper.mapToList(clazz, r));
            } catch (Exception e) {
                CompletableFuture<List<T>> f = new CompletableFuture<>();
                f.completeExceptionally(e);
                return f;
            }
        });
    }

    /**
     * 异步执行一条 SQL 查询语句，并将查询结果集分页以列表形式返回
     * 本函数会先执行一条 COUNT 查询，并且不在内部使用事务
     * @param clazz 指定的类/实体的类型
     * @param pageNum 要查询的页数，从 0 开始
     * @param pageSize 每页最大条目数量
     * @param sql 要执行的 SQL 查询语句
     * @param params 该语句的参数列表
     * @param <T> 指定的类/实体的类型
     * @return 包含总页数、总条目数及列表的 CompletableFuture 对象
     */
    public <T> CompletableFuture<PageResult<T>> queryListPaged(Class<T> clazz, int pageNum, int pageSize, String sql,
                                                               Object... params)
    {
        int begin = sql.indexOf("SELECT");
        int end = sql.indexOf("FROM", begin);

        if (begin < 0) {
            return FutureUtils.failed(new InvalidParameterException("SQL " + sql + " does not contain a SELECT!"));
        }

        if (end < 0) {
            return FutureUtils.failed(new InvalidParameterException("SQL " + sql + " does not contain a FROM after SELECT!"));
        }

        String countSql = sql.indexOf(0, begin) + "SELECT COUNT(*) " + sql.indexOf(end);

        PageResult<T> result = new PageResult<>();
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);

        return queryFirstValue(countSql, params)
                .thenCompose(r -> {
                    int totalCount = (int)r;
                    String limitSql = sql + " LIMIT " + pageSize + " OFFSET " + (pageNum * pageSize);

                    result.setTotalCount(totalCount);
                    result.setPageCount(PageUtils.calculatePageCount(pageSize, totalCount));

                    return queryList(clazz, limitSql, params);
                })
                .thenApply(r -> {
                    result.setList(r);
                    return result;
                });
    }

    /**
     * 异步执行一条 SQL 查询语句，并返回查询结果第一行第一列的值
     * @param sql 要执行的 SQL 查询语句
     * @param params 该语句的参数列表
     * @return 包含查询结果第一行第一列的值的 CompletableFuture 对象
     */
    public CompletableFuture<Object> queryFirstValue(String sql, Object... params)
    {
        return query(sql, params).thenApply(r -> r.getNumRows() > 0 ? r.getResults().get(0).getValue(0) : null);
    }
}
