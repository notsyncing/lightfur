package io.github.notsyncing.lightfur.entity.events

interface CanFireEvents {
    val events: MutableList<EntityEvent>

    fun fire(event: EntityEvent) {
        events.add(event)
    }
}