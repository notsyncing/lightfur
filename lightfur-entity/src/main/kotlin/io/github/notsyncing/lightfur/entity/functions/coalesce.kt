package io.github.notsyncing.lightfur.entity.functions

import io.github.notsyncing.lightfur.core.sql.base.ExpressionBuilder
import io.github.notsyncing.lightfur.entity.EntityField

fun coalesce(field: EntityField<*>, defValue: Any?): ExpressionBuilder {
    return function("COALESCE", field, defValue)
}

fun coalesce(expr: ExpressionBuilder, defValue: Any?): ExpressionBuilder {
    return function("COALESCE", expr, defValue)
}

fun coalesce(value: Any?, defValue: Any?): ExpressionBuilder {
    return function("COALESCE", value, defValue)
}
