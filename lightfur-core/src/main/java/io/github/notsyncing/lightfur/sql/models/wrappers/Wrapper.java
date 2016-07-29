package io.github.notsyncing.lightfur.sql.models.wrappers;

import io.github.notsyncing.lightfur.entity.DataModel;

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
