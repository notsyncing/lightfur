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
    public static final String NOTHING_TO_UPDATE = "NOTHING_TO_UPDATE";

    private TableModel table;
    private Map<ColumnModel, SQLPart> setColumns = new LinkedHashMap<>();
    private List<TableModel> fromTables = new ArrayList<>();
    private ExpressionBuilder whereConditions = new ExpressionBuilder();

    public boolean skipTableName = false;

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

        setColumns.put(c, new ExpressionBuilder().parameter(v));
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

    public UpdateQueryBuilder clearWhere()
    {
        whereConditions = new ExpressionBuilder();
        return this;
    }

    @Override
    public UpdateQueryBuilder returning()
    {
        return (UpdateQueryBuilder) super.returning();
    }

    @Override
    public UpdateQueryBuilder returning(SQLPart expr)
    {
        return (UpdateQueryBuilder) super.returning(expr);
    }

    @Override
    public UpdateQueryBuilder returning(SQLPart expr, String name)
    {
        return (UpdateQueryBuilder) super.returning(expr, name);
    }

    @Override
    public String toString()
    {
        if (setColumns.isEmpty()) {
            return NOTHING_TO_UPDATE;
        }

        StringBuilder buf = new StringBuilder();

        buf.append("UPDATE");

        if (!skipTableName) {
            buf.append(" ").append(table);
        }

        buf.append("\nSET ");

        buf.append(setColumns.entrySet().stream()
            .map(e -> {
                String castTo = e.getKey().getFieldType() == null ? "" : ("::" + e.getKey().getFieldType());
                return e.getKey().toColumnString() + " = (" + e.getValue().toUpdateColumnString() + ")" + castTo;
            })
            .collect(Collectors.joining(", ")));

        for (SQLPart p : setColumns.values()) {
            getParameters().addAll(p.getParameters());
        }

        if (fromTables.size() > 0) {
            buf.append("\nFROM ");

            buf.append(fromTables.stream()
                .map(TableModel::toString)
                .collect(Collectors.joining(", ")));

            for (TableModel t : fromTables) {
                if (t.isSubQuery()) {
                    getParameters().addAll(t.getSubQuery().getParameters());
                }
            }
        }

        if (!whereConditions.isEmpty()) {
            buf.append("\nWHERE ").append(whereConditions);
            getParameters().addAll(whereConditions.getParameters());
        }

        appendReturningClause(buf);

        return buf.toString();
    }
}
