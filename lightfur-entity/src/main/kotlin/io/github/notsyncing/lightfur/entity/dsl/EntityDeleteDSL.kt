package io.github.notsyncing.lightfur.entity.dsl

import io.github.notsyncing.lightfur.entity.EntityModel
import io.github.notsyncing.lightfur.sql.base.ExpressionBuilder
import io.github.notsyncing.lightfur.sql.builders.DeleteQueryBuilder

class EntityDeleteDSL(val deleteModel: EntityModel) : EntityBaseDSL() {
    override val builder = DeleteQueryBuilder()

    init {
        val t = getTableModelFromEntityModel(deleteModel)
        builder.from(t)
    }

    fun using(model: EntityModel): EntityDeleteDSL {
        builder.using(getTableModelFromEntityModel(model))
        return this
    }

    fun where(conditions: () -> ExpressionBuilder): EntityDeleteDSL {
        builder.where(conditions())

        return this
    }
}