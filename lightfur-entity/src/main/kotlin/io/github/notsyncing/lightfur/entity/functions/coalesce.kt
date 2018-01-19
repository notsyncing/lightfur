package io.github.notsyncing.lightfur.entity.functions

import io.github.notsyncing.lightfur.entity.EntityField
import io.github.notsyncing.lightfur.sql.base.ExpressionBuilder

fun coalesce(field: EntityField<*>, defValue: Any?): ExpressionBuilder {
    return function("COALESCE", field, defValue)
}

fun coalesce(expr: ExpressionBuilder, defValue: Any?): ExpressionBuilder {
    return function("COALESCE", expr, defValue)
}

fun coalesce(value: Any?, defValue: Any?): ExpressionBuilder {
    return function("COALESCE", value, defValue)
}
