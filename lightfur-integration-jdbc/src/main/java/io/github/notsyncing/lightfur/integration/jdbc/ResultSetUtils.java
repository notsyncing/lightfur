package io.github.notsyncing.lightfur.integration.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ResultSetUtils {
    public static int findColumnIndex(ResultSet rs, String columnLabel) throws SQLException {
        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
            if (rs.getMetaData().getColumnLabel(i).equals(columnLabel)) {
                return i;
            }
        }

        return 0;
    }
}
