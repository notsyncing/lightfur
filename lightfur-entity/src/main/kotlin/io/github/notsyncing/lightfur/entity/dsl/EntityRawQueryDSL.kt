package io.github.notsyncing.lightfur.entity.dsl

import io.github.notsyncing.lightfur.entity.EntityModel
import io.github.notsyncing.lightfur.sql.base.SQLPart

open class EntityRawQueryDSL<F: EntityModel>(val model: F,
                                             val sql: String,
                                             val params: Array<Any?>) : EntityBaseDSL<F>(model, isQuery = true) {
    override val builder: SQLPart
        get() = throw UnsupportedOperationException()

    override fun toSQL(): String {
        return sql
    }

    override fun toSQLParameters(): MutableList<Any?>? {
        return params.toMutableList()
    }
}