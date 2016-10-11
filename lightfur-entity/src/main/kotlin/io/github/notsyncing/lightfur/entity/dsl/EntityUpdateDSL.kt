package io.github.notsyncing.lightfur.entity.dsl

import io.github.notsyncing.lightfur.entity.EntityFieldInfo
import io.github.notsyncing.lightfur.entity.EntityModel
import io.github.notsyncing.lightfur.sql.base.ExpressionBuilder
import io.github.notsyncing.lightfur.sql.base.SQLPart
import io.github.notsyncing.lightfur.sql.builders.UpdateQueryBuilder

class EntityUpdateDSL<F: EntityModel>(val updateModel: F) : EntityBaseDSL<F>(updateModel) {
    override val builder = UpdateQueryBuilder()

    private var firstWhere = true

    init {
        builder.on(getTableModelFromEntityModel(updateModel))

        updateModel.fieldMap.map { it }
                .filter { it.value.changed }
                .sortedBy { it.key }
                .forEach {
                    val info = updateModel.fieldMap[it.key]!!.info

                    if (info.inner.dbAutoGenerated) {
                        return@forEach
                    }

                    builder.set(getColumnModelFromEntityFieldInfo(info), it.value.data)
                }

        for ((i, v) in updateModel.primaryKeyFieldInfos.withIndex()) {
            val c = getColumnModelFromEntityFieldInfo(v)
            val value = updateModel.primaryKeyFields[i].get()

            builder.where(ExpressionBuilder().column(c).eq().parameter(value))
        }
    }

    fun set(f: EntityFieldInfo, source: EntityFieldInfo): EntityUpdateDSL<F> {
        builder.set(getColumnModelFromEntityFieldInfo(f), getColumnModelFromEntityFieldInfo(source))
        return this
    }

    fun set(f: EntityFieldInfo, expr: SQLPart): EntityUpdateDSL<F> {
        builder.set(getColumnModelFromEntityFieldInfo(f), expr)
        return this
    }

    fun from(model: EntityModel): EntityUpdateDSL<F> {
        builder.from(getTableModelFromEntityModel(model))
        return this
    }

    fun where(conditions: () -> ExpressionBuilder): EntityUpdateDSL<F> {
        if (firstWhere) {
            builder.clearWhere()
            firstWhere = false
        }

        builder.where(conditions())

        return this
    }
}