package io.github.notsyncing.lightfur.sql.models;

import io.github.notsyncing.lightfur.sql.base.SQLUtils;
import io.github.notsyncing.lightfur.sql.builders.SelectQueryBuilder;

public class TableModel extends DatabaseItemModel
{
    private String name;
    private SelectQueryBuilder subQuery;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public SelectQueryBuilder getSubQuery()
    {
        return subQuery;
    }

    public void setSubQuery(SelectQueryBuilder subQuery)
    {
        this.subQuery = subQuery;
    }

    public boolean isSubQuery()
    {
        return subQuery != null;
    }

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        if (subQuery == null) {
            if (getDatabase() != null) {
                buf.append(SQLUtils.escapeName(getDatabase())).append(".");
            }

            if (getSchema() != null) {
                buf.append(SQLUtils.escapeName(getSchema())).append(".");
            }

            buf.append(SQLUtils.escapeName(name));
        } else {
            buf.append("(").append(subQuery).append(")");
        }

        if (getAlias() != null) {
            buf.append(" AS ").append(SQLUtils.escapeName(getAlias()));
        }

        return buf.toString();
    }
}
