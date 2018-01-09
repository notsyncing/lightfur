package io.github.notsyncing.lightfur.entity

import io.github.notsyncing.lightfur.DataSession
import io.github.notsyncing.lightfur.entity.dsl.EntityRawQueryDSL
import io.github.notsyncing.lightfur.read.ReadModel
import java.util.concurrent.CompletableFuture

interface EntityReadModel<T: EntityModel> : ReadModel<T> {
    fun query(): EntityRawQueryDSL<T>

    override fun get(db: DataSession<*, *, *>): CompletableFuture<T?> {
        return query().executeFirst(db)
    }

    override fun getList(db: DataSession<*, *, *>): CompletableFuture<List<T>> {
        return query().execute(db)
                .thenApply { (l, _) -> l }
    }
}