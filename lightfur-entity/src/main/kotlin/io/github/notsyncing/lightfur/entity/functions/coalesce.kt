package io.github.notsyncing.lightfur.entity.functions

import io.github.notsyncing.lightfur.entity.EntityFieldInfo
import io.github.notsyncing.lightfur.entity.field
import io.github.notsyncing.lightfur.sql.base.ExpressionBuilder
import io.github.notsyncing.lightfur.sql.base.SQLPart

fun coalesce(field: EntityFieldInfo, defValue: Any?): ExpressionBuilder {
    return function("COALESCE", field, defValue)
}

fun coalesce(expr: ExpressionBuilder, defValue: Any?): ExpressionBuilder {
    return function("COALESCE", expr, defValue)
}

fun coalesce(value: Any?, defValue: Any?): ExpressionBuilder {
    return function("COALESCE", value, defValue)
}
