package io.github.notsyncing.lightfur.entity.events

interface EntityEventHandlerResolver {
    fun <T: EntityEvent> resolve(eventType: Class<T>): List<EntityEventHandler<T>>
}