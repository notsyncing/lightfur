package io.github.notsyncing.lightfur.entity;

import com.alibaba.fastjson.JSON;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import scala.Char;
import scala.math.BigDecimal;

import java.lang.reflect.Array;
import java.lang.reflect.Executable;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;

public abstract class DataMapper
{
    private DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
            .withResolverStyle(ResolverStyle.SMART);

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    protected Instant valueToInstant(Object value)
    {
        if (value == null) {
            return null;
        }

        Instant time = null;

        if ((value instanceof Integer) || (value instanceof Long) || (value.getClass() == int.class) || (value.getClass() == long.class)) {
            time = Instant.ofEpochMilli((long)value);
        } else if (value instanceof String) {
            try {
                time = Instant.from(dateFormat.parse((String)value));
            } catch (DateTimeException e1) {
                e1.printStackTrace();

                try {
                    time = Instant.parse((String) value);
                } catch (Exception e) {
                    e.printStackTrace();

                    try {
                        time = sdf.parse((String)value).toInstant();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
        }

        return time;
    }

    protected LocalDateTime valueToLocalDateTime(Object value)
    {
        if (value == null) {
            return null;
        }

        if ((value instanceof Integer) || (value instanceof Long) || (value.getClass() == int.class) || (value.getClass() == long.class)) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli((long)value), ZoneId.systemDefault());
        }

        if (value instanceof String) {
            try {
                return LocalDateTime.from(dateFormat.parse((String)value));
            } catch (DateTimeException e1) {
                e1.printStackTrace();

                try {
                    return LocalDateTime.parse((String) value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    protected Object stringToType(Class<?> clazz, String value)
    {
        if (clazz.equals(String.class)) {
            return value;
        }

        if ((clazz.equals(int.class)) || (clazz.equals(Integer.class))) {
            return Integer.parseInt(value);
        }

        if ((clazz.equals(float.class)) || (clazz.equals(Float.class))) {
            return Float.parseFloat(value);
        }

        if ((clazz.equals(double.class)) || (clazz.equals(Double.class))) {
            return Double.parseDouble(value);
        }

        if ((clazz.equals(boolean.class)) || (clazz.equals(Boolean.class))) {
            return Boolean.parseBoolean(value);
        }

        if ((clazz.equals(byte.class)) || (clazz.equals(Byte.class))) {
            return Byte.parseByte(value);
        }

        if ((clazz.equals(long.class)) || (clazz.equals(Long.class))) {
            return Long.parseLong(value);
        }

        if ((clazz.equals(char.class)) || (clazz.equals(Char.class))) {
            return value.charAt(0);
        }

        return value;
    }

    protected Object convertSQLArrayToJavaArray(Class<?> arrayComponentType, String sqlArrayAsString)
    {
        sqlArrayAsString = sqlArrayAsString.substring(1, sqlArrayAsString.length() - 1);
        String[] values = sqlArrayAsString.split(",");
        Object vals = Array.newInstance(arrayComponentType, values.length);

        for (int i = 0; i < values.length; i++) {
            Array.set(vals, i, stringToType(arrayComponentType, values[i]));
        }

        return vals;
    }

    protected <T> T[] convertJsonArrayToJavaArray(Class<T> arrayComponentType, String jsonArrayAsString)
    {
        List<T> values = JSON.parseArray(jsonArrayAsString, arrayComponentType);
        Object vals = Array.newInstance(arrayComponentType, values.size());

        for (int i = 0; i < values.size(); i++) {
            Array.set(vals, i, values.get(i));
        }

        return (T[])vals;
    }

    protected Object valueToEnum(Class<?> enumClass, Integer value)
    {
        if (value == null) {
            return null;
        }

        return enumClass.getEnumConstants()[value];
    }

    protected Object valueToType(Class<?> type, Object value) throws IllegalAccessException
    {
        if (type == Instant.class) {
            return valueToInstant(value);
        } else if (type == LocalDateTime.class) {
            return valueToLocalDateTime(value);
        } else if (Enum.class.isAssignableFrom(type)) {
            return valueToEnum(type, (Integer)value);
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

                if (((s.startsWith("{")) && (s.endsWith("}"))) && (!type.isPrimitive()) && (!type.equals(String.class))) {
                    return JSON.parseObject(s, type);
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
