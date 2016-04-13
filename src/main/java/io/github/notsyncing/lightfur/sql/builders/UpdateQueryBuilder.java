package io.github.notsyncing.lightfur.sql.builders;

import io.github.notsyncing.lightfur.sql.base.ConditionBuilder;
import io.github.notsyncing.lightfur.sql.base.ReturningQueryBuilder;
import io.github.notsyncing.lightfur.sql.base.SQLPart;
import io.github.notsyncing.lightfur.sql.models.ColumnModel;
import io.github.notsyncing.lightfur.sql.models.TableModel;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UpdateQueryBuilder extends ReturningQueryBuilder implements SQLPart
{
    private TableModel table;
    private Map<ColumnModel, SQLPart> setColumns = new LinkedHashMap<>();
    private List<TableModel> fromTables;
    private ConditionBuilder whereConditions = new ConditionBuilder();

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

    public UpdateQueryBuilder where(ConditionBuilder cond)
    {
        whereConditions.and().beginGroup().expr(cond).endGroup();
        return this;
    }

    public UpdateQueryBuilder returning()
    {
        needReturn = true;
        return this;
    }

    public UpdateQueryBuilder returning(SQLPart expr, String name)
    {
        needReturn = true;
        returnExpressions.put(expr, name);

        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append("UPDATE ").append(table).append("\nSET ");

        buf.append(setColumns.entrySet().stream()
            .map(e -> e.getKey() + " = (" + e.getValue() + ")")
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
