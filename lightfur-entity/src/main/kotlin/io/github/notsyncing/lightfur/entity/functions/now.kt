package io.github.notsyncing.lightfur.entity.functions

import io.github.notsyncing.lightfur.core.sql.base.ExpressionBuilder

fun now(): ExpressionBuilder {
    return function("now")
}