package io.github.notsyncing.lightfur.sql.builders;

import io.github.notsyncing.lightfur.sql.base.ConditionBuilder;
import io.github.notsyncing.lightfur.sql.base.SQLPart;

public class CaseWhenBuilder implements SQLPart
{
    private StringBuilder buf = new StringBuilder();

    public CaseWhenBuilder()
    {
        buf.append("CASE ");
    }

    @Override
    public String toString()
    {
        return buf.toString();
    }

    public CaseWhenBuilder when(ConditionBuilder c)
    {
        buf.append("WHEN ").append(c);
        return this;
    }

    public CaseWhenBuilder then(SQLPart p)
    {
        buf.append(" THEN ").append(p);
        return this;
    }

    public CaseWhenBuilder elseThen(SQLPart p)
    {
        buf.append(" ELSE ").append(p);
        return this;
    }

    public CaseWhenBuilder end()
    {
        buf.append(" END");
        return this;
    }
}
