package io.github.notsyncing.lightfur.integration.vertx;

import io.github.notsyncing.lightfur.annotations.entity.Column;
import io.vertx.core.json.JsonObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class ReflectDataMapper extends VertxDataMapper
{
    @Override
    public <T> T mapSingleRow(Class<T> clazz, JsonObject row) throws IllegalAccessException, InstantiationException
    {
        T instance = clazz.newInstance();

        List<Field> fields = new ArrayList<>();
        fields.addAll(Arrays.asList(clazz.getFields()));

        if (clazz.getDeclaredFields().length > 0) {
            Stream.of(clazz.getDeclaredFields())
                    .filter(f -> Modifier.isPrivate(f.getModifiers()))
                    .forEach(f -> {
                        f.setAccessible(true);
                        fields.add(f);
                    });
        }

        for (Field f : fields) {
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
