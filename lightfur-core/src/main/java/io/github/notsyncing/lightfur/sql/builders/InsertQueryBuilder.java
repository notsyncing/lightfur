package io.github.notsyncing.lightfur.sql.builders;

import io.github.notsyncing.lightfur.annotations.entity.Column;
import io.github.notsyncing.lightfur.sql.base.SQLUtils;
import io.github.notsyncing.lightfur.sql.base.ReturningQueryBuilder;
import io.github.notsyncing.lightfur.sql.base.SQLPart;
import io.github.notsyncing.lightfur.sql.models.ColumnModel;
import io.github.notsyncing.lightfur.sql.models.TableModel;

import java.util.*;
import java.util.stream.Collectors;

public class InsertQueryBuilder extends ReturningQueryBuilder implements SQLPart
{
    private TableModel table;
    private SelectQueryBuilder select;
    private List<ColumnModel> columns = new ArrayList<>();
    private List<Object> values = new ArrayList<>();

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

    @Override
    public InsertQueryBuilder returning()
    {
        return (InsertQueryBuilder) super.returning();
    }

    @Override
    public InsertQueryBuilder returning(SQLPart expr, String name)
    {
        return (InsertQueryBuilder) super.returning(expr, name);
    }

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append("INSERT INTO ").append(table);

        buf.append(" (");

        buf.append(columns.stream()
                .map(ColumnModel::toColumnString)
                .collect(Collectors.joining(", ")));

        buf.append(")");

        if (select != null) {
            buf.append("\n").append(select.toString());
        } else {
            buf.append("\nVALUES (");

            buf.append(values.stream()
                    .map(SQLUtils::valueToSQL)
                    .collect(Collectors.joining(", ")));

            buf.append(")");
        }

        appendReturningClause(buf);

        return buf.toString();
    }
}
