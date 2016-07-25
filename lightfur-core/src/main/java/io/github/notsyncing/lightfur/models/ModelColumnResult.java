package io.github.notsyncing.lightfur.models;

import io.github.notsyncing.lightfur.annotations.entity.Column;
import io.github.notsyncing.lightfur.sql.base.SQLPart;
import io.github.notsyncing.lightfur.sql.models.ColumnModel;
import io.github.notsyncing.lightfur.sql.models.TableModel;

import java.util.ArrayList;
import java.util.List;

public class ModelColumnResult
{
    private TableModel table;
    private List<SQLPart> columns = new ArrayList<>();

    public TableModel getTable()
    {
        return table;
    }

    public void setTable(TableModel table)
    {
        this.table = table;
    }

    public List<SQLPart> getColumns()
    {
        return columns;
    }

    public void setColumns(List<SQLPart> columns)
    {
        this.columns = columns;
    }

    public SQLPart getKeyColumn()
    {
        return columns.stream()
                .filter(c -> (c instanceof ColumnModel) && ((ColumnModel)c).isPrimaryKey())
                .map(c -> (ColumnModel)c)
                .findFirst()
                .orElse(null);
    }
}
