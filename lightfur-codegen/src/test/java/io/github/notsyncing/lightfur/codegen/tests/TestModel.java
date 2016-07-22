package io.github.notsyncing.lightfur.codegen.tests;

import io.github.notsyncing.lightfur.annotations.entity.Column;
import io.github.notsyncing.lightfur.annotations.entity.Table;
import io.github.notsyncing.lightfur.entity.DataModel;

@Table("test_table")
public class TestModel implements DataModel
{
    @Column("id")
    public int id;

    @Column("name")
    public String name;
}
