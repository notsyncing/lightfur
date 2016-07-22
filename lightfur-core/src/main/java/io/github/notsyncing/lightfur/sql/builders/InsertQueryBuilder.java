package io.github.notsyncing.lightfur.sql.builders;

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

    public InsertQueryBuilder returning()
    {
        needReturn = true;
        return this;
    }

    public InsertQueryBuilder returning(SQLPart expr, String name)
    {
        needReturn = true;
        returnExpressions.put(expr, name);

        return this;
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
