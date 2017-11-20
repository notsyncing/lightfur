package io.github.notsyncing.lightfur.integration.jdbc;

import com.alibaba.fastjson.JSONObject;
import io.github.notsyncing.lightfur.DataSession;
import io.github.notsyncing.lightfur.models.ExecutionResult;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Logger;

public class JdbcDataSession extends DataSession<Connection, ResultSet, ExecutionResult> {
    private Logger log = Logger.getLogger(this.getClass().getSimpleName());

    public JdbcDataSession() {
        this(new Exception("Jdbc data session started here"));
    }

    public JdbcDataSession(Exception createStack) {
        this(new ReflectDataMapper(), createStack);
    }

    public JdbcDataSession(JdbcDataMapper dataMapper, Exception createStack) {
        super(dataMapper, createStack);
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
        setLastQuery(sql);

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
            return "bigint";
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

    private Object enumToSQLType(Enum param, int targetType) {
        switch (targetType) {
            case Types.BIGINT:
            case Types.BIT:
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.CHAR:
                return param.ordinal();
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.CLOB:
            case Types.LONGNVARCHAR:
            case Types.NVARCHAR:
                return param.name();
            default:
                log.warning("Unsupported SQL type " + targetType + " when converting enum param " + param +
                        ", will be treat as integer");

                return param.ordinal();
        }
    }

    private void putParametersIntoPreparedStatement(PreparedStatement ps, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];

            if (param != null) {
                if (param.getClass().isArray()) {
                    param = ps.getConnection().createArrayOf(javaTypeToPostgreSQLType(param.getClass().getComponentType()),
                            makeSureObjectArray(param));
                } else if (param instanceof List) {
                    param = ps.getConnection().createArrayOf(javaTypeToPostgreSQLType(param.getClass().getComponentType()),
                            ((List) param).toArray());
                } else if (param.getClass().isEnum()) {
                    int targetType = ps.getParameterMetaData().getParameterType(i + 1);
                    param = enumToSQLType((Enum) param, targetType);
                }
            }

            ps.setObject(i + 1, param);
        }
    }

    @Override
    public CompletableFuture<ExecutionResult> execute(String sql, Object... params) {
        Exception ex = new Exception();
        setLastQuery(sql);

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
        setLastQuery(sql);

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
        setLastQuery(sql);

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
    public CompletableFuture<List<JSONObject>> queryJson(String sql, Object... params) {
        return query(sql, params)
                .thenApply(r -> {
                    try {
                        List<JSONObject> list = new ArrayList<>();

                        while (r.next()) {
                            JSONObject o = new JSONObject();

                            for (int i = 1; i <= r.getMetaData().getColumnCount(); i++) {
                                Object value = r.getObject(i);

                                if (value instanceof java.sql.Array) {
                                    value = ((java.sql.Array) value).getArray();
                                }

                                o.put(r.getMetaData().getColumnLabel(i), value);
                            }

                            list.add(o);
                        }

                        return list;
                    } catch (Exception e) {
                        throw new CompletionException(e);
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
