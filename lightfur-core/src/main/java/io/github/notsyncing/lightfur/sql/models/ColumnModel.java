package io.github.notsyncing.lightfur.sql.models;

import io.github.notsyncing.lightfur.sql.base.SQLPart;
import io.github.notsyncing.lightfur.sql.base.SQLUtils;
import io.netty.util.internal.StringUtil;

public class ColumnModel extends DatabaseItemModel implements SQLPart
{
    private TableModel table;
    private String column;
    private String fieldName;
    private String modelType;
    private boolean primaryKey;
    private boolean autoIncrement;

    public ColumnModel()
    {

    }

    public ColumnModel(TableModel table)
    {
        this.table = table;
    }

    public TableModel getTable()
    {
        return table;
    }

    public void setTable(TableModel table)
    {
        this.table = table;
    }

    public String getColumn()
    {
        return column;
    }

    public void setColumn(String column)
    {
        this.column = column;
    }

    public String getFieldName()
    {
        return fieldName;
    }

    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }

    public String getModelType()
    {
        return modelType;
    }

    public void setModelType(String modelType)
    {
        this.modelType = modelType;
    }

    public boolean isPrimaryKey()
    {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey)
    {
        this.primaryKey = primaryKey;
    }

    public boolean isAutoIncrement()
    {
        return autoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement)
    {
        this.autoIncrement = autoIncrement;
    }

    @Override
    public String toString()
    {
        if (table.isSubQuery()) {
            return "(" + table.getSubQuery() + ")";
        }

        boolean tableHasAlias = table.getAlias() != null;

        StringBuilder buf = new StringBuilder();

        if (!tableHasAlias) {
            if (getDatabase() != null) {
                buf.append(SQLUtils.escapeName(getDatabase())).append(".");
            }

            if (getSchema() != null) {
                buf.append(SQLUtils.escapeName(getSchema())).append(".");
            }
        }

        if (tableHasAlias) {
            buf.append(SQLUtils.escapeName(table.getAlias()));
        } else {
            buf.append(SQLUtils.escapeName(table.getName()));
        }

        buf.append(".");

        buf.append(SQLUtils.escapeName(column));

        if (!StringUtil.isNullOrEmpty(getAlias())) {
            buf.append(" AS ").append(SQLUtils.escapeName(getAlias()));
        }

        return buf.toString();
    }

    public String toColumnString()
    {
        return SQLUtils.escapeName(column);
    }

    public String toUpdateColumnString()
    {
        if (table.isSubQuery()) {
            return "(" + table.getSubQuery() + ")";
        }

        boolean tableHasAlias = table.getAlias() != null;

        StringBuilder buf = new StringBuilder();

        if (tableHasAlias) {
            buf.append(SQLUtils.escapeName(table.getAlias()))
                    .append(".");
        }

        buf.append(SQLUtils.escapeName(column));

        return buf.toString();
    }
}
