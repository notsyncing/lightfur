package io.github.notsyncing.lightfur.entity.dsl

import io.github.notsyncing.lightfur.entity.EntityModel
import io.github.notsyncing.lightfur.sql.base.ExpressionBuilder
import io.github.notsyncing.lightfur.sql.builders.DeleteQueryBuilder

class EntityDeleteDSL<F: EntityModel>(val deleteModel: F) : EntityBaseDSL<F>(deleteModel, isQuery = true) {
    override val builder = DeleteQueryBuilder()

    init {
        val t = getTableModelFromEntityModel(deleteModel)
        builder.from(t)
    }

    fun using(model: EntityModel): EntityDeleteDSL<F> {
        builder.using(getTableModelFromEntityModel(model))
        return this
    }

    fun where(conditions: () -> ExpressionBuilder): EntityDeleteDSL<F> {
        builder.where(conditions())

        return this
    }
}