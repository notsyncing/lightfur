package io.github.notsyncing.lightfur.sql.base;

public class SQLUtils
{
    public static String escapeName(String n)
    {
        return "\"" + n + "\"";
    }

    public static String valueToSQL(Object v)
    {
        if ((v.getClass().equals(boolean.class)) || (v.getClass().equals(Boolean.class))) {
            return (Boolean)v ? "'1'" : "'0'";
        }

        return "'" + v + "'";
    }
}
