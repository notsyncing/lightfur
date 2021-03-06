package io.github.notsyncing.lightfur.entity;

import com.alibaba.fastjson.JSON;

import java.lang.reflect.Array;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public abstract class DataMapper<R>
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

        if (value instanceof Timestamp) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(((Timestamp) value).getTime()), ZoneId.systemDefault());
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

        if ((clazz.equals(char.class)) || (clazz.equals(Character.class))) {
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

    protected Object valueToEnum(Class<?> enumClass, Object value)
    {
        if (value == null) {
            return null;
        }

        if (value instanceof String) {
            for (Object enumItem : enumClass.getEnumConstants()) {
                if (((Enum)enumItem).name().equals(value)) {
                    return enumItem;
                }
            }

            return null;
        } else {
            return enumClass.getEnumConstants()[(Integer)value];
        }
    }

    protected Object[] convertToObjectArray(Object array, Class<?> targetType) {
        Class ofArray = array.getClass().getComponentType();

        if (targetType.isEnum()) {
            List ar = new ArrayList();
            int length = Array.getLength(array);

            for (int i = 0; i < length; i++) {
                Object value = Array.get(array, i);

                Object enumValue;

                if (value instanceof String) {
                    enumValue = Stream.of(targetType.getEnumConstants())
                            .filter(e -> ((Enum)e).name().equals(value))
                            .findFirst()
                            .orElse(null);
                } else {
                    enumValue = targetType.getEnumConstants()[(int) value];
                }

                ar.add(enumValue);
            }

            return ar.toArray((Object[]) Array.newInstance(targetType, 0));
        } else if (ofArray.isPrimitive()) {
            List ar = new ArrayList();
            int length = Array.getLength(array);

            for (int i = 0; i < length; i++) {
                ar.add(targetType.cast(Array.get(array, i)));
            }

            return ar.toArray();
        } else {
            return (Object[]) array;
        }
    }

    public abstract <T> T map(Class<T> clazz, R results) throws IllegalAccessException, InstantiationException, SQLException;

    public abstract <T> List<T> mapToList(Class<T> clazz, R results) throws InstantiationException, IllegalAccessException, SQLException;
}
