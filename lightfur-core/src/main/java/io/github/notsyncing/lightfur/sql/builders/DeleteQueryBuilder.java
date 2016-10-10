package io.github.notsyncing.lightfur.sql.builders;

import io.github.notsyncing.lightfur.sql.base.ExpressionBuilder;
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
    private ExpressionBuilder whereConditions = new ExpressionBuilder();

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

    public DeleteQueryBuilder where(ExpressionBuilder cond)
    {
        if (!whereConditions.isEmpty()) {
            whereConditions.and();
        }

        whereConditions.beginGroup().expr(cond).endGroup();
        return this;
    }

    public DeleteQueryBuilder clearWhere()
    {
        whereConditions = new ExpressionBuilder();
        return this;
    }

    @Override
    public DeleteQueryBuilder returning()
    {
        return (DeleteQueryBuilder) super.returning();
    }

    @Override
    public DeleteQueryBuilder returning(SQLPart expr)
    {
        return (DeleteQueryBuilder) super.returning(expr);
    }

    @Override
    public DeleteQueryBuilder returning(SQLPart expr, String name)
    {
        return (DeleteQueryBuilder) super.returning(expr, name);
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

            for (TableModel t : usingTables) {
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
