package io.github.notsyncing.lightfur.tests.builders;

import io.github.notsyncing.lightfur.sql.SQLBuilder;
import io.github.notsyncing.lightfur.sql.base.ExpressionBuilder;
import io.github.notsyncing.lightfur.sql.builders.QueryBuilder;
import io.github.notsyncing.lightfur.sql.models.ColumnModel;
import io.github.notsyncing.lightfur.sql.models.OrderByColumnInfo;
import io.github.notsyncing.lightfur.sql.models.TableModel;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class SQLBuilderSelectTest
{
    private TableModel tableA = new TableModel();
    private ColumnModel columnId_A = new ColumnModel(tableA);
    private ColumnModel columnName_A = new ColumnModel(tableA);

    private TableModel tableB = new TableModel();
    private ColumnModel columnId_B = new ColumnModel(tableB);
    private ColumnModel columnAId_B = new ColumnModel(tableB);
    private ColumnModel columnFlag_B = new ColumnModel(tableB);

    public SQLBuilderSelectTest()
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
    public void testSimpleQuery()
    {
        String sql = SQLBuilder.select(columnId_A, columnName_A).from(tableA).toString();

        String expected = "SELECT \"test_table\".\"id\", \"test_table\".\"name\"\n" +
                "FROM \"test_table\"";

        assertEquals(expected, sql);
    }

    @Test
    public void testSimpleQueryWithConditions()
    {
        String sql = SQLBuilder.select(columnId_A, columnName_A)
                .from(tableA)
                .where(new ExpressionBuilder().expr(columnId_A).gt().literal(0))
                .toString();

        String expected = "SELECT \"test_table\".\"id\", \"test_table\".\"name\"\n" +
                "FROM \"test_table\"\n" +
                "WHERE (\"test_table\".\"id\" > 0)";

        assertEquals(expected, sql);
    }

    @Test
    public void testSimpleJoinQuery()
    {
        String sql = SQLBuilder.select(columnFlag_B)
                .from(tableA)
                .leftJoin(tableB, new ExpressionBuilder().expr(columnAId_B).eq().expr(columnId_A))
                .toString();

        String expected = "SELECT \"test_table_sub\".\"flag\"\n" +
                "FROM \"test_table\"\n" +
                "LEFT JOIN (\"test_table_sub\") ON \"test_table_sub\".\"p_id\" = \"test_table\".\"id\"";

        assertEquals(expected, sql);
    }

    @Test
    public void testSimpleQueryWithOffsetAndOrder()
    {
        QueryBuilder b = SQLBuilder.select(columnId_A, columnName_A)
                .from(tableA)
                .orderBy(new OrderByColumnInfo(columnId_A, true))
                .limit(10).offset(20);
        String sql = b.toString();
        List<Object> params = b.getParameters();

        String expected = "SELECT \"test_table\".\"id\", \"test_table\".\"name\"\n" +
                "FROM \"test_table\"\n" +
                "ORDER BY \"test_table\".\"id\" DESC\n" +
                "LIMIT ?\n" +
                "OFFSET ?";

        assertEquals(expected, sql);
        assertArrayEquals(new Object[] { 10, 20 }, params.toArray());
    }

    @Test
    public void testSimpleConditions()
    {
        String sql = new ExpressionBuilder().beginGroup().expr(columnId_A).gt().literal(1).endGroup()
                .and().beginGroup().expr(columnName_A).like().expr(columnId_A).endGroup()
                .toString();

        String expected = "(\"test_table\".\"id\" > 1) AND (\"test_table\".\"name\" LIKE \"test_table\".\"id\")";

        assertEquals(expected, sql);
    }
}
