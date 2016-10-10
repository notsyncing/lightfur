package io.github.notsyncing.lightfur.entity;

import io.github.notsyncing.lightfur.annotations.entity.Column;
import io.vertx.core.json.JsonObject;

import java.lang.reflect.Field;

public class ReflectDataMapper extends DataMapper
{
    @Override
    public <T> T mapSingleRow(Class<T> clazz, JsonObject row) throws IllegalAccessException, InstantiationException
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

            f.set(instance, valueToType(f.getType(), row.getValue(c.value())));
        }

        return instance;
    }
}
