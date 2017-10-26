package io.github.notsyncing.lightfur.entity.dsl

import io.github.notsyncing.lightfur.entity.EntityModel
import io.github.notsyncing.lightfur.sql.base.ExpressionBuilder
import io.github.notsyncing.lightfur.sql.builders.DeleteQueryBuilder
import java.security.InvalidParameterException

class EntityDeleteDSL<F: EntityModel>(val deleteModel: F) : EntityBaseDSL<F>(deleteModel) {
    override val builder = DeleteQueryBuilder()

    private var firstWhere = true
    private var skippedSomePrimaryKeyFields = false

    init {
        val t = getTableModelFromEntityModel(deleteModel)
        builder.from(t)

        for (v in deleteModel.primaryKeyFieldInfos) {
            val c = getColumnModelFromEntityFieldInfo(v)
            val fieldInfo = deleteModel.fieldMap[v.inner.name]!!

            val value = fieldInfo.data

            if ((value == null) && (!fieldInfo.nullable)) {
                skippedSomePrimaryKeyFields = true
                continue
            }

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

    override fun toSQL(): String {
        if ((firstWhere) && (skippedSomePrimaryKeyFields)) {
            throw InvalidParameterException("You have neither specified a where condition, " +
                    "nor filled all the primary key fields in $finalModel")
        }

        return super.toSQL()
    }
}