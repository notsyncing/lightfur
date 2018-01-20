package io.github.notsyncing.lightfur.integration.vertx.ql

import com.alibaba.fastjson.JSON
import io.github.notsyncing.lightfur.entity.dsl.EntitySelectDSL
import io.github.notsyncing.lightfur.ql.RawQueryProcessor
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.sql.ResultSet
import java.util.concurrent.CompletableFuture

class VertxRawQueryProcessor : RawQueryProcessor {
    override fun resultSetToList(resultSet: Any?): List<Map<String, Any?>> {
        if (resultSet is ResultSet) {
            return resultSet.rows.map { it.map }
        } else {
            throw UnsupportedOperationException("$resultSet is not an ${ResultSet::class.java}")
        }
    }

    override fun processValue(value: Any?): Any? {
        if (value is JsonArray) {
            return JSON.parseArray(value.toString())
        } else if (value is JsonObject) {
            return JSON.parseObject(value.toString())
        }

        return value
    }

    override fun query(dsl: EntitySelectDSL<*>): CompletableFuture<Any?> {
        return dsl.queryRaw()
    }

    override fun end(): CompletableFuture<Unit> {
        return CompletableFuture.completedFuture(Unit)
    }
}