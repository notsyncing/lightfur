package io.github.notsyncing.lightfur.ql

import io.github.notsyncing.lightfur.entity.dsl.EntitySelectDSL
import java.util.concurrent.CompletableFuture

interface RawQueryProcessor {
    fun resultSetToList(resultSet: Any?): List<Map<String, Any?>>

    fun processValue(value: Any?): Any?

    fun query(dsl: EntitySelectDSL<*>): CompletableFuture<Any?>

    fun end(): CompletableFuture<Unit>
}