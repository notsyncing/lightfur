package io.github.notsyncing.lightfur.entity

import io.github.notsyncing.lightfur.entity.dsl.EntityBaseDSL
import io.github.notsyncing.lightfur.sql.base.ExpressionBuilder
import io.github.notsyncing.lightfur.sql.models.OrderByColumnInfo

infix fun EntityFieldInfo.eq(value: EntityFieldInfo): ExpressionBuilder {
    val column1 = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)
    val column2 = EntityBaseDSL.getColumnModelFromEntityFieldInfo(value)

    return ExpressionBuilder()
            .beginGroup()
            .column(column1)
            .eq()
            .column(column2)
            .endGroup()
}

infix fun EntityFieldInfo.eq(value: Any?): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)

    val b = ExpressionBuilder()
            .beginGroup()
            .column(column)

    if (value == null) {
        b.eqNull()
    } else {
        b.eq().literal(value.toString())
    }

    return b.endGroup()
}

infix fun EntityFieldInfo.neq(value: EntityFieldInfo): ExpressionBuilder {
    val column1 = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)
    val column2 = EntityBaseDSL.getColumnModelFromEntityFieldInfo(value)

    return ExpressionBuilder()
            .beginGroup()
            .column(column1)
            .ne()
            .column(column2)
            .endGroup()
}

infix fun EntityFieldInfo.neq(value: Any?): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)

    val b = ExpressionBuilder()
            .beginGroup()
            .column(column)

    if (value == null) {
        b.neNull()
    } else {
        b.ne().literal(value.toString())
    }

    return b.endGroup()
}

infix fun EntityFieldInfo.gt(value: EntityFieldInfo): ExpressionBuilder {
    val column1 = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)
    val column2 = EntityBaseDSL.getColumnModelFromEntityFieldInfo(value)

    return ExpressionBuilder()
            .beginGroup()
            .column(column1)
            .gt()
            .column(column2)
            .endGroup()
}

infix fun EntityFieldInfo.gt(value: Any): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)

    return ExpressionBuilder()
            .beginGroup()
            .column(column)
            .gt()
            .literal(value.toString())
            .endGroup()
}

infix fun EntityFieldInfo.lt(value: EntityFieldInfo): ExpressionBuilder {
    val column1 = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)
    val column2 = EntityBaseDSL.getColumnModelFromEntityFieldInfo(value)

    return ExpressionBuilder()
            .beginGroup()
            .column(column1)
            .lt()
            .column(column2)
            .endGroup()
}

infix fun EntityFieldInfo.lt(value: Any): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)

    return ExpressionBuilder()
            .beginGroup()
            .column(column)
            .lt()
            .literal(value.toString())
            .endGroup()
}

infix fun EntityFieldInfo.gte(value: EntityFieldInfo): ExpressionBuilder {
    val column1 = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)
    val column2 = EntityBaseDSL.getColumnModelFromEntityFieldInfo(value)

    return ExpressionBuilder()
            .beginGroup()
            .column(column1)
            .gte()
            .column(column2)
            .endGroup()
}

infix fun EntityFieldInfo.gte(value: Any): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)

    return ExpressionBuilder()
            .beginGroup()
            .column(column)
            .gte()
            .literal(value.toString())
            .endGroup()
}

infix fun EntityFieldInfo.lte(value: EntityFieldInfo): ExpressionBuilder {
    val column1 = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)
    val column2 = EntityBaseDSL.getColumnModelFromEntityFieldInfo(value)

    return ExpressionBuilder()
            .beginGroup()
            .column(column1)
            .lte()
            .column(column2)
            .endGroup()
}

infix fun EntityFieldInfo.lte(value: Any): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)

    return ExpressionBuilder()
            .beginGroup()
            .column(column)
            .lte()
            .literal(value.toString())
            .endGroup()
}

infix fun ExpressionBuilder.and(next: ExpressionBuilder): ExpressionBuilder {
    return this.and().expr(next)
}

infix fun ExpressionBuilder.or(next: ExpressionBuilder): ExpressionBuilder {
    return this.or().expr(next)
}

infix fun EntityFieldInfo.desc(d: Boolean): OrderByColumnInfo {
    val o = OrderByColumnInfo()
    o.column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)
    o.isDesc = d

    return o
}

operator fun EntityFieldInfo.plus(value: EntityFieldInfo): ExpressionBuilder {
    val column1 = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)
    val column2 = EntityBaseDSL.getColumnModelFromEntityFieldInfo(value)

    return ExpressionBuilder()
            .beginGroup()
            .column(column1)
            .operator("+")
            .column(column2)
            .endGroup()
}

operator fun EntityFieldInfo.plus(value: Any): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)

    return ExpressionBuilder()
            .beginGroup()
            .column(column)
            .operator("+")
            .literal(value.toString())
            .endGroup()
}

operator fun EntityFieldInfo.minus(value: EntityFieldInfo): ExpressionBuilder {
    val column1 = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)
    val column2 = EntityBaseDSL.getColumnModelFromEntityFieldInfo(value)

    return ExpressionBuilder()
            .beginGroup()
            .column(column1)
            .operator("-")
            .column(column2)
            .endGroup()
}

operator fun EntityFieldInfo.minus(value: Any): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)

    return ExpressionBuilder()
            .beginGroup()
            .column(column)
            .operator("-")
            .literal(value.toString())
            .endGroup()
}

operator fun EntityFieldInfo.times(value: EntityFieldInfo): ExpressionBuilder {
    val column1 = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)
    val column2 = EntityBaseDSL.getColumnModelFromEntityFieldInfo(value)

    return ExpressionBuilder()
            .beginGroup()
            .column(column1)
            .operator("*")
            .column(column2)
            .endGroup()
}

operator fun EntityFieldInfo.times(value: Any): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)

    return ExpressionBuilder()
            .beginGroup()
            .column(column)
            .operator("*")
            .literal(value.toString())
            .endGroup()
}

operator fun EntityFieldInfo.div(value: EntityFieldInfo): ExpressionBuilder {
    val column1 = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)
    val column2 = EntityBaseDSL.getColumnModelFromEntityFieldInfo(value)

    return ExpressionBuilder()
            .beginGroup()
            .column(column1)
            .operator("/")
            .column(column2)
            .endGroup()
}

operator fun EntityFieldInfo.div(value: Any): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)

    return ExpressionBuilder()
            .beginGroup()
            .column(column)
            .operator("/")
            .literal(value.toString())
            .endGroup()
}

infix fun ExpressionBuilder.gt(next: ExpressionBuilder): ExpressionBuilder {
    return this.gt().expr(next)
}

infix fun ExpressionBuilder.gt(next: EntityFieldInfo): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(next)

    return this.gt().column(column)
}

infix fun ExpressionBuilder.gt(next: Any): ExpressionBuilder {
    return this.gt().literal(next.toString())
}

infix fun ExpressionBuilder.gte(next: ExpressionBuilder): ExpressionBuilder {
    return this.gte().expr(next)
}

infix fun ExpressionBuilder.gte(next: EntityFieldInfo): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(next)

    return this.gte().column(column)
}

infix fun ExpressionBuilder.gte(next: Any): ExpressionBuilder {
    return this.gte().literal(next.toString())
}

infix fun ExpressionBuilder.lt(next: ExpressionBuilder): ExpressionBuilder {
    return this.lt().expr(next)
}

infix fun ExpressionBuilder.lt(next: EntityFieldInfo): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(next)

    return this.lt().column(column)
}

infix fun ExpressionBuilder.lt(next: Any): ExpressionBuilder {
    return this.lt().literal(next.toString())
}

infix fun ExpressionBuilder.lte(next: ExpressionBuilder): ExpressionBuilder {
    return this.lte().expr(next)
}

infix fun ExpressionBuilder.lte(next: EntityFieldInfo): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(next)

    return this.lte().column(column)
}

infix fun ExpressionBuilder.lte(next: Any): ExpressionBuilder {
    return this.lte().literal(next.toString())
}

infix fun ExpressionBuilder.eq(next: ExpressionBuilder): ExpressionBuilder {
    return this.eq().expr(next)
}

infix fun ExpressionBuilder.eq(next: EntityFieldInfo): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(next)

    return this.eq().column(column)
}

infix fun ExpressionBuilder.eq(next: Any): ExpressionBuilder {
    return this.eq().literal(next.toString())
}