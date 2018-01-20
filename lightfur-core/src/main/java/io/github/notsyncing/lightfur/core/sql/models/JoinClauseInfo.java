package io.github.notsyncing.lightfur.core.sql.models;

import io.github.notsyncing.lightfur.core.sql.base.ExpressionBuilder;

public class JoinClauseInfo
{
    public static final String JOIN_LEFT = "LEFT";
    public static final String JOIN_RIGHT = "RIGHT";
    public static final String JOIN_INNER = "INNER";
    public static final String JOIN_CROSS = "CROSS";
    public static final String JOIN_FULL = "FULL";

    private String joinType;
    private TableModel targetTable;
    private ExpressionBuilder joinCondition;

    public String getJoinType()
    {
        return joinType;
    }

    public void setJoinType(String joinType)
    {
        this.joinType = joinType;
    }

    public TableModel getTargetTable()
    {
        return targetTable;
    }

    public void setTargetTable(TableModel targetTable)
    {
        this.targetTable = targetTable;
    }

    public ExpressionBuilder getJoinCondition()
    {
        return joinCondition;
    }

    public void setJoinCondition(ExpressionBuilder joinCondition)
    {
        this.joinCondition = joinCondition;
    }

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append(joinType).append(" JOIN ");

        if (targetTable.isSubQuery()) {
            buf.append("(").append(targetTable).append(")");
        } else {
            buf.append(targetTable);
        }

        buf.append(" ON ").append(joinCondition);

        return buf.toString();
    }
}
