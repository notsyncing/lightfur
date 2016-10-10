package io.github.notsyncing.lightfur.entity.dsl

import io.github.notsyncing.lightfur.entity.EntityModel
import io.github.notsyncing.lightfur.sql.builders.InsertQueryBuilder
import io.github.notsyncing.lightfur.sql.builders.SelectQueryBuilder

class EntityInsertDSL<F: EntityModel>(val insertModel: F) : EntityBaseDSL<F>(insertModel, isInsert = true) {
    override val builder = InsertQueryBuilder()

    init {
        val tableModel = getTableModelFromEntityModel(insertModel)
        builder.into(tableModel)

        for (info in insertModel.primaryKeyFieldInfos) {
            builder.returning(getColumnModelFromEntityFieldInfo(info))
        }
    }

    fun select(query: EntitySelectDSL<*>): EntityInsertDSL<F> {
        insertModel.fieldInfo.map { p -> getColumnModelFromEntityFieldInfo(p.value) }
                .filter { c -> !c.isAutoGenerated }
                .sortedBy { it.fieldName }
                .forEach { c -> builder.column(c) }

        builder.select(query.toSQLPart() as SelectQueryBuilder)
        return this
    }

    fun values(data: EntityModel? = null): EntityInsertDSL<F> {
        val d = data ?: insertModel

        d.fieldInfo.map { it }
                .sortedBy { it.value.name }
                .forEach {
                    if (it.value.dbAutoGenerated) {
                        return@forEach
                    }

                    builder.column(getColumnModelFromEntityFieldInfo(it.value), d.changedDataMap[it.key])
                }

        return this
    }

    fun skipExisting(): EntityInsertDSL<F> {
        builder.skipExisting()
        return this
    }

    fun whenExists(alterOp: EntityUpdateDSL<*>): EntityInsertDSL<F> {
        builder.whenExists(alterOp.toSQLPart())
        return this
    }
}