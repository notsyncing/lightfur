package io.github.notsyncing.lightfur.tests;

import io.github.notsyncing.lightfur.sql.SQLBuilder;
import io.github.notsyncing.lightfur.sql.base.ConditionBuilder;
import io.github.notsyncing.lightfur.sql.models.ColumnModel;
import io.github.notsyncing.lightfur.sql.models.OrderByColumnInfo;
import io.github.notsyncing.lightfur.sql.models.TableModel;
import io.vertx.core.impl.verticle.PackageHelper;
import org.junit.Test;

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
                .where(new ConditionBuilder().expr(columnId_A).gt().expr("0"))
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
                .leftJoin(tableB, new ConditionBuilder().expr(columnAId_B).eq().expr(columnId_A))
                .toString();

        String expected = "SELECT \"test_table_sub\".\"flag\"\n" +
                "FROM \"test_table\"\n" +
                "LEFT JOIN (\"test_table_sub\") ON \"test_table_sub\".\"p_id\" = \"test_table\".\"id\"";

        assertEquals(expected, sql);
    }

    @Test
    public void testSimpleQueryWithOffsetAndOrder()
    {
        String sql = SQLBuilder.select(columnId_A, columnName_A)
                .from(tableA)
                .orderBy(new OrderByColumnInfo(columnId_A, true))
                .limit(10).offset(20)
                .toString();

        String expected = "SELECT \"test_table\".\"id\", \"test_table\".\"name\"\n" +
                "FROM \"test_table\"\n" +
                "ORDER BY \"test_table\".\"id\" DESC\n" +
                "LIMIT 10\n" +
                "OFFSET 20";

        assertEquals(expected, sql);
    }

    @Test
    public void testSimpleConditions()
    {
        String sql = new ConditionBuilder().beginGroup().expr(columnId_A).gt().expr("1").endGroup()
                .and().beginGroup().expr(columnName_A).like().expr(columnId_A).endGroup()
                .toString();

        String expected = "(\"test_table\".\"id\" > 1) AND (\"test_table\".\"name\" LIKE \"test_table\".\"id\")";

        assertEquals(expected, sql);
    }
}
