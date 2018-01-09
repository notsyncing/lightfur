package io.github.notsyncing.lightfur.entity.events

import io.github.notsyncing.lightfur.DataSession
import java.util.concurrent.CompletableFuture

interface EntityEventHandler<T: EntityEvent> {
    fun handle(db: DataSession<*, *, *>, event: T): CompletableFuture<Unit>

    fun fire(db: DataSession<*, *, *>, event: EntityEvent): CompletableFuture<Unit> {
        return EntityEventDispatcher.dispatch(db, event)
    }
}