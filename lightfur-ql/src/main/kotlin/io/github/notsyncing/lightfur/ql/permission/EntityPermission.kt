package io.github.notsyncing.lightfur.ql.permission

import io.github.notsyncing.lightfur.entity.EntityModel
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class EntityPermission<T: EntityModel>(val entityModel: T) {
    val allowFields = mutableListOf<EntityFieldPermission>()

    fun all(): EntityPermission<T> {
        entityModel.javaClass.kotlin.memberProperties
                .filter { it.apply { it.isAccessible = true }.getDelegate(entityModel) != null }
                .forEach { allowFields.add(EntityFieldPermission(it)) }

        return this
    }

    fun include(field: (T) -> KProperty<*>, fieldPermission: (EntityFieldPermission) -> Unit = {}): EntityPermission<T> {
        val f = field(entityModel)
        val perm = EntityFieldPermission(f)

        fieldPermission(perm)

        allowFields.removeIf { it.field.name == f.name }
        allowFields.add(perm)

        return this
    }

    fun exclude(field: (T) -> KProperty<*>): EntityPermission<T> {
        val f = field(entityModel)

        allowFields.removeIf { it.field.name == f.name }

        return this
    }
}