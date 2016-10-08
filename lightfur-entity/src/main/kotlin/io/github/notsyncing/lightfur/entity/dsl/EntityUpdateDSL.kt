package io.github.notsyncing.lightfur.entity.dsl

import io.github.notsyncing.lightfur.entity.EntityFieldInfo
import io.github.notsyncing.lightfur.entity.EntityModel
import io.github.notsyncing.lightfur.sql.base.ExpressionBuilder
import io.github.notsyncing.lightfur.sql.base.SQLPart
import io.github.notsyncing.lightfur.sql.builders.UpdateQueryBuilder

class EntityUpdateDSL(val updateModel: EntityModel) : EntityBaseDSL() {
    private val builder = UpdateQueryBuilder()

    init {
        builder.on(getTableModelFromEntityModel(updateModel))

        updateModel.changedDataMap.forEach {
            val c = getColumnModelFromEntityFieldInfo(updateModel.fieldInfo[it.key]!!)
            builder.set(c, it.value)
        }
    }

    fun set(f: EntityFieldInfo, source: EntityFieldInfo): EntityUpdateDSL {
        builder.set(getColumnModelFromEntityFieldInfo(f), getColumnModelFromEntityFieldInfo(source))
        return this
    }

    fun set(f: EntityFieldInfo, expr: SQLPart): EntityUpdateDSL {
        builder.set(getColumnModelFromEntityFieldInfo(f), expr)
        return this
    }

    fun from(model: EntityModel): EntityUpdateDSL {
        builder.from(getTableModelFromEntityModel(model))
        return this
    }

    fun where(conditions: () -> ExpressionBuilder): EntityUpdateDSL {
        builder.where(conditions())

        return this
    }

    override fun toSQLPart() = builder
}