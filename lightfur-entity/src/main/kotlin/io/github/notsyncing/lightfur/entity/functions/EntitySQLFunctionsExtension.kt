package io.github.notsyncing.lightfur.entity.functions

import io.github.notsyncing.lightfur.core.sql.base.ExpressionBuilder
import io.github.notsyncing.lightfur.core.sql.base.SQLPart
import io.github.notsyncing.lightfur.entity.EntityField
import io.github.notsyncing.lightfur.entity.field

fun function(func: String, vararg param: Any?): ExpressionBuilder {
    val b = ExpressionBuilder().beginFunction(func)

    if (param.isNotEmpty()) {
        for (p in param) {
            if (p is EntityField<*>) {
                b.field(p).separator()
            } else if (p is SQLPart) {
                b.expr(p).separator()
            } else if (p is Pair<*, *>) {
                val (rp, castTo) = p
                b.parameter(rp, castTo as String).separator()
            } else {
                b.parameter(p).separator()
            }
        }
    }

    b.endFunction()

    return b
}

fun sum(field: EntityField<*>): ExpressionBuilder {
    return function("SUM", field)
}

fun sum(expr: ExpressionBuilder): ExpressionBuilder {
    return function("SUM", expr)
}

fun count(field: EntityField<*>): ExpressionBuilder {
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