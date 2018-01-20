package io.github.notsyncing.lightfur.core.sql.models;

import io.github.notsyncing.lightfur.core.sql.base.SQLUtils;
import io.github.notsyncing.lightfur.core.sql.builders.SelectQueryBuilder;
import io.github.notsyncing.lightfur.core.utils.StringUtils;

public class TableModel extends DatabaseItemModel implements Cloneable
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

    private String toString(boolean needAlias)
    {
        StringBuilder buf = new StringBuilder();

        if (subQuery == null) {
            if (!StringUtils.isEmpty(getDatabase())) {
                buf.append(SQLUtils.escapeName(getDatabase())).append(".");
            }

            if (!StringUtils.isEmpty(getSchema())) {
                buf.append(SQLUtils.escapeName(getSchema())).append(".");
            }

            buf.append(SQLUtils.escapeName(name));
        } else {
            buf.append("(").append(subQuery).append(")");
        }

        if ((needAlias) && (getAlias() != null)) {
            buf.append(" AS ").append(SQLUtils.escapeName(getAlias()));
        }

        return buf.toString();
    }

    @Override
    public String toString()
    {
        return toString(true);
    }

    public String toStringWithoutAlias()
    {
        return toString(false);
    }

    @Override
    public TableModel clone() throws CloneNotSupportedException
    {
        return (TableModel) super.clone();
    }
}
