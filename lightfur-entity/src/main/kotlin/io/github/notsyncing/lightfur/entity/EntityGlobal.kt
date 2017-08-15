package io.github.notsyncing.lightfur.entity

import io.github.notsyncing.lightfur.entity.utils.removeIf
import io.github.notsyncing.lightfur.sql.models.TableModel
import java.util.concurrent.ConcurrentHashMap

object EntityGlobal {
    val tableModels = ConcurrentHashMap<Class<EntityModel>, TableModel>()
    val fieldInfoInners = ConcurrentHashMap<Class<EntityModel>, ConcurrentHashMap<String, EntityFieldInfo.Inner>>()
    val sqlCache = ConcurrentHashMap<String, String>()

    fun removeClassFromCacheIf(predicate: (Class<EntityModel>) -> Boolean) {
        tableModels.removeIf { (clazz, _) -> predicate(clazz) }
        fieldInfoInners.removeIf { (clazz, _) -> predicate(clazz) }
    }

    fun reset() {
        tableModels.clear()
        fieldInfoInners.clear()
        sqlCache.clear()
    }
}