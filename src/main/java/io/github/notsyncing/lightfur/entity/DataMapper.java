package io.github.notsyncing.lightfur.entity;

import io.github.notsyncing.lightfur.annotations.entity.Column;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;

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
                f.set(instance, f.getType().getEnumConstants()[row.getInteger(c.value())]);
            } else {
                f.set(instance, row.getValue(c.value()));
            }
        }

        return instance;
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
            return null;
        }

        List<T> list = new ArrayList<>();

        for (JsonObject o : results.getRows()) {
            list.add(mapSingleRow(clazz, o));
        }

        return list;
    }
}
