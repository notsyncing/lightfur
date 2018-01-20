package io.github.notsyncing.lightfur.integration.jdbc.ql

import io.github.notsyncing.lightfur.core.DataSession
import io.github.notsyncing.lightfur.entity.dsl.EntitySelectDSL
import io.github.notsyncing.lightfur.integration.jdbc.JdbcDataSession
import io.github.notsyncing.lightfur.ql.RawQueryProcessor
import kotlinx.coroutines.experimental.future.await
import kotlinx.coroutines.experimental.future.future
import java.sql.Array
import java.sql.ResultSet
import java.util.concurrent.CompletableFuture

class JdbcRawQueryProcessor : RawQueryProcessor {
    private lateinit var db: JdbcDataSession

    private fun currentRowToMap(rs: ResultSet): Map<String, Any?> {
        val count = rs.metaData.columnCount
        val map = mutableMapOf<String, Any?>()

        for (i in 1..count) {
            map.put(rs.metaData.getColumnLabel(i), rs.getObject(i))
        }

        return map
    }

    override fun resultSetToList(resultSet: Any?): List<Map<String, Any?>> {
        if (resultSet is ResultSet) {
            val list = mutableListOf<Map<String, Any?>>()

            while (resultSet.next()) {
                list.add(currentRowToMap(resultSet))
            }

            return list
        } else {
            throw UnsupportedOperationException("$resultSet is not an ${ResultSet::class.java}")
        }
    }

    override fun processValue(value: Any?): Any? {
        if (value is Array) {
            return value.array
        }

        return value
    }

    override fun query(dsl: EntitySelectDSL<*>) = future {
        db = DataSession.start()

        try {
            dsl.queryRaw(db).await()
        } catch (e: Exception) {
            e.printStackTrace()

            db.end().await()

            throw e
        }
    }

    override fun end(): CompletableFuture<Unit> {
        return db.end()
                .thenApply {  }
    }
}