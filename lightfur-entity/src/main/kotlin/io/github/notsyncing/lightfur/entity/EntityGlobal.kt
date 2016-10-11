package io.github.notsyncing.lightfur.entity

import io.github.notsyncing.lightfur.sql.models.TableModel
import java.util.concurrent.ConcurrentHashMap

object EntityGlobal {
    val tableModels = ConcurrentHashMap<Class<EntityModel>, TableModel>()
    val fieldInfoInners = ConcurrentHashMap<Class<EntityModel>, ConcurrentHashMap<String, EntityFieldInfo.Inner>>()

    fun reset() {
        tableModels.clear()
        fieldInfoInners.clear()
    }
}