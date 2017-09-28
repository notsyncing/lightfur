package io.github.notsyncing.lightfur.entity

import io.github.notsyncing.lightfur.DataSession
import io.github.notsyncing.lightfur.entity.dsl.EntityBaseDSL
import java.util.concurrent.CompletableFuture

interface EntityQueryExecutor<C, R, U> {
    fun execute(dsl: EntityBaseDSL<*>, session: DataSession<C, R, U>? = null): CompletableFuture<Pair<List<Any?>, Int>>

    fun queryRaw(dsl: EntityBaseDSL<*>, session: DataSession<C, R, U>? = null): CompletableFuture<R>
}