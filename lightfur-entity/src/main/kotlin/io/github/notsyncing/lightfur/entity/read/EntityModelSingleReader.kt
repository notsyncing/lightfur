package io.github.notsyncing.lightfur.entity.read

import io.github.notsyncing.lightfur.core.DataSession
import io.github.notsyncing.lightfur.entity.EntityModel
import java.util.concurrent.CompletableFuture

interface EntityModelSingleReader<T: EntityModel> : EntityModelReader<T> {
    fun get(db: DataSession<*, *, *>): CompletableFuture<T?> {
        return query().executeFirst(db)
    }
}