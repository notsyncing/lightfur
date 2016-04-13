package io.github.notsyncing.lightfur.sql.base;

public class ConditionBuilder implements SQLPart
{
    private StringBuilder buf = new StringBuilder();

    @Override
    public String toString()
    {
        return buf.toString();
    }

    public boolean isEmpty()
    {
        return buf.length() <= 0;
    }

    public ConditionBuilder expr(SQLPart p)
    {
        buf.append(p);
        return this;
    }

    public ConditionBuilder operator(String op)
    {
        if (buf.length() <= 0) {
            return this;
        }

        buf.append(" ").append(op).append(" ");
        return this;
    }

    public ConditionBuilder beginGroup()
    {
        buf.append("(");
        return this;
    }

    public ConditionBuilder endGroup()
    {
        buf.append(")");
        return this;
    }

    public ConditionBuilder and()
    {
        return operator("AND");
    }

    public ConditionBuilder or()
    {
        return operator("OR");
    }

    public ConditionBuilder not()
    {
        return operator("NOT");
    }
}
