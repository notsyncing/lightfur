package io.github.notsyncing.lightfur.entity.dsl

import io.github.notsyncing.lightfur.entity.EntityModel
import io.github.notsyncing.lightfur.sql.builders.InsertQueryBuilder

// TODO: Write primary key back to insertModel after insertion

class EntityInsertDSL(val insertModel: EntityModel) : EntityBaseDSL() {
    private val builder = InsertQueryBuilder()

    init {
        val tableModel = getTableModelFromEntityModel(insertModel)
        builder.into(tableModel)
    }

    fun select(query: EntitySelectDSL): EntityInsertDSL {
        insertModel.fieldInfo.map { p -> getColumnModelFromEntityFieldInfo(p.value) }
                .forEach { c -> builder.column(c) }

        builder.select(query.toSQLPart())
        return this
    }

    fun values(data: EntityModel? = null): EntityInsertDSL {
        val d = data ?: insertModel
        d.fieldInfo.forEach { builder.column(getColumnModelFromEntityFieldInfo(it.value), d.changedDataMap[it.key]) }

        return this
    }

    override fun toSQLPart() = builder
}