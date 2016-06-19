package io.github.notsyncing.lightfur.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.IntegerCodec;
import io.github.notsyncing.lightfur.annotations.entity.Column;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import scala.Char;
import scala.math.BigDecimal;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class DataMapper
{
    private static Instant valueToInstant(Object value)
    {
        if (value == null) {
            return null;
        }

        Instant time = null;

        if ((value instanceof Integer) || (value instanceof Long) || (value.getClass() == int.class) || (value.getClass() == long.class)) {
            time = Instant.ofEpochMilli((long)value);
        } else if (value instanceof String) {
            try {
                time = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S").parse((String)value).toInstant();
            } catch (ParseException e1) {
                try {
                    time = Instant.parse((String) value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return time;
    }

    private static Object stringToType(Class<?> clazz, String value)
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
            return Byte.parseByte(value);
        }

        if ((clazz.equals(char.class)) || (clazz.equals(Char.class))) {
            return Byte.parseByte(value);
        }

        return value;
    }

    private static <T> T mapSingleRow(Class<T> clazz, JsonObject row) throws IllegalAccessException, InstantiationException
    {
        T instance = clazz.newInstance();

        for (Field f : clazz.getFields()) {
            if (!f.isAnnotationPresent(Column.class)) {
                continue;
            }

            Column c = f.getAnnotation(Column.class);

            if (!row.containsKey(c.value())) {
                continue;
            }

            if (f.getType() == Instant.class) {
                f.set(instance, valueToInstant(row.getValue(c.value())));
            } else if (Enum.class.isAssignableFrom(f.getType())) {
                Integer v = row.getInteger(c.value());

                if (v == null) {
                    f.set(instance, null);
                } else {
                    f.set(instance, f.getType().getEnumConstants()[v]);
                }
            } else if (f.getType().isArray()) {
                Object value = row.getValue(c.value());

                if (value instanceof JsonArray) {
                    JsonArray arr = (JsonArray)value;
                    Object vals = Array.newInstance(f.getType().getComponentType(), arr.size());

                    for (int i = 0; i < arr.size(); i++) {
                        Array.set(vals, i, arr.getValue(i));
                    }

                    f.set(instance, vals);
                } else if (value instanceof String) {
                    String s = (String) value;

                    if ((s.startsWith("{")) && (s.endsWith("}"))) {
                        f.set(instance, convertSQLArrayToJavaArray(f.getType().getComponentType(), s));
                    } else if ((s.startsWith("[")) && (s.endsWith("]"))) {
                        f.set(instance, convertJsonArrayToJavaArray(f.getType().getComponentType(), s));
                    } else {
                        throw new IllegalAccessException("Invalid array result " + value);
                    }
                } else if (value == null) {
                    f.set(instance, null);
                } else {
                    throw new IllegalAccessException("Invalid array result " + value);
                }
            } else {
                Object value = row.getValue(c.value());

                if (value instanceof String) {
                    String s = (String)value;

                    if (((s.startsWith("{")) && (s.endsWith("}")))
                            && (!f.getType().isPrimitive())
                            && (!f.getType().equals(String.class))) {
                        f.set(instance, JSON.parseObject(s, f.getType()));
                        continue;
                    }
                } else if (value instanceof JsonObject) {
                    if (f.getType().equals(JsonObject.class)) {
                        f.set(instance, value);
                    } else {
                        f.set(instance, JSON.parseObject(((JsonObject)value).encode(), f.getType()));
                    }

                    continue;
                } else if ((value instanceof JsonArray) && (f.getType().equals(JsonArray.class))) {
                    f.set(instance, value);
                    continue;
                } else if (value instanceof BigDecimal) {
                    if (f.getType().equals(java.math.BigDecimal.class)) {
                        f.set(instance, ((BigDecimal)value).bigDecimal());
                        continue;
                    }
                }

                f.set(instance, value);
            }
        }

        return instance;
    }

    private static Object convertSQLArrayToJavaArray(Class<?> arrayComponentType, String sqlArrayAsString)
    {
        sqlArrayAsString = sqlArrayAsString.substring(1, sqlArrayAsString.length() - 1);
        String[] values = sqlArrayAsString.split(",");
        Object vals = Array.newInstance(arrayComponentType, values.length);

        for (int i = 0; i < values.length; i++) {
            Array.set(vals, i, stringToType(arrayComponentType, values[i]));
        }

        return vals;
    }

    private static <T> T[] convertJsonArrayToJavaArray(Class<T> arrayComponentType, String jsonArrayAsString)
    {
        List<T> values = JSON.parseArray(jsonArrayAsString, arrayComponentType);
        Object vals = Array.newInstance(arrayComponentType, values.size());

        for (int i = 0; i < values.size(); i++) {
            Array.set(vals, i, values.get(i));
        }

        return (T[])vals;
    }

    /**
     * 将结果集中的第一行映射至指定的类/实体上
     * @param clazz 指定的类/实体的类型
     * @param results c
     * @param <T> 指定的类/实体的类型
     * @return 指定类型的，已实例化的，并根据结果集的第一行填充其字段的类/实体对象
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public static <T> T map(Class<T> clazz, ResultSet results) throws IllegalAccessException, InstantiationException
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
    public static <T> List<T> mapToList(Class<T> clazz, ResultSet results) throws InstantiationException, IllegalAccessException
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
