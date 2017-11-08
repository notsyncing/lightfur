package io.github.notsyncing.lightfur.sql.builders;

import io.github.notsyncing.lightfur.sql.base.ReturningQueryBuilder;
import io.github.notsyncing.lightfur.sql.base.SQLPart;
import io.github.notsyncing.lightfur.sql.models.ColumnModel;
import io.github.notsyncing.lightfur.sql.models.TableModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class InsertQueryBuilder extends ReturningQueryBuilder implements SQLPart
{
    private TableModel table;
    private SelectQueryBuilder select;
    private List<ColumnModel> columns = new ArrayList<>();
    private List<Object> values = new ArrayList<>();
    private boolean needOnConflict = false;
    private ColumnModel onConflictColumn = null;
    private SQLPart onConflictDo = null;
    private boolean needTableAlias = false;

    public InsertQueryBuilder into(TableModel t)
    {
        table = t;
        return this;
    }

    public InsertQueryBuilder select(SelectQueryBuilder s)
    {
        select = s;
        return this;
    }

    public InsertQueryBuilder column(ColumnModel c)
    {
        return column(c, null);
    }

    public InsertQueryBuilder column(ColumnModel c, Object value)
    {
        columns.add(c);
        values.add(value);
        return this;
    }

    public InsertQueryBuilder columns(ColumnModel[] c, Object[] v)
    {
        Collections.addAll(columns, c);
        Collections.addAll(values, v);
        return this;
    }

    public InsertQueryBuilder ignore(ColumnModel c)
    {
        int i = columns.indexOf(c);
        columns.remove(i);
        values.remove(i);
        return this;
    }

    public InsertQueryBuilder ignore(String columnName)
    {
        int i = columns.stream()
                .filter(c -> c.getColumn().equals(columnName))
                .map(c -> columns.indexOf(c))
                .findFirst()
                .orElse(-1);

        if (i >= 0) {
            columns.remove(i);
            values.remove(i);
        }

        return this;
    }

    public InsertQueryBuilder skipExisting()
    {
        needOnConflict = true;
        return this;
    }

    public InsertQueryBuilder whenExists(ColumnModel column, SQLPart alterOp)
    {
        needOnConflict = true;
        onConflictColumn = column;
        onConflictDo = alterOp;
        return this;
    }

    @Override
    public InsertQueryBuilder returning()
    {
        return (InsertQueryBuilder) super.returning();
    }

    @Override
    public InsertQueryBuilder returning(SQLPart expr)
    {
        if (expr instanceof ColumnModel) {
            ((ColumnModel) expr).getTable().setAlias(null);
            ((ColumnModel) expr).getTable().setName(null);
        }

        return (InsertQueryBuilder) super.returning(expr);
    }

    @Override
    public InsertQueryBuilder returning(SQLPart expr, String name)
    {
        if (expr instanceof ColumnModel) {
            ((ColumnModel) expr).getTable().setAlias(null);
            ((ColumnModel) expr).getTable().setName(null);
        }

        return (InsertQueryBuilder) super.returning(expr, name);
    }

    public InsertQueryBuilder withAlias()
    {
        needTableAlias = true;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append("INSERT INTO ").append(needTableAlias ? table.toString() : table.toStringWithoutAlias());

        buf.append(" (");

        buf.append(columns.stream()
                .map(ColumnModel::toColumnString)
                .collect(Collectors.joining(", ")));

        buf.append(")");

        if (select != null) {
            buf.append("\n").append(select.toString());
            getParameters().addAll(select.getParameters());
        } else {
            buf.append("\nVALUES (");

            for (int i = 0; i < values.size(); i++) {
                Object o = values.get(i);
                ColumnModel c = columns.get(i);

                String castTo = c.getFieldType() == null ? "" : ("::" + c.getFieldType());

                if (o instanceof SQLPart) {
                    String v = o.toString();
                    buf.append(v).append(castTo).append(", ");

                    getParameters().addAll(((SQLPart) o).getParameters());
                } else {
                    buf.append("?").append(castTo).append(", ");

                    getParameters().add(o);
                }
            }

            buf.delete(buf.length() - 2, buf.length());

            buf.append(")");
        }

        if (needOnConflict) {
            buf.append("\nON CONFLICT ");

            if (onConflictColumn != null) {
                buf.append("(").append(onConflictColumn.toColumnString()).append(") ");
            }

            buf.append("DO ");

            if (onConflictDo == null) {
                buf.append("NOTHING");
            } else {
                buf.append(onConflictDo.toString());
                getParameters().addAll(onConflictDo.getParameters());
            }
        }

        appendReturningClause(buf);

        return buf.toString();
    }
}
