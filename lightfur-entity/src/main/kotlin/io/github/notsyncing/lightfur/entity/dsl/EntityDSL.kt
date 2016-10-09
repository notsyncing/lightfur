package io.github.notsyncing.lightfur.entity.dsl

import io.github.notsyncing.lightfur.entity.EntityModel

object EntityDSL {
    fun <F: EntityModel> select(resultModel: F) = EntitySelectDSL(resultModel)

    fun <F: EntityModel> insert(insertModel: F) = EntityInsertDSL(insertModel)

    fun <F: EntityModel> update(updateModel: F) = EntityUpdateDSL(updateModel)

    fun <F: EntityModel> delete(deleteModel: F) = EntityDeleteDSL(deleteModel)
}