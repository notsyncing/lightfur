package io.github.notsyncing.lightfur.dsl;

public abstract class DataContext
{
    private String tag;
    private String sql;

    protected DataContext(String tag, String sql)
    {
        this.tag = tag;
        this.sql = sql;
    }

    public String getTag()
    {
        return tag;
    }

    public void setTag(String tag)
    {
        this.tag = tag;
    }

    public String getSql()
    {
        return sql;
    }

    public void setSql(String sql)
    {
        this.sql = sql;
    }
}
