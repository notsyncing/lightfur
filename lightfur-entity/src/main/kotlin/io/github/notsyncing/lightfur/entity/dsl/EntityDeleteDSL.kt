package io.github.notsyncing.lightfur.entity.dsl

import io.github.notsyncing.lightfur.entity.EntityModel
import io.github.notsyncing.lightfur.sql.base.ExpressionBuilder
import io.github.notsyncing.lightfur.sql.builders.DeleteQueryBuilder

class EntityDeleteDSL<F: EntityModel>(val deleteModel: F) : EntityBaseDSL<F>(deleteModel, isQuery = true) {
    override val builder = DeleteQueryBuilder()

    private var firstWhere = true

    init {
        val t = getTableModelFromEntityModel(deleteModel)
        builder.from(t)

        for (v in deleteModel.primaryKeyFieldInfos) {
            val c = getColumnModelFromEntityFieldInfo(v)
            val value = deleteModel.primaryKeyFields.first { it.name == v.inner.name }.getter.call(deleteModel)

            builder.where(ExpressionBuilder().column(c).eq().parameter(value))
        }
    }

    fun using(model: EntityModel): EntityDeleteDSL<F> {
        builder.using(getTableModelFromEntityModel(model))
        return this
    }

    fun where(conditions: () -> ExpressionBuilder): EntityDeleteDSL<F> {
        if (firstWhere) {
            builder.clearWhere()
            firstWhere = false
        }

        builder.where(conditions())

        return this
    }
}