package io.github.notsyncing.lightfur.integration.vertx;

import com.alibaba.fastjson.JSON;
import io.github.notsyncing.lightfur.entity.DataMapper;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import scala.math.BigDecimal;

import java.lang.reflect.Array;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class VertxDataMapper extends DataMapper<ResultSet>
{
    protected Object valueToType(Class<?> type, Object value) throws IllegalAccessException
    {
        if (type == Instant.class) {
            return valueToInstant(value);
        } else if (type == LocalDateTime.class) {
            return valueToLocalDateTime(value);
        } else if (Enum.class.isAssignableFrom(type)) {
            return valueToEnum(type, value);
        } else if (type.isArray()) {
            if (value instanceof JsonArray) {
                JsonArray arr = (JsonArray) value;
                Object vals = Array.newInstance(type.getComponentType(), arr.size());

                for (int i = 0; i < arr.size(); i++) {
                    Array.set(vals, i, arr.getValue(i));
                }

                return vals;
            } else if (value instanceof String) {
                String s = (String) value;

                if ((s.startsWith("{")) && (s.endsWith("}"))) {
                    return convertSQLArrayToJavaArray(type.getComponentType(), s);
                } else if ((s.startsWith("[")) && (s.endsWith("]"))) {
                    return convertJsonArrayToJavaArray(type.getComponentType(), s);
                } else {
                    throw new IllegalAccessException("Invalid array result " + value);
                }
            } else if (value == null) {
                return null;
            } else {
                throw new IllegalAccessException("Invalid array result " + value);
            }
        } else if (type.equals(java.math.BigDecimal.class)) {
            if (value != null) {
                return new java.math.BigDecimal((String)value);
            } else {
                return null;
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
            } else if (value instanceof JsonObject) {
                if (type.equals(JsonObject.class)) {
                    return value;
                } else {
                    return JSON.parseObject(((JsonObject)value).encode(), type);
                }
            } else if ((value instanceof JsonArray) && (type.equals(JsonArray.class))) {
                return value;
            } else if (value instanceof BigDecimal) {
                if (type.equals(java.math.BigDecimal.class)) {
                    return ((BigDecimal)value).bigDecimal();
                }
            }

            return value;
        }
    }

    protected abstract <T> T mapSingleRow(Class<T> clazz, JsonObject row) throws IllegalAccessException, InstantiationException;

    /**
     * 将结果集中的第一行映射至指定的类/实体上
     * @param clazz 指定的类/实体的类型
     * @param results c
     * @param <T> 指定的类/实体的类型
     * @return 指定类型的，已实例化的，并根据结果集的第一行填充其字段的类/实体对象
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @Override
    public <T> T map(Class<T> clazz, ResultSet results) throws IllegalAccessException, InstantiationException
    {
        if (results.getNumRows() <= 0) {
            return null;
        }

        JsonObject row = results.getRows().get(0);
        return mapSingleRow(clazz, row);
    }

    /**
     * 将整个结果集映射到指定的类/实体的列表上
     * @param clazz 指定的类/实体的类型
     * @param results 指定的类/实体的类型
     * @param <T> 指定的类/实体的类型
     * @return 指定类型的，已实例化的，并根据结果集的每一行填充其字段的类/实体对象的列表
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @Override
    public <T> List<T> mapToList(Class<T> clazz, ResultSet results) throws InstantiationException, IllegalAccessException
    {
        if (results.getNumRows() <= 0) {
            return new ArrayList<>();
        }

        List<T> list = new ArrayList<>();

        for (JsonObject o : results.getRows()) {
            list.add(mapSingleRow(clazz, o));
        }

        return list;
    }
}
