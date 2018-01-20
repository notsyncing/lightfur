package io.github.notsyncing.lightfur.core.sql.models;

import io.github.notsyncing.lightfur.core.sql.base.SQLPart;

public class OrderByColumnInfo
{
    private SQLPart column;
    private boolean desc;

    public OrderByColumnInfo()
    {

    }

    public OrderByColumnInfo(SQLPart column)
    {
        this.column = column;
    }

    public OrderByColumnInfo(SQLPart column, boolean desc)
    {
        this.column = column;
        this.desc = desc;
    }

    public SQLPart getColumn()
    {
        return column;
    }

    public void setColumn(SQLPart column)
    {
        this.column = column;
    }

    public boolean isDesc()
    {
        return desc;
    }

    public void setDesc(boolean desc)
    {
        this.desc = desc;
    }

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        if (column instanceof ColumnModel) {
            ColumnModel cm = (ColumnModel)column;
            String alias = cm.getAlias();
            cm.setAlias(null);

            buf.append(column);

            cm.setAlias(alias);
        } else {
            buf.append(column);
        }

        if (desc) {
            buf.append(" DESC");
        }

        return buf.toString();
    }
}
