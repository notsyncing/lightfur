package io.github.notsyncing.lightfur.entity

import io.github.notsyncing.lightfur.sql.models.TableModel
import java.util.concurrent.ConcurrentHashMap

object EntityGlobal {
    val tableModels = ConcurrentHashMap<Class<EntityModel>, TableModel>()

    fun reset() {
        tableModels.clear()
    }
}