package io.github.notsyncing.lightfur.entity.functions

import io.github.notsyncing.lightfur.sql.base.ExpressionBuilder

fun now(): ExpressionBuilder {
    return function("now")
}