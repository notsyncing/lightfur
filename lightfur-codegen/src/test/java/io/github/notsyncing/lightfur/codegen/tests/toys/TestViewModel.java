package io.github.notsyncing.lightfur.codegen.tests.toys;

import io.github.notsyncing.lightfur.annotations.entity.Column;
import io.github.notsyncing.lightfur.annotations.entity.PrimaryKey;
import io.github.notsyncing.lightfur.annotations.entity.Table;
import io.github.notsyncing.lightfur.entity.DataModel;
import io.github.notsyncing.lightfur.entity.TableDefineModel;

@Table("test_table")
public class TestViewModel implements DataModel, TableDefineModel
{
    @Column("id")
    @PrimaryKey(autoIncrement = true)
    public int id;

    @Column("name")
    public String name;
}
