package io.github.notsyncing.lightfur.integration.jdbc.ql

import com.alibaba.fastjson.JSON
import io.github.notsyncing.lightfur.ql.RawQueryResultProcessor
import java.sql.Array
import java.sql.ResultSet

class JdbcRawQueryResultProcessor : RawQueryResultProcessor {
    private fun currentRowToMap(rs: ResultSet): Map<String, Any?> {
        val count = rs.metaData.columnCount
        val map = mutableMapOf<String, Any?>()

        for (i in 0 until count) {
            map.put(rs.metaData.getColumnLabel(i), rs.getObject(i))
        }

        return map
    }

    override fun resultSetToList(resultSet: Any?): List<Map<String, Any?>> {
        if (resultSet is ResultSet) {
            val list = mutableListOf<Map<String, Any?>>()

            while (!resultSet.next()) {
                list.add(currentRowToMap(resultSet))
            }

            return list
        } else {
            throw UnsupportedOperationException("$resultSet is not an ${ResultSet::class.java}")
        }
    }

    override fun processValue(value: Any?): Any? {
        if (value is Array) {

        }

        return value
    }
}