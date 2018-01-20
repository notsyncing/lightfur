package io.github.notsyncing.lightfur.core.sql.builders;

import io.github.notsyncing.lightfur.core.sql.base.ExpressionBuilder;
import io.github.notsyncing.lightfur.core.sql.base.SQLPart;
import io.github.notsyncing.lightfur.core.sql.base.SQLUtils;
import io.github.notsyncing.lightfur.core.sql.models.ColumnModel;
import io.github.notsyncing.lightfur.core.sql.models.JoinClauseInfo;
import io.github.notsyncing.lightfur.core.sql.models.OrderByColumnInfo;
import io.github.notsyncing.lightfur.core.sql.models.TableModel;

import java.util.*;
import java.util.stream.Collectors;

public class SelectQueryBuilder extends QueryBuilder implements SQLPart
{
    private List<TableModel> fromTables = new ArrayList<>();
    private List<SQLPart> selectColumns = new ArrayList<>();
    private List<AbstractMap.SimpleEntry<SQLPart, ColumnModel>> selectAsColumns = new ArrayList<>();
    private List<JoinClauseInfo> joinClauses = new ArrayList<>();
    private ExpressionBuilder whereConditions = new ExpressionBuilder();
    private List<SQLPart> groupColumns = new ArrayList<>();
    private List<OrderByColumnInfo> orderByColumns = new ArrayList<>();
    private ExpressionBuilder havingConditions = new ExpressionBuilder();
    private int limit = -1;
    private int offset = -1;
    private boolean distinct = false;
    private SQLPart distinctOn;

    public List<SQLPart> getSelectColumns()
    {
        return selectColumns;
    }

    public void setSelectColumns(List<SQLPart> selectColumns)
    {
        this.selectColumns = selectColumns;
    }

    public SelectQueryBuilder from(TableModel... tables)
    {
        if ((tables == null) || (tables.length <= 0)) {
            return this;
        }

        Collections.addAll(fromTables, tables);
        return this;
    }

    public SelectQueryBuilder distinct()
    {
        distinct = true;
        return this;
    }

    public SelectQueryBuilder distinctOn(SQLPart column)
    {
        distinct = true;
        distinctOn = column;
        return this;
    }

    public SelectQueryBuilder select(SQLPart... columns)
    {
        if ((columns == null) || (columns.length <= 0)) {
            return this;
        }

        Collections.addAll(selectColumns, columns);
        return this;
    }

    public SelectQueryBuilder select(List<SQLPart> columns)
    {
        if ((columns == null) || (columns.size() <= 0)) {
            return this;
        }

        selectColumns.addAll(columns);
        return this;
    }

    public SelectQueryBuilder selectAs(SQLPart source, ColumnModel asColumn)
    {
        selectColumns.removeIf(p -> {
            if (!(p instanceof ColumnModel)) {
                return false;
            }

            ColumnModel c = (ColumnModel)p;

            return (c.getModelType().equals(asColumn.getModelType())) && (c.getFieldName().equals(asColumn.getFieldName()));
        });

        selectAsColumns.add(new AbstractMap.SimpleEntry<>(source, asColumn));

        return this;
    }

    public SelectQueryBuilder join(String joinType, TableModel targetTable, ExpressionBuilder conditions)
    {
        JoinClauseInfo info = new JoinClauseInfo();
        info.setJoinType(joinType);
        info.setTargetTable(targetTable);
        info.setJoinCondition(conditions);

        joinClauses.add(info);
        return this;
    }

    public SelectQueryBuilder leftJoin(TableModel targetTable, ExpressionBuilder conditions)
    {
        return join(JoinClauseInfo.JOIN_LEFT, targetTable, conditions);
    }

    public SelectQueryBuilder rightJoin(TableModel targetTable, ExpressionBuilder conditions)
    {
        return join(JoinClauseInfo.JOIN_RIGHT, targetTable, conditions);
    }

    public SelectQueryBuilder innerJoin(TableModel targetTable, ExpressionBuilder conditions)
    {
        return join(JoinClauseInfo.JOIN_INNER, targetTable, conditions);
    }

    public SelectQueryBuilder crossJoin(TableModel targetTable, ExpressionBuilder conditions)
    {
        return join(JoinClauseInfo.JOIN_CROSS, targetTable, conditions);
    }

    public SelectQueryBuilder fullJoin(TableModel targetTable, ExpressionBuilder conditions)
    {
        return join(JoinClauseInfo.JOIN_FULL, targetTable, conditions);
    }

    public SelectQueryBuilder where(ExpressionBuilder conditions)
    {
        if (!whereConditions.isEmpty()) {
            whereConditions.and();
        }

        whereConditions.beginGroup().expr(conditions).endGroup();
        return this;
    }

    public SelectQueryBuilder groupBy(SQLPart... columns)
    {
        if ((columns == null) || (columns.length <= 0)) {
            return this;
        }

        Collections.addAll(groupColumns, columns);
        return this;
    }

    public SelectQueryBuilder orderBy(OrderByColumnInfo... columns)
    {
        if ((columns == null) || (columns.length <= 0)) {
            return this;
        }

        Collections.addAll(orderByColumns, columns);
        return this;
    }

    public SelectQueryBuilder having(ExpressionBuilder conditions)
    {
        havingConditions.and().beginGroup().expr(conditions).endGroup();
        return this;
    }

    public SelectQueryBuilder offset(int n)
    {
        offset = n;
        return this;
    }

    public SelectQueryBuilder limit(int n)
    {
        limit = n;
        return this;
    }

    @Override
    public String toString()
    {
        getParameters().clear();
        StringBuilder buf = new StringBuilder();

        buf.append("SELECT ");

        if (distinct) {
            buf.append("DISTINCT ");

            if (distinctOn != null) {
                buf.append("ON (").append(distinctOn).append(") ");
            }
        }

        buf.append(selectColumns.stream()
                .map(SQLPart::toString)
                .collect(Collectors.joining(", ")));

        for (SQLPart p : selectColumns) {
            getParameters().addAll(p.getParameters());
        }

        if (selectAsColumns.size() > 0) {
            if (selectColumns.size() > 0) {
                buf.append(", ");
            }

            buf.append(selectAsColumns.stream()
                    .map(p -> "(" + p.getKey().toString() + ") AS " + SQLUtils.escapeName(p.getValue().getColumn()))
                    .collect(Collectors.joining(", ")));

            for (Map.Entry<SQLPart, ColumnModel> p : selectAsColumns) {
                getParameters().addAll(p.getKey().getParameters());
            }
        }

        if (fromTables.size() > 0) {
            buf.append("\nFROM ");

            buf.append(fromTables.stream()
                    .map(TableModel::toString)
                    .collect(Collectors.joining(", ")));

            for (TableModel t : fromTables) {
                if (!t.isSubQuery()) {
                    continue;
                }

                getParameters().addAll(t.getSubQuery().getParameters());
            }
        }

        if (joinClauses.size() > 0) {
            buf.append("\n");

            buf.append(joinClauses.stream()
                    .map(JoinClauseInfo::toString)
                    .collect(Collectors.joining("\n")));

            for (JoinClauseInfo j : joinClauses) {
                if (j.getTargetTable().isSubQuery()) {
                    getParameters().addAll(j.getTargetTable().getSubQuery().getParameters());
                }

                getParameters().addAll(j.getJoinCondition().getParameters());
            }
        }

        if (!whereConditions.isEmpty()) {
            getParameters().addAll(whereConditions.getParameters());
            buf.append("\nWHERE ").append(whereConditions);
        }

        if (groupColumns.size() > 0) {
            buf.append("\nGROUP BY ");

            buf.append(groupColumns.stream()
                    .map(SQLPart::toString)
                    .collect(Collectors.joining("\n")));
        }

        if (!havingConditions.isEmpty()) {
            getParameters().addAll(havingConditions.getParameters());
            buf.append("\nHAVING ").append(havingConditions);
        }

        if (orderByColumns.size() > 0) {
            buf.append("\nORDER BY ");

            buf.append(orderByColumns.stream()
                    .map(OrderByColumnInfo::toString)
                    .collect(Collectors.joining(", ")));
        }

        if (limit >= 0) {
            getParameters().add(limit);
            buf.append("\nLIMIT ?");
        }

        if (offset >= 0) {
            getParameters().add(offset);
            buf.append("\nOFFSET ?");
        }

        return buf.toString();
    }
}
