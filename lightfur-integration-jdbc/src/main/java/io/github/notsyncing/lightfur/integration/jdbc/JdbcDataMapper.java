package io.github.notsyncing.lightfur.integration.jdbc;

import com.alibaba.fastjson.JSON;
import io.github.notsyncing.lightfur.entity.DataMapper;
import org.postgresql.util.PGobject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public abstract class JdbcDataMapper extends DataMapper<ResultSet> {
    protected Object valueToType(Class<?> type, Object value) throws IllegalAccessException, SQLException {
        if (type == Instant.class) {
            return valueToInstant(value);
        } else if (type == LocalDateTime.class) {
            return valueToLocalDateTime(value);
        } else if (Enum.class.isAssignableFrom(type)) {
            return valueToEnum(type, value);
        } else if (type.isArray()) {
            if (value == null) {
                return null;
            } else if (value instanceof java.sql.Array) {
                return convertToObjectArray(((java.sql.Array) value).getArray(), type.getComponentType());
            } else if (value.getClass().isArray()) {
                return convertToObjectArray(value, type.getComponentType());
            } else if (value instanceof String) {
                String s = (String) value;

                if ((s.startsWith("{")) && (s.endsWith("}"))) {
                    return convertSQLArrayToJavaArray(type.getComponentType(), s);
                } else if ((s.startsWith("[")) && (s.endsWith("]"))) {
                    return convertJsonArrayToJavaArray(type.getComponentType(), s);
                } else {
                    throw new IllegalAccessException("Invalid array result " + value);
                }
            } else {
                throw new IllegalAccessException("Invalid array result " + value);
            }
        } else {
            if (value instanceof String) {
                String s = (String)value;

                if ((!type.isPrimitive()) && (!type.equals(String.class))) {
                    if ((s.startsWith("{")) && (s.endsWith("}"))) {
                        return JSON.parseObject(s, type);
                    } else if ((s.startsWith("[")) && (s.endsWith("]"))) {
                        return JSON.parseArray(s, type);
                    }
                }
            } else if (value instanceof PGobject) {
                return valueToType(type, value.toString());
            }

            return value;
        }
    }

    public abstract <T> T map(Class<T> clazz, ResultSet results) throws IllegalAccessException, InstantiationException, SQLException;

    public abstract <T> List<T> mapToList(Class<T> clazz, ResultSet results) throws InstantiationException, IllegalAccessException, SQLException;
}
