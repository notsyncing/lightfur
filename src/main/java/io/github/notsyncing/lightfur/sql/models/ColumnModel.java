package io.github.notsyncing.lightfur.sql.models;

import io.github.notsyncing.lightfur.sql.base.SQLPart;
import io.github.notsyncing.lightfur.sql.base.SQLUtils;

public class ColumnModel extends DatabaseItemModel implements SQLPart
{
    private TableModel table;
    private String column;

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

        return buf.toString();
    }

    public String toColumnString()
    {
        return SQLUtils.escapeName(column);
    }
}
