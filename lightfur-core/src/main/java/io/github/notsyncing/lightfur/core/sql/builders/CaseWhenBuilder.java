package io.github.notsyncing.lightfur.core.sql.builders;

import io.github.notsyncing.lightfur.core.sql.base.ExpressionBuilder;
import io.github.notsyncing.lightfur.core.sql.base.SQLPart;

import java.util.ArrayList;
import java.util.List;

public class CaseWhenBuilder implements SQLPart
{
    private StringBuilder buf = new StringBuilder();
    private List<Object> parameters = new ArrayList<>();

    public CaseWhenBuilder()
    {
        buf.append("CASE ");
    }

    @Override
    public List<Object> getParameters()
    {
        return parameters;
    }

    @Override
    public String toString()
    {
        return buf.toString();
    }

    public CaseWhenBuilder when(ExpressionBuilder c)
    {
        buf.append("WHEN ").append(c);
        getParameters().addAll(c.getParameters());
        return this;
    }

    public CaseWhenBuilder then(SQLPart p)
    {
        buf.append(" THEN ").append(p);
        getParameters().addAll(p.getParameters());
        return this;
    }

    public CaseWhenBuilder elseThen(SQLPart p)
    {
        buf.append(" ELSE ").append(p);
        getParameters().addAll(p.getParameters());
        return this;
    }

    public CaseWhenBuilder end()
    {
        buf.append(" END");
        return this;
    }
}
