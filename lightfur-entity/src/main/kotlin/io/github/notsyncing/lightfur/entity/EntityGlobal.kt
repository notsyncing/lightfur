package io.github.notsyncing.lightfur.entity

import io.github.notsyncing.lightfur.entity.dsl.EntityBaseDSL
import io.github.notsyncing.lightfur.entity.utils.removeIf
import io.github.notsyncing.lightfur.sql.models.TableModel
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