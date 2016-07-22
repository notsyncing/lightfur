package io.github.notsyncing.lightfur.codegen.tests;

import io.github.notsyncing.lightfur.annotations.entity.Column;
import io.github.notsyncing.lightfur.annotations.entity.Table;
import io.github.notsyncing.lightfur.entity.TableDefineModel;

@Table("test_table")
class TestTable implements TableDefineModel
{
    @Column("id")
    public int id;

    @Column("name")
    public String name;
}
