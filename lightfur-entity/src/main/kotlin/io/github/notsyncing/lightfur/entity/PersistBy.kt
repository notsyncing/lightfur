package io.github.notsyncing.lightfur.entity

interface PersistBy<T: EntityModel> {
    fun toPersistenceModel(): T {
        return this as T
    }
}