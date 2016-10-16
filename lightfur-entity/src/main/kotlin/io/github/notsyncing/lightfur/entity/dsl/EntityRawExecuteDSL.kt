package io.github.notsyncing.lightfur.entity.dsl

import io.github.notsyncing.lightfur.entity.EntityModel
import io.github.notsyncing.lightfur.sql.base.SQLPart

class EntityRawExecuteDSL(val sql: String,
                          val params: Array<Any?>) : EntityBaseDSL<EntityModel>(null, isQuery = false, isInsert = false) {
    override val builder: SQLPart
        get() = throw UnsupportedOperationException()

    override fun toSQL(): String {
        return sql
    }

    override fun toSQLParameters(): MutableList<Any?>? {
        return params.toMutableList()
    }
}