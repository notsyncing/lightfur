package io.github.notsyncing.lightfur.sql.builders;

import io.github.notsyncing.lightfur.sql.base.ConditionBuilder;
import io.github.notsyncing.lightfur.sql.base.ReturningQueryBuilder;
import io.github.notsyncing.lightfur.sql.base.SQLPart;
import io.github.notsyncing.lightfur.sql.models.TableModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DeleteQueryBuilder extends ReturningQueryBuilder implements SQLPart
{
    private TableModel table;
    private List<TableModel> usingTables = new ArrayList<>();
    private ConditionBuilder whereConditions = new ConditionBuilder();

    public DeleteQueryBuilder from(TableModel t)
    {
        table = t;
        return this;
    }

    public DeleteQueryBuilder using(TableModel t)
    {
        usingTables.add(t);
        return this;
    }

    public DeleteQueryBuilder using(TableModel... t)
    {
        Collections.addAll(usingTables, t);
        return this;
    }

    public DeleteQueryBuilder where(ConditionBuilder cond)
    {
        whereConditions.and().beginGroup().expr(cond).endGroup();
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append("DELETE FROM ").append(table);

        if (usingTables.size() > 0) {
            buf.append("\nUSING ");

            buf.append(usingTables.stream()
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
