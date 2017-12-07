package io.github.notsyncing.lightfur.ql.permission

import io.github.notsyncing.lightfur.entity.EntityModel

class QueryPermissions {
    companion object {
        val ALL = QueryPermissions()
    }

    val allowEntities = mutableListOf<EntityPermission<*>>()

    fun <T: EntityModel> entity(entityModel: T, f: (EntityPermission<T>) -> Unit = { it.all() }): QueryPermissions {
        val ent = EntityPermission(entityModel)
        f(ent)
        allowEntities.add(ent)

        return this
    }
}