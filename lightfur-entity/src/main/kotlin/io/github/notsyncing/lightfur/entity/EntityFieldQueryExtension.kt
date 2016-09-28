package io.github.notsyncing.lightfur.entity

import io.github.notsyncing.lightfur.entity.dsl.EntityBaseDSL
import io.github.notsyncing.lightfur.sql.base.ExpressionBuilder
import io.github.notsyncing.lightfur.sql.models.ColumnModel
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