package io.github.notsyncing.lightfur.entity.read

import io.github.notsyncing.lightfur.core.DataSession
import io.github.notsyncing.lightfur.entity.EntityModel
import java.util.concurrent.CompletableFuture

interface EntityModelListReader<T: EntityModel> : EntityModelReader<T> {
    fun getList(db: DataSession<*, *, *>): CompletableFuture<List<T>> {
        return query().execute(db)
                .thenApply { (l, _) -> l }
    }
}