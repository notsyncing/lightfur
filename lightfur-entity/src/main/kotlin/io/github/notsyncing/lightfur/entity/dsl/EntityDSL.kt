package io.github.notsyncing.lightfur.entity.dsl

import io.github.notsyncing.lightfur.entity.EntityModel

object EntityDSL {
    fun select(resultModel: EntityModel) = EntitySelectDSL(resultModel)

    fun insert(insertModel: EntityModel) = EntityInsertDSL(insertModel)

    fun update(updateModel: EntityModel) = EntityUpdateDSL(updateModel)

    fun delete(deleteModel: EntityModel) = EntityDeleteDSL(deleteModel)
}