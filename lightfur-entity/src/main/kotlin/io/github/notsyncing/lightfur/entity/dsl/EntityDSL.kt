package io.github.notsyncing.lightfur.entity.dsl

import io.github.notsyncing.lightfur.entity.EntityModel


object EntityDSL {
    fun <F: EntityModel> select(resultModel: F) = EntitySelectDSL(resultModel)

    fun <F: EntityModel> insert(insertModel: F) = EntityInsertDSL(insertModel)

    fun <F: EntityModel> update(updateModel: F) = EntityUpdateDSL(updateModel)

    fun <F: EntityModel> delete(deleteModel: F) = EntityDeleteDSL(deleteModel)

    fun <F: EntityModel> rawQuery(resultModel: F, sql: String, vararg params: Any?) = EntityRawQueryDSL(resultModel, sql, params as Array<Any?>)

    fun rawExecute(sql: String, vararg params: Any?) = EntityRawExecuteDSL(sql, params as Array<Any?>)
}