package io.github.notsyncing.lightfur.tests;

import io.github.notsyncing.lightfur.sql.SQLBuilder;
import io.github.notsyncing.lightfur.sql.base.ConditionBuilder;
import io.github.notsyncing.lightfur.sql.builders.SelectQueryBuilder;
import io.github.notsyncing.lightfur.sql.models.ColumnModel;
import io.github.notsyncing.lightfur.sql.models.OrderByColumnInfo;
import io.github.notsyncing.lightfur.sql.models.TableModel;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SQLBuilderInsertTest
{
    private TableModel tableA = new TableModel();
    private ColumnModel columnId_A = new ColumnModel(tableA);
    private ColumnModel columnName_A = new ColumnModel(tableA);

    private TableModel tableB = new TableModel();
    private ColumnModel columnId_B = new ColumnModel(tableB);
    private ColumnModel columnAId_B = new ColumnModel(tableB);
    private ColumnModel columnFlag_B = new ColumnModel(tableB);

    public SQLBuilderInsertTest()
    {
        tableA.setName("test_table");

        columnId_A.setColumn("id");
        columnName_A.setColumn("name");

        tableB.setName("test_table_sub");

        columnId_B.setColumn("id");
        columnAId_B.setColumn("p_id");
        columnFlag_B.setColumn("flag");
    }

    @Test
    public void testSimpleInsert()
    {
        String sql = SQLBuilder.insert().into(tableA)
                .column(columnId_A, "1").column(columnName_A, "a")
                .toString();

        String expected = "INSERT INTO \"test_table\" (\"id\", \"name\")\n" +
                "VALUES ('1', 'a')";

        assertEquals(expected, sql);
    }

    @Test
    public void testSelectInsert()
    {
        String sql = SQLBuilder.insert().into(tableA)
                .column(columnId_A).column(columnName_A)
                .select(new SelectQueryBuilder()
                        .select(columnId_B, columnFlag_B)
                        .from(tableB))
                .toString();

        String expected = "INSERT INTO \"test_table\" (\"id\", \"name\")\n" +
                "SELECT \"test_table_sub\".\"id\", \"test_table_sub\".\"flag\"\n" +
                "FROM \"test_table_sub\"";

        assertEquals(expected, sql);
    }
}
