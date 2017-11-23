package io.github.notsyncing.lightfur.sql.base;

import io.github.notsyncing.lightfur.sql.models.ColumnModel;

import java.util.ArrayList;
import java.util.List;

public class ExpressionBuilder implements SQLPart
{
    private StringBuilder buf = new StringBuilder();
    private List<Object> parameters = new ArrayList<>();

    @Override
    public List<Object> getParameters()
    {
        return parameters;
    }

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
        return column(c, false);
    }

    public ExpressionBuilder column(SQLPart c, boolean noAlias)
    {
        if ((c instanceof ColumnModel) && (noAlias)) {
            ColumnModel column = (ColumnModel)c;
            String alias = column.getAlias();
            column.setAlias(null);

            buf.append(c);

            column.setAlias(alias);
        } else {
            buf.append(c);
        }

        return this;
    }

    public ExpressionBuilder expr(SQLPart e)
    {
        if (e instanceof ColumnModel) {
            ColumnModel column = (ColumnModel)e;
            String alias = column.getAlias();
            column.setAlias(null);

            buf.append(e);

            column.setAlias(alias);
        } else {
            buf.append(e);
            parameters.addAll(e.getParameters());
        }

        return this;
    }

    public ExpressionBuilder beginFunction(String f)
    {
        buf.append(f).append("(");
        return this;
    }

    public ExpressionBuilder endFunction()
    {
        if ((buf.charAt(buf.length() - 2) == ',') && (buf.charAt(buf.length() - 1) == ' ')) {
            buf.delete(buf.length() - 2, buf.length());
        }

        buf.append(")");
        return this;
    }

    public ExpressionBuilder separator()
    {
        buf.append(", ");
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

    public ExpressionBuilder literal(Object l)
    {
        if (l instanceof String) {
            return literal((String)l);
        } else if ((l.getClass() == int.class) || (l instanceof Integer)) {
            return literal((int)l);
        } else if ((l.getClass() == double.class) || (l instanceof Double)) {
            return literal((double)l);
        } else if ((l.getClass() == float.class) || (l instanceof Float)) {
            return literal((float)l);
        } else if ((l.getClass() == boolean.class) || (l instanceof Boolean)) {
            return literal((boolean)l);
        } else if ((l.getClass() == long.class) || (l instanceof Long)) {
            return literal((long)l);
        }

        return literal(l.toString());
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

    public ExpressionBuilder eqNull()
    {
        return is().raw("NULL");
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

    public ExpressionBuilder neNull()
    {
        return is().not().raw("NULL");
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

    public ExpressionBuilder is()
    {
        return operator("IS");
    }

    public ExpressionBuilder namedParameter(String name, Object parameter, String castTo)
    {
        buf.append(":").append(name);

        if (castTo != null) {
            buf.append("::").append(castTo);
        }

        parameters.add(parameter);
        return this;
    }

    public ExpressionBuilder namedParameter(String name, Object parameter)
    {
        return namedParameter(name, parameter, null);
    }

    public ExpressionBuilder parameter(Object parameter, String castTo)
    {
        buf.append("?");

        if (castTo != null) {
            buf.append("::").append(castTo);
        }

        parameters.add(parameter);
        return this;
    }

    public ExpressionBuilder parameter(Object parameter)
    {
        return parameter(parameter, null);
    }

    public ExpressionBuilder raw(String s)
    {
        buf.append(s);
        return this;
    }
}
