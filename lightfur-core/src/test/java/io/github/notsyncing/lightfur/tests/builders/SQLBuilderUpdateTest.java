package io.github.notsyncing.lightfur.tests.builders;

import io.github.notsyncing.lightfur.sql.SQLBuilder;
import io.github.notsyncing.lightfur.sql.base.ExpressionBuilder;
import io.github.notsyncing.lightfur.sql.models.ColumnModel;
import io.github.notsyncing.lightfur.sql.models.TableModel;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SQLBuilderUpdateTest
{
    private TableModel tableA = new TableModel();
    private ColumnModel columnId_A = new ColumnModel(tableA);
    private ColumnModel columnName_A = new ColumnModel(tableA);
    private ColumnModel columnNameCast_A = new ColumnModel(tableA);

    private TableModel tableB = new TableModel();
    private ColumnModel columnId_B = new ColumnModel(tableB);
    private ColumnModel columnAId_B = new ColumnModel(tableB);
    private ColumnModel columnFlag_B = new ColumnModel(tableB);

    public SQLBuilderUpdateTest()
    {
        tableA.setName("test_table");

        columnId_A.setColumn("id");
        columnName_A.setColumn("name");
        columnNameCast_A.setColumn("name");
        columnNameCast_A.setFieldType("text");

        tableB.setName("test_table_sub");

        columnId_B.setColumn("id");
        columnAId_B.setColumn("p_id");
        columnFlag_B.setColumn("flag");
    }

    @Test
    public void testSimpleUpdate()
    {
        String sql = SQLBuilder.update(tableA).set(columnId_A, new ExpressionBuilder().literal("1"))
                .set(columnName_A, columnId_A)
                .toString();

        String expected = "UPDATE \"test_table\"\n" +
                "SET \"id\" = ('1'), \"name\" = (\"id\")";

        assertEquals(expected, sql);
    }

    @Test
    public void testSimpleUpdateWithCast()
    {
        String sql = SQLBuilder.update(tableA).set(columnId_A, new ExpressionBuilder().literal("1"))
                .set(columnNameCast_A, columnId_A)
                .toString();

        String expected = "UPDATE \"test_table\"\n" +
                "SET \"id\" = ('1'), \"name\" = (\"id\")::text";

        assertEquals(expected, sql);
    }

    @Test
    public void testConditionalUpdate()
    {
        String sql = SQLBuilder.update(tableA).set(columnId_A, new ExpressionBuilder().literal("1"))
                .set(columnName_A, columnId_A)
                .where(new ExpressionBuilder().column(columnId_A).gt().literal(1))
                .toString();

        String expected = "UPDATE \"test_table\"\n" +
                "SET \"id\" = ('1'), \"name\" = (\"id\")\n" +
                "WHERE (\"test_table\".\"id\" > 1)";

        assertEquals(expected, sql);
    }
}
