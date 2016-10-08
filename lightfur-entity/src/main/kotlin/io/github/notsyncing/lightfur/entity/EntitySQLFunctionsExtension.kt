package io.github.notsyncing.lightfur.entity

import io.github.notsyncing.lightfur.sql.base.ExpressionBuilder
import io.github.notsyncing.lightfur.sql.base.SQLPart

private fun function(func: String, field: EntityFieldInfo): ExpressionBuilder {
    return ExpressionBuilder().beginFunction(func)
            .field(field)
            .endFunction()
}

private fun function(func: String, expr: SQLPart): ExpressionBuilder {
    return ExpressionBuilder().beginFunction(func)
            .expr(expr)
            .endFunction()
}

fun sum(field: EntityFieldInfo): ExpressionBuilder {
    return function("SUM", field)
}

fun sum(expr: ExpressionBuilder): ExpressionBuilder {
    return function("SUM", expr)
}

fun count(field: EntityFieldInfo): ExpressionBuilder {
    return function("COUNT", field)
}

fun count(expr: ExpressionBuilder): ExpressionBuilder {
    return function("COUNT", expr)
}

fun count(): ExpressionBuilder {
    return ExpressionBuilder().beginFunction("COUNT")
            .literal("*")
            .endFunction()
}