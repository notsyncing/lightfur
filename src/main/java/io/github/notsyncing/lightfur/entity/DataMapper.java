package io.github.notsyncing.lightfur.entity;

import io.github.notsyncing.lightfur.annotations.entity.Column;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class DataMapper
{
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

            f.set(instance, row.getValue(c.value()));
        }

        return instance;
    }

    public static <T> T map(Class<T> clazz, ResultSet results) throws IllegalAccessException, InstantiationException
    {
        if (results.getNumRows() <= 0) {
            return null;
        }

        JsonObject row = results.getRows().get(0);
        return mapSingleRow(clazz, row);
    }

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
