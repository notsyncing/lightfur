package io.github.notsyncing.lightfur.core.sql.builders;

import io.github.notsyncing.lightfur.core.sql.base.SQLPart;

import java.util.ArrayList;
import java.util.List;

public abstract class QueryBuilder implements SQLPart
{
    private List<Object> parameters = new ArrayList<>();

    @Override
    public List<Object> getParameters()
    {
        return parameters;
    }
}
