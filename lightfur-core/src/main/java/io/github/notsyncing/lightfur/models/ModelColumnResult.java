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
    private List<ColumnModel> columns = new ArrayList<>();

    public TableModel getTable()
    {
        return table;
    }

    public void setTable(TableModel table)
    {
        this.table = table;
    }

    public List<ColumnModel> getColumns()
    {
        return columns;
    }

    public void setColumns(List<ColumnModel> columns)
    {
        this.columns = columns;
    }

    public ColumnModel getKeyColumn()
    {
        return columns.stream()
                .filter(ColumnModel::isPrimaryKey)
                .findFirst()
                .orElse(null);
    }
}
