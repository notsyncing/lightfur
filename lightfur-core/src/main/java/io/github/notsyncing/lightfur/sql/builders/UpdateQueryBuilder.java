package io.github.notsyncing.lightfur.sql.builders;

import io.github.notsyncing.lightfur.sql.base.ExpressionBuilder;
import io.github.notsyncing.lightfur.sql.base.ReturningQueryBuilder;
import io.github.notsyncing.lightfur.sql.base.SQLPart;
import io.github.notsyncing.lightfur.sql.models.ColumnModel;
import io.github.notsyncing.lightfur.sql.models.TableModel;

import java.util.*;
import java.util.stream.Collectors;

public class UpdateQueryBuilder extends ReturningQueryBuilder implements SQLPart
{
    private TableModel table;
    private Map<ColumnModel, SQLPart> setColumns = new LinkedHashMap<>();
    private List<TableModel> fromTables = new ArrayList<>();
    private ExpressionBuilder whereConditions = new ExpressionBuilder();

    public UpdateQueryBuilder on(TableModel t)
    {
        table = t;
        return this;
    }

    public UpdateQueryBuilder set(ColumnModel c, SQLPart v)
    {
        setColumns.put(c, v);
        return this;
    }

    public UpdateQueryBuilder set(ColumnModel c, Object v)
    {
        if (v instanceof SQLPart) {
            return set(c, (SQLPart)v);
        }

        setColumns.put(c, new ExpressionBuilder().literal(v));

        return this;
    }

    public UpdateQueryBuilder from(TableModel t)
    {
        fromTables.add(t);
        return this;
    }

    public UpdateQueryBuilder from(TableModel... t)
    {
        Collections.addAll(fromTables, t);
        return this;
    }

    public UpdateQueryBuilder where(ExpressionBuilder cond)
    {
        if (!whereConditions.isEmpty()) {
            whereConditions.and();
        }

        whereConditions.beginGroup().expr(cond).endGroup();

        return this;
    }

    @Override
    public UpdateQueryBuilder returning()
    {
        return (UpdateQueryBuilder) super.returning();
    }

    @Override
    public UpdateQueryBuilder returning(SQLPart expr, String name)
    {
        return (UpdateQueryBuilder) super.returning(expr, name);
    }

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append("UPDATE ").append(table).append("\nSET ");

        buf.append(setColumns.entrySet().stream()
            .map(e -> e.getKey().toColumnString() + " = (" + e.getValue().toUpdateColumnString() + ")")
            .collect(Collectors.joining(", ")));

        if (fromTables.size() > 0) {
            buf.append("\nFROM ");

            buf.append(fromTables.stream()
                .map(TableModel::toString)
                .collect(Collectors.joining(", ")));
        }

        if (!whereConditions.isEmpty()) {
            buf.append("\nWHERE ").append(whereConditions);
        }

        appendReturningClause(buf);

        return buf.toString();
    }
}
