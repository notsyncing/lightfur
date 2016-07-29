package io.github.notsyncing.lightfur.sql.base;

public class ExpressionBuilder implements SQLPart
{
    private StringBuilder buf = new StringBuilder();

    public boolean isEmpty()
    {
        return buf.length() <= 0;
    }

    @Override
    public String toString()
    {
        return buf.toString();
    }

    public ExpressionBuilder beginGroup()
    {
        buf.append("(");
        return this;
    }

    public ExpressionBuilder endGroup()
    {
        buf.append(")");
        return this;
    }

    public ExpressionBuilder identifier(String s)
    {
        buf.append(s);
        return this;
    }

    public ExpressionBuilder column(SQLPart c)
    {
        buf.append(c);
        return this;
    }

    public ExpressionBuilder expr(SQLPart e)
    {
        buf.append(e);
        return this;
    }

    public ExpressionBuilder beginFunction(String f)
    {
        buf.append(f).append("(");
        return this;
    }

    public ExpressionBuilder endFunction()
    {
        buf.append(")");
        return this;
    }

    public ExpressionBuilder literal(String l)
    {
        buf.append("'").append(l).append("'");
        return this;
    }

    public ExpressionBuilder literal(int l)
    {
        buf.append(l);
        return this;
    }

    public ExpressionBuilder literal(float l)
    {
        buf.append(l);
        return this;
    }

    public ExpressionBuilder literal(double l)
    {
        buf.append(l);
        return this;
    }

    public ExpressionBuilder literal(boolean l)
    {
        buf.append(l ? "true" : "false");
        return this;
    }

    public ExpressionBuilder literal(long l)
    {
        buf.append(l);
        return this;
    }

    public ExpressionBuilder operator(String op)
    {
        buf.append(" ").append(op).append(" ");
        return this;
    }

    public ExpressionBuilder and()
    {
        return operator("AND");
    }

    public ExpressionBuilder or()
    {
        return operator("OR");
    }

    public ExpressionBuilder eq()
    {
        return operator("=");
    }

    public ExpressionBuilder gt()
    {
        return operator(">");
    }

    public ExpressionBuilder lt()
    {
        return operator("<");
    }

    public ExpressionBuilder gte()
    {
        return operator(">=");
    }

    public ExpressionBuilder lte()
    {
        return operator("<=");
    }

    public ExpressionBuilder ne()
    {
        return operator("<>");
    }

    public ExpressionBuilder in()
    {
        return operator("IN");
    }

    public ExpressionBuilder not()
    {
        return operator("NOT");
    }

    public ExpressionBuilder like()
    {
        return operator("LIKE");
    }

    public ExpressionBuilder namedParameterReference(String name)
    {
        buf.append(":").append(name);
        return this;
    }

    public ExpressionBuilder parameterReference()
    {
        buf.append("?");
        return this;
    }

    public ExpressionBuilder raw(String s)
    {
        buf.append(s);
        return this;
    }
}
