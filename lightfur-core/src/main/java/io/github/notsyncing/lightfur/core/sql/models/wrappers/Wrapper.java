package io.github.notsyncing.lightfur.core.sql.models.wrappers;

import io.github.notsyncing.lightfur.core.entity.DataModel;

public class Wrapper<T> implements DataModel
{
    private T value;

    public T getValue()
    {
        return value;
    }

    public void setValue(T value)
    {
        this.value = value;
    }
}
