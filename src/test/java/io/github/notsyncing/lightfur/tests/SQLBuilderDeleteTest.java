package io.github.notsyncing.lightfur.tests;

import io.github.notsyncing.lightfur.sql.SQLBuilder;
import io.github.notsyncing.lightfur.sql.base.ConditionBuilder;
import io.github.notsyncing.lightfur.sql.base.ExpressionBuilder;
import io.github.notsyncing.lightfur.sql.models.ColumnModel;
import io.github.notsyncing.lightfur.sql.models.TableModel;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SQLBuilderDeleteTest
{
    private TableModel tableA = new TableModel();
    private ColumnModel columnId_A = new ColumnModel(tableA);
    private ColumnModel columnName_A = new ColumnModel(tableA);

    private TableModel tableB = new TableModel();
    private ColumnModel columnId_B = new ColumnModel(tableB);
    private ColumnModel columnAId_B = new ColumnModel(tableB);
    private ColumnModel columnFlag_B = new ColumnModel(tableB);

    public SQLBuilderDeleteTest()
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
    public void testSimpleUpdate()
    {
        String sql = SQLBuilder.delete().from(tableA)
                .where(new ConditionBuilder().expr(columnId_A).gt().expr("2"))
                .toString();

        String expected = "DELETE FROM \"test_table\"\n" +
                "WHERE (\"test_table\".\"id\" > 2)";

        assertEquals(expected, sql);
    }
}
