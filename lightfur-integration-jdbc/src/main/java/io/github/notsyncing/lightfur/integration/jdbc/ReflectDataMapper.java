package io.github.notsyncing.lightfur.integration.jdbc;

import io.github.notsyncing.lightfur.annotations.entity.Column;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class ReflectDataMapper extends JdbcDataMapper
{
    private <T> T mapCurrentRow(Class<T> clazz, ResultSet result) throws IllegalAccessException, InstantiationException, SQLException {
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

            int colIndex;

            try {
                colIndex = result.findColumn(c.value());
            } catch (SQLException e) {
                continue;
            }

            f.set(instance, valueToType(f.getType(), result.getObject(colIndex)));
        }

        return instance;
    }

    @Override
    public <T> T map(Class<T> clazz, ResultSet results) throws IllegalAccessException, InstantiationException, SQLException {
        if (!results.next()) {
            return null;
        }

        return mapCurrentRow(clazz, results);
    }

    @Override
    public <T> List<T> mapToList(Class<T> clazz, ResultSet results) throws InstantiationException, IllegalAccessException, SQLException {
        List<T> list = new ArrayList<>();

        while (results.next()) {
            list.add(mapCurrentRow(clazz, results));
        }

        return list;
    }
}
