package io.github.notsyncing.lightfur.entity

import io.github.notsyncing.lightfur.core.sql.models.TableModel
import io.github.notsyncing.lightfur.entity.dsl.EntityBaseDSL
import io.github.notsyncing.lightfur.entity.utils.removeIf
import java.util.concurrent.ConcurrentHashMap

object EntityGlobal {
    val tableModels = ConcurrentHashMap<Class<EntityModel>, TableModel>()
    val sqlCache = ConcurrentHashMap<String, String>()

    fun removeClassFromCacheIf(predicate: (Class<EntityModel>) -> Boolean) {
        tableModels.removeIf { (clazz, _) -> predicate(clazz) }
    }

    fun reset() {
        tableModels.clear()
        sqlCache.clear()
    }

    fun setQueryExecutor(e: EntityQueryExecutor<*, *, *>) {
        return EntityBaseDSL.setQueryExecutor(e)
    }
}