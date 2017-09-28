package io.github.notsyncing.lightfur.integration.jdbc;

import com.alibaba.fastjson.JSON;
import io.github.notsyncing.lightfur.DataSession;
import io.github.notsyncing.lightfur.entity.DataMapper;
import io.github.notsyncing.lightfur.models.ExecutionResult;
import jdk.nashorn.internal.scripts.JD;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class JdbcDataSession extends DataSession<Connection, ResultSet, ExecutionResult> {
    public JdbcDataSession() {
        this(new ReflectDataMapper());
    }

    public JdbcDataSession(JdbcDataMapper dataMapper) {
        super(dataMapper);
    }

    @Override
    protected CompletableFuture<Void> setAutoCommit(boolean autoCommit) {
        return ensureConnection()
                .thenCompose(c -> {
                    try {
                        c.setAutoCommit(autoCommit);

                        return super.setAutoCommit(autoCommit);
                    } catch (SQLException e) {
                        throw new CompletionException(e);
                    }
                });
    }

    @Override
    protected CompletableFuture<Void> _commit() {
        return ensureConnection()
                .thenAccept(r -> {
                    try {
                        conn.commit();
                    } catch (SQLException e) {
                        throw new CompletionException(e);
                    }
                });
    }

    @Override
    protected CompletableFuture<Void> _rollback() {
        return ensureConnection()
                .thenAccept(r -> {
                    try {
                        conn.rollback();
                    } catch (SQLException e) {
                        throw new CompletionException(e);
                    }
                });
    }

    @Override
    public CompletableFuture<ExecutionResult> executeWithoutPreparing(String sql) {
        Exception ex = new Exception();

        return ensureConnection().thenApply(c -> {
            try {
                Statement s = c.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                s.execute(sql);
                return new ExecutionResult(s.getUpdateCount());
            } catch (SQLException e) {
                log.warning("Error occured when executing SQL: " + sql);
                ex.initCause(e);
                throw new CompletionException(ex);
            }
        });
    }

    @Override
    public CompletableFuture<ExecutionResult> updateWithoutPreparing(String sql) {
        return executeWithoutPreparing(sql);
    }

    private String javaTypeToPostgreSQLType(Class<?> type) {
        if (type == String.class) {
            return "text";
        } else if ((type == Integer.class) || (type == int.class)) {
            return "integer";
        } else if ((type == Float.class) || (type == float.class)) {
            return "real";
        } else if ((type == Double.class) || (type == double.class)) {
            return "double precision";
        } else if ((type == Boolean.class) || (type == boolean.class)) {
            return "boolean";
        } else if ((type == Character.class) || (type == char.class)) {
            return "character";
        } else if (type == BigDecimal.class) {
            return "numeric";
        } else if ((type == Byte.class) || (type == byte.class)) {
            return "integer";
        } else if ((type == Short.class) || (type == short.class)) {
            return "smallint";
        } else if ((type == Long.class) || (type == long.class)) {
            return "long";
        }

        return "text";
    }

    private Object[] makeSureObjectArray(Object array) {
        if (!array.getClass().getComponentType().isPrimitive()) {
            return (Object[]) array;
        }

        int length = Array.getLength(array);
        Object[] result = new Object[length];

        for (int i = 0; i < length; i++) {
            result[i] = Array.get(array, i);
        }

        return result;
    }

    private void putParametersIntoPreparedStatement(PreparedStatement ps, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];

            if (param.getClass().isArray()) {
                param = ps.getConnection().createArrayOf(javaTypeToPostgreSQLType(param.getClass().getComponentType()),
                        makeSureObjectArray(param));
            } else if (param instanceof List) {
                param = ((List)param).toArray();
            }

            ps.setObject(i + 1, param);
        }
    }

    @Override
    public CompletableFuture<ExecutionResult> execute(String sql, Object... params) {
        Exception ex = new Exception();

        return ensureConnection().thenApply(c -> {
            try {
                PreparedStatement s = c.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                putParametersIntoPreparedStatement(s, params);
                s.execute();

                return new ExecutionResult(s.getUpdateCount());
            } catch (SQLException e) {
                log.warning("Error occured when executing SQL: " + sql);
                ex.initCause(e);
                throw new CompletionException(ex);
            }
        });
    }

    @Override
    public CompletableFuture<ExecutionResult> update(String sql, Object... params) {
        return execute(sql, params);
    }

    @Override
    public CompletableFuture<ResultSet> executeWithReturning(String sql, Object... params) {
        Exception ex = new Exception();

        return ensureConnection().thenApply(c -> {
            try {
                PreparedStatement s = c.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                putParametersIntoPreparedStatement(s, params);
                s.execute();

                return s.getResultSet();
            } catch (SQLException e) {
                log.warning("Error occured when executing SQL: " + sql);
                ex.initCause(e);
                throw new CompletionException(ex);
            }
        });
    }

    @Override
    public CompletableFuture<ResultSet> query(String sql, Object... params) {
        Exception ex = new Exception();

        return ensureConnection().thenApply(c -> {
            try {
                PreparedStatement s = c.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                putParametersIntoPreparedStatement(s, params);
                return s.executeQuery();
            } catch (SQLException e) {
                log.warning("Error occured when executing SQL: " + sql);
                ex.initCause(e);
                throw new CompletionException(ex);
            }
        });
    }

    @Override
    protected CompletableFuture<Void> _end() {
        return setAutoCommit(false)
                .thenAccept(r -> {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        throw new CompletionException(e);
                    }
                });
    }

    @Override
    public CompletableFuture<Object> queryFirstValue(String sql, Object... params) {
        return query(sql, params)
                .thenApply(r -> {
                    try {
                        if (r.next()) {
                            return r.getObject(1);
                        } else {
                            return null;
                        }
                    } catch (SQLException e) {
                        throw new CompletionException(e);
                    }
                });
    }
}
