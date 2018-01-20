package io.github.notsyncing.lightfur.core.sql.base;

import java.util.ArrayList;
import java.util.List;

public interface SQLPart
{
    String toString();

    default String toUpdateColumnString()
    {
        return toString();
    }

    default List<Object> getParameters() { return new ArrayList<>(); }
}
