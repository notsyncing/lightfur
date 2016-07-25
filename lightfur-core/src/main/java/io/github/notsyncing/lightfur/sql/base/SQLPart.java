package io.github.notsyncing.lightfur.sql.base;

public interface SQLPart
{
    String toString();

    default String toUpdateColumnString()
    {
        return toString();
    }
}
