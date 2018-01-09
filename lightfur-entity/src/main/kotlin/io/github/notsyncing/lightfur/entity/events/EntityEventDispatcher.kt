package io.github.notsyncing.lightfur.entity.events

import io.github.notsyncing.lightfur.DataSession
import kotlinx.coroutines.experimental.future.await
import kotlinx.coroutines.experimental.future.future

object EntityEventDispatcher {
    private var handlerResolver: EntityEventHandlerResolver? = null

    fun dispatch(db: DataSession<*, *, *>, event: EntityEvent) = future {
        if (handlerResolver == null) {
            throw UnsupportedOperationException("You must specify a ${EntityEventHandlerResolver::class.java} " +
                    "to be able to dispatch entity events!")
        }

        val handlers = handlerResolver!!.resolve(event::class.java) as List<EntityEventHandler<EntityEvent>>

        for (handler in handlers) {
            handler.handle(db, event).await()
        }
    }

    fun dispatch(db: DataSession<*, *, *>, events: Collection<EntityEvent>) = future {
        for (e in events) {
            dispatch(db, e).await()
        }
    }
}