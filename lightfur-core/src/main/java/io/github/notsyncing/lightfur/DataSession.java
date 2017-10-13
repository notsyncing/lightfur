package io.github.notsyncing.lightfur;

import io.github.notsyncing.lightfur.entity.DataMapper;
import io.github.notsyncing.lightfur.models.ExecutionResult;
import io.github.notsyncing.lightfur.models.PageResult;
import io.github.notsyncing.lightfur.utils.FutureUtils;
import io.github.notsyncing.lightfur.utils.PageUtils;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * 数据操作会话类
 */
public abstract class DataSession<C, R, U>
{
    private static DataSessionCreator creator = null;

    private static ScheduledExecutorService leakChecker = Executors.newScheduledThreadPool(1, r -> {
        Thread thread = new Thread(r);
        thread.setName("lightfur-datasession-leak-checker");
        thread.setDaemon(true);
        return thread;
    });

    protected DatabaseManager mgr;
    protected C conn;
    protected CompletableFuture<Void> connFuture;
    protected boolean inTransaction = false;
    protected DataMapper<R> dataMapper;
    protected boolean ended = false;
    protected Logger log = Logger.getLogger(getClass().getSimpleName());

    private String lastQuery;
    private ScheduledFuture leakCheckingFuture;

    public DataSession(DataMapper<R> dataMapper)
    {
        this.dataMapper = dataMapper;

        mgr = DatabaseManager.getInstance();

        connFuture = mgr.getConnection().thenAccept(c -> {
            conn = (C) c;

            if (mgr.configs.isEnableDataSessionLeakChecking()) {
                leakCheckingFuture = leakChecker.schedule(() -> {
                    log.warning("DataSession " + this + " is still not ended after " +
                            mgr.configs.getDataSessionLeakCheckingInterval() + "ms, maybe leaked? " +
                            "Last query: " + lastQuery);
                }, mgr.configs.getDataSessionLeakCheckingInterval(), TimeUnit.MILLISECONDS);
            }
        });
    }

    public static void setCreator(DataSessionCreator creator) {
        DataSession.creator = creator;
    }

    protected void setLastQuery(String lastQuery) {
        this.lastQuery = lastQuery;
    }

    /**
     * 开始一个数据操作会话
     */
    public static <D extends DataSession> D start() {
        if (creator == null) {
            throw new RuntimeException("You must specify a DataSessionCreator!");
        }

        return (D) creator.create();
    }

    protected CompletableFuture<C> ensureConnection()
    {
        if (connFuture.isDone()) {
            return CompletableFuture.completedFuture(conn);
        } else {
            return connFuture.thenApply(o -> conn);
        }
    }

    public boolean isInTransaction()
    {
        return inTransaction;
    }

    protected CompletableFuture<Void> setAutoCommit(boolean autoCommit) {
        inTransaction = !autoCommit;

        return CompletableFuture.completedFuture(null);
    }

    /**
     * 异步开始一个数据库事务
     * @return 指示事务是否已开始的 CompletableFuture 对象
     */
    public CompletableFuture<Void> beginTransaction()
    {
        return setAutoCommit(false);
    }

    protected abstract CompletableFuture<Void> _commit();

    /**
     * 异步提交当前数据库事务
     * 应先调用 {@link DataSession#beginTransaction} 来开始一个事务
     * @param endTransaction 提交后是否结束事务
     * @return 指示提交是否已完成的 CompletableFuture 对象
     */
    public CompletableFuture<Void> commit(boolean endTransaction) {
        if (endTransaction) {
            return setAutoCommit(true);
        } else {
            return _commit();
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

    protected abstract CompletableFuture<Void> _rollback();

    /**
     * 异步回滚当前数据库事务
     * 应先调用 {@link DataSession#beginTransaction} 来开始一个事务
     * @param endTransaction 回滚后是否结束事务
     * @return 指示回滚是否已完成的 CompletableFuture 对象
     */
    public CompletableFuture<Void> rollback(boolean endTransaction) {
        return _rollback().thenCompose(o -> {
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
     * 异步执行一条 SQL 语句，不使用 PreparedStatement
     * @param sql 要执行的 SQL 语句
     * @return 包含执行结果的 CompletableFuture 对象
     */
    @Deprecated
    public abstract CompletableFuture<U> executeWithoutPreparing(String sql);

    public abstract CompletableFuture<ExecutionResult> updateWithoutPreparing(String sql);

    /**
     * 异步执行一条 SQL 语句
     * @param sql 要执行的 SQL 语句
     * @param params 该语句的参数列表
     * @return 包含执行结果的 CompletableFuture 对象
     */
    @Deprecated
    public abstract CompletableFuture<U> execute(String sql, Object... params);

    public abstract CompletableFuture<ExecutionResult> update(String sql, Object... params);

    /**
     * 异步执行一条 SQL 语句，并获取返回的列
     * @param sql 要执行的 SQL 语句
     * @param params 该语句的参数列表
     * @return 包含执行结果的 CompletableFuture 对象
     */
    public abstract CompletableFuture<R> executeWithReturning(String sql, Object... params);

    public CompletableFuture<Object> executeWithReturningFirst(String sql, Object... params) {
        return queryFirstValue(sql, params);
    }

    /**
     * 异步执行一条 SQL 查询语句
     * @param sql 要执行的 SQL 查询语句
     * @param params 该语句的参数列表
     * @return 包含查询结果集的 CompletableFuture 对象
     */
    public abstract CompletableFuture<R> query(String sql, Object... params);

    protected abstract CompletableFuture<Void> _end();

    /**
     * 结束当前数据会话，并关闭数据库连接
     * 在执行此函数之后，当前数据会话对象上的所有后续操作将失败
     * 要再次操作，请重新实例化一个 {@link DataSession} 以开始一个新的数据会话
     * @return 指示是否关闭连接的 CompletableFuture 对象
     */
    public CompletableFuture<Void> end() {
        if (ended) {
            if (leakCheckingFuture != null) {
                leakCheckingFuture.cancel(true);
            }

            return CompletableFuture.completedFuture(null);
        }

        if (conn == null) {
            if (leakCheckingFuture != null) {
                leakCheckingFuture.cancel(true);
            }

            return CompletableFuture.completedFuture(null);
        }

        return _end()
                .thenAccept(r -> {
                    if (leakCheckingFuture != null) {
                        leakCheckingFuture.cancel(true);
                    }
                });
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
    public abstract CompletableFuture<Object> queryFirstValue(String sql, Object... params);
}
