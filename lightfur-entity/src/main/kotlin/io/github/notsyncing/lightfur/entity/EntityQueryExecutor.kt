package io.github.notsyncing.lightfur.entity

import io.github.notsyncing.lightfur.DataSession
import io.github.notsyncing.lightfur.entity.dsl.EntityBaseDSL
import java.util.concurrent.CompletableFuture

interface EntityQueryExecutor<R, U> {
    fun execute(dsl: EntityBaseDSL<*>, session: DataSession<R, U>? = null): CompletableFuture<Pair<List<Any?>, Int>>

    fun queryRaw(dsl: EntityBaseDSL<*>, session: DataSession<R, U>? = null): CompletableFuture<R>
}