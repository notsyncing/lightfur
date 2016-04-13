package io.github.notsyncing.lightfur.sql.models;

import io.github.notsyncing.lightfur.sql.base.SQLPart;

public class OrderByColumnInfo
{
    private SQLPart column;
    private boolean desc;

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

        buf.append(column);

        if (desc) {
            buf.append(" DESC");
        }

        return buf.toString();
    }
}
