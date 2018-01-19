package io.github.notsyncing.lightfur.entity.dsl

import io.github.notsyncing.lightfur.entity.EntityField
import io.github.notsyncing.lightfur.entity.EntityModel
import io.github.notsyncing.lightfur.sql.base.ExpressionBuilder
import io.github.notsyncing.lightfur.sql.base.SQLPart
import io.github.notsyncing.lightfur.sql.builders.UpdateQueryBuilder
import java.security.InvalidParameterException

class EntityUpdateDSL<F: EntityModel>(val updateModel: F) : EntityBaseDSL<F>(updateModel) {
    override val builder = UpdateQueryBuilder()

    private var firstWhere = true
    private var skippedSomePrimaryKeyFields = false
    private var parentDsl: EntityBaseDSL<F>? = null

    var skipTableName
        get() = builder.skipTableName
        set(value) {
            builder.skipTableName = value
        }

    constructor(parentDsl: EntityBaseDSL<F>, updateModel: F) : this(updateModel) {
        this.parentDsl = parentDsl
    }

    init {
        builder.on(getTableModelFromEntityModel(updateModel))
    }

    fun set(skipPrimaryKeys: Boolean = false): EntityUpdateDSL<F> {
        updateModel.fieldMap.map { it }
                .filter { it.value.changed }
                .sortedBy { it.key }
                .forEach {
                    val info = updateModel.fieldMap[it.key]!!

                    if (info.dbAutoGenerated) {
                        return@forEach
                    }

                    if ((skipPrimaryKeys) && (info.dbPrimaryKey)) {
                        return@forEach
                    }

                    builder.set(getColumnModelFromEntityField(info), it.value.data)
                }

        for (v in updateModel.primaryKeyFieldInfos) {
            val c = getColumnModelFromEntityField(v)
            val fieldInfo = updateModel.fieldMap[v.name]!!

            val value = fieldInfo.data

            if ((value == null) && (!fieldInfo.nullable)) {
                skippedSomePrimaryKeyFields = true
                continue
            }

            builder.where(ExpressionBuilder().column(c).eq().parameter(value))
        }

        return this
    }

    fun set(f: EntityField<*>, source: EntityField<*>): EntityUpdateDSL<F> {
        builder.set(getColumnModelFromEntityField(f), getColumnModelFromEntityField(source))
        return this
    }

    fun set(f: EntityField<*>, expr: SQLPart): EntityUpdateDSL<F> {
        builder.set(getColumnModelFromEntityField(f), expr)
        return this
    }

    fun set(f: EntityField<*>, value: Any?): EntityUpdateDSL<F> {
        builder.set(getColumnModelFromEntityField(f), value)
        return this
    }

    fun from(model: EntityModel): EntityUpdateDSL<F> {
        builder.from(getTableModelFromEntityModel(model))
        return this
    }

    fun where(conditions: (() -> ExpressionBuilder)?): EntityUpdateDSL<F> {
        if (conditions == null) {
            builder.clearWhere()
            return this
        }

        if (firstWhere) {
            builder.clearWhere()
            firstWhere = false
        }

        builder.where(conditions())

        parentDsl?.requireTableAlias = true

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