package io.github.notsyncing.lightfur.entity.dsl

import io.github.notsyncing.lightfur.core.sql.base.SQLPart
import io.github.notsyncing.lightfur.entity.EntityModel

open class EntityRawQueryDSL<F: EntityModel>(val model: F,
                                             val sql: String,
                                             val params: Array<Any?>) : EntityBaseDSL<F>(model, isQuery = true) {
    override val builder: SQLPart
        get() = throw UnsupportedOperationException()

    override fun toSQL(): String {
        if (sql.isBlank()) {
            return super.toSQL()
        }

        return sql
    }

    override fun toSQLParameters(): MutableList<Any?>? {
        if (sql.isBlank()) {
            return super.toSQLParameters()
        }

        return params.toMutableList()
    }
}