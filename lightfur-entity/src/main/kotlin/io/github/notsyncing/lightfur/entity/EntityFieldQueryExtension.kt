package io.github.notsyncing.lightfur.entity

import io.github.notsyncing.lightfur.entity.dsl.EntityBaseDSL
import io.github.notsyncing.lightfur.sql.base.ExpressionBuilder
import io.github.notsyncing.lightfur.sql.base.SQLPart
import io.github.notsyncing.lightfur.sql.builders.CaseWhenBuilder
import io.github.notsyncing.lightfur.sql.models.OrderByColumnInfo

infix fun EntityFieldInfo.eq(value: EntityFieldInfo): ExpressionBuilder {
    val column1 = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)
    val column2 = EntityBaseDSL.getColumnModelFromEntityFieldInfo(value)

    return ExpressionBuilder()
            .beginGroup()
            .column(column1, true)
            .eq()
            .column(column2, true)
            .endGroup()
}

infix fun EntityFieldInfo.eq(value: SQLPart): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)

    return ExpressionBuilder()
            .beginGroup()
            .column(column, true)
            .eq()
            .expr(value)
            .endGroup()
}

infix fun EntityFieldInfo.eq(value: Any?): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)

    val b = ExpressionBuilder()
            .beginGroup()
            .column(column, true)

    if (value == null) {
        b.eqNull()
    } else {
        b.eq().parameter(value)
    }

    return b.endGroup()
}

infix fun EntityFieldInfo.neq(value: EntityFieldInfo): ExpressionBuilder {
    val column1 = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)
    val column2 = EntityBaseDSL.getColumnModelFromEntityFieldInfo(value)

    return ExpressionBuilder()
            .beginGroup()
            .column(column1, true)
            .ne()
            .column(column2, true)
            .endGroup()
}

infix fun EntityFieldInfo.neq(value: SQLPart): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)

    return ExpressionBuilder()
            .beginGroup()
            .column(column, true)
            .ne()
            .expr(value)
            .endGroup()
}

infix fun EntityFieldInfo.neq(value: Any?): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)

    val b = ExpressionBuilder()
            .beginGroup()
            .column(column, true)

    if (value == null) {
        b.neNull()
    } else {
        b.ne().parameter(value)
    }

    return b.endGroup()
}

infix fun EntityFieldInfo.gt(value: EntityFieldInfo): ExpressionBuilder {
    val column1 = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)
    val column2 = EntityBaseDSL.getColumnModelFromEntityFieldInfo(value)

    return ExpressionBuilder()
            .beginGroup()
            .column(column1, true)
            .gt()
            .column(column2, true)
            .endGroup()
}

infix fun EntityFieldInfo.gt(value: SQLPart): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)

    return ExpressionBuilder()
            .beginGroup()
            .column(column, true)
            .gt()
            .expr(value)
            .endGroup()
}

infix fun EntityFieldInfo.gt(value: Any): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)

    return ExpressionBuilder()
            .beginGroup()
            .column(column, true)
            .gt()
            .parameter(value)
            .endGroup()
}

infix fun EntityFieldInfo.lt(value: EntityFieldInfo): ExpressionBuilder {
    val column1 = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)
    val column2 = EntityBaseDSL.getColumnModelFromEntityFieldInfo(value)

    return ExpressionBuilder()
            .beginGroup()
            .column(column1, true)
            .lt()
            .column(column2, true)
            .endGroup()
}

infix fun EntityFieldInfo.lt(value: SQLPart): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)

    return ExpressionBuilder()
            .beginGroup()
            .column(column, true)
            .lt()
            .expr(value)
            .endGroup()
}


infix fun EntityFieldInfo.lt(value: Any): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)

    return ExpressionBuilder()
            .beginGroup()
            .column(column, true)
            .lt()
            .parameter(value)
            .endGroup()
}

infix fun EntityFieldInfo.gte(value: EntityFieldInfo): ExpressionBuilder {
    val column1 = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)
    val column2 = EntityBaseDSL.getColumnModelFromEntityFieldInfo(value)

    return ExpressionBuilder()
            .beginGroup()
            .column(column1, true)
            .gte()
            .column(column2)
            .endGroup()
}

infix fun EntityFieldInfo.gte(value: SQLPart): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)

    return ExpressionBuilder()
            .beginGroup()
            .column(column, true)
            .gte()
            .expr(value)
            .endGroup()
}

infix fun EntityFieldInfo.gte(value: Any): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)

    return ExpressionBuilder()
            .beginGroup()
            .column(column, true)
            .gte()
            .parameter(value)
            .endGroup()
}

infix fun EntityFieldInfo.lte(value: EntityFieldInfo): ExpressionBuilder {
    val column1 = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)
    val column2 = EntityBaseDSL.getColumnModelFromEntityFieldInfo(value)

    return ExpressionBuilder()
            .beginGroup()
            .column(column1, true)
            .lte()
            .column(column2, true)
            .endGroup()
}

infix fun EntityFieldInfo.lte(value: SQLPart): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)

    return ExpressionBuilder()
            .beginGroup()
            .column(column, true)
            .lte()
            .expr(value)
            .endGroup()
}

infix fun EntityFieldInfo.lte(value: Any): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)

    return ExpressionBuilder()
            .beginGroup()
            .column(column, true)
            .lte()
            .parameter(value)
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
            .column(column1, true)
            .operator("+")
            .column(column2, true)
            .endGroup()
}

operator fun EntityFieldInfo.plus(value: SQLPart): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)

    return ExpressionBuilder()
            .beginGroup()
            .column(column, true)
            .operator("+")
            .expr(value)
            .endGroup()
}

operator fun EntityFieldInfo.plus(value: Any): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)

    return ExpressionBuilder()
            .beginGroup()
            .column(column, true)
            .operator("+")
            .parameter(value)
            .endGroup()
}

operator fun ExpressionBuilder.plus(value: EntityFieldInfo): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(value)

    return ExpressionBuilder()
            .beginGroup()
            .expr(this)
            .operator("+")
            .column(column, true)
            .endGroup()
}

operator fun ExpressionBuilder.plus(value: SQLPart): ExpressionBuilder {
    return ExpressionBuilder()
            .beginGroup()
            .expr(this)
            .operator("+")
            .expr(value)
            .endGroup()
}

operator fun ExpressionBuilder.plus(value: Any): ExpressionBuilder {
    return ExpressionBuilder()
            .beginGroup()
            .expr(this)
            .operator("+")
            .parameter(value)
            .endGroup()
}

operator fun EntityFieldInfo.minus(value: EntityFieldInfo): ExpressionBuilder {
    val column1 = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)
    val column2 = EntityBaseDSL.getColumnModelFromEntityFieldInfo(value)

    return ExpressionBuilder()
            .beginGroup()
            .column(column1, true)
            .operator("-")
            .column(column2, true)
            .endGroup()
}

operator fun EntityFieldInfo.minus(value: SQLPart): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)

    return ExpressionBuilder()
            .beginGroup()
            .column(column, true)
            .operator("-")
            .expr(value)
            .endGroup()
}

operator fun ExpressionBuilder.minus(value: Any): ExpressionBuilder {
    return ExpressionBuilder()
            .beginGroup()
            .expr(this)
            .operator("-")
            .parameter(value)
            .endGroup()
}

operator fun ExpressionBuilder.minus(value: EntityFieldInfo): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(value)

    return ExpressionBuilder()
            .beginGroup()
            .expr(this)
            .operator("-")
            .column(column, true)
            .endGroup()
}

operator fun ExpressionBuilder.minus(value: SQLPart): ExpressionBuilder {
    return ExpressionBuilder()
            .beginGroup()
            .expr(this)
            .operator("-")
            .expr(value)
            .endGroup()
}

operator fun EntityFieldInfo.minus(value: Any): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)

    return ExpressionBuilder()
            .beginGroup()
            .column(column, true)
            .operator("-")
            .parameter(value)
            .endGroup()
}

operator fun EntityFieldInfo.times(value: EntityFieldInfo): ExpressionBuilder {
    val column1 = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)
    val column2 = EntityBaseDSL.getColumnModelFromEntityFieldInfo(value)

    return ExpressionBuilder()
            .beginGroup()
            .column(column1, true)
            .operator("*")
            .column(column2, true)
            .endGroup()
}

operator fun EntityFieldInfo.times(value: SQLPart): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)

    return ExpressionBuilder()
            .beginGroup()
            .column(column, true)
            .operator("*")
            .expr(value)
            .endGroup()
}

operator fun EntityFieldInfo.times(value: Any): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)

    return ExpressionBuilder()
            .beginGroup()
            .column(column, true)
            .operator("*")
            .parameter(value)
            .endGroup()
}

operator fun ExpressionBuilder.times(value: EntityFieldInfo): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(value)

    return ExpressionBuilder()
            .beginGroup()
            .expr(this)
            .operator("*")
            .column(column, true)
            .endGroup()
}

operator fun ExpressionBuilder.times(value: SQLPart): ExpressionBuilder {
    return ExpressionBuilder()
            .beginGroup()
            .expr(this)
            .operator("*")
            .expr(value)
            .endGroup()
}

operator fun ExpressionBuilder.times(value: Any): ExpressionBuilder {
    return ExpressionBuilder()
            .beginGroup()
            .expr(this)
            .operator("*")
            .parameter(value)
            .endGroup()
}

operator fun EntityFieldInfo.div(value: EntityFieldInfo): ExpressionBuilder {
    val column1 = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)
    val column2 = EntityBaseDSL.getColumnModelFromEntityFieldInfo(value)

    return ExpressionBuilder()
            .beginGroup()
            .column(column1, true)
            .operator("/")
            .column(column2, true)
            .endGroup()
}

operator fun EntityFieldInfo.div(value: SQLPart): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)

    return ExpressionBuilder()
            .beginGroup()
            .column(column, true)
            .operator("/")
            .expr(value)
            .endGroup()
}

operator fun EntityFieldInfo.div(value: Any): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(this)

    return ExpressionBuilder()
            .beginGroup()
            .column(column, true)
            .operator("/")
            .parameter(value)
            .endGroup()
}

operator fun ExpressionBuilder.div(value: EntityFieldInfo): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(value)

    return ExpressionBuilder()
            .beginGroup()
            .expr(this)
            .operator("/")
            .column(column, true)
            .endGroup()
}

operator fun ExpressionBuilder.div(value: SQLPart): ExpressionBuilder {
    return ExpressionBuilder()
            .beginGroup()
            .expr(this)
            .operator("/")
            .expr(value)
            .endGroup()
}

operator fun ExpressionBuilder.div(value: Any): ExpressionBuilder {
    return ExpressionBuilder()
            .beginGroup()
            .expr(this)
            .operator("/")
            .parameter(value)
            .endGroup()
}

infix fun ExpressionBuilder.gt(next: SQLPart): ExpressionBuilder {
    return this.gt().expr(next)
}

infix fun ExpressionBuilder.gt(next: EntityFieldInfo): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(next)

    return this.gt().column(column, true)
}

infix fun ExpressionBuilder.gt(next: Any): ExpressionBuilder {
    return this.gt().parameter(next)
}

infix fun ExpressionBuilder.gte(next: SQLPart): ExpressionBuilder {
    return this.gte().expr(next)
}

infix fun ExpressionBuilder.gte(next: EntityFieldInfo): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(next)

    return this.gte().column(column, true)
}

infix fun ExpressionBuilder.gte(next: Any): ExpressionBuilder {
    return this.gte().parameter(next)
}

infix fun ExpressionBuilder.lt(next: SQLPart): ExpressionBuilder {
    return this.lt().expr(next)
}

infix fun ExpressionBuilder.lt(next: EntityFieldInfo): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(next)

    return this.lt().column(column, true)
}

infix fun ExpressionBuilder.lt(next: Any): ExpressionBuilder {
    return this.lt().parameter(next)
}

infix fun ExpressionBuilder.lte(next: SQLPart): ExpressionBuilder {
    return this.lte().expr(next)
}

infix fun ExpressionBuilder.lte(next: EntityFieldInfo): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(next)

    return this.lte().column(column, true)
}

infix fun ExpressionBuilder.lte(next: Any): ExpressionBuilder {
    return this.lte().parameter(next)
}

infix fun ExpressionBuilder.eq(next: SQLPart): ExpressionBuilder {
    return this.eq().expr(next)
}

infix fun ExpressionBuilder.eq(next: EntityFieldInfo): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(next)

    return this.eq().column(column, true)
}

infix fun ExpressionBuilder.eq(next: Any): ExpressionBuilder {
    return this.eq().parameter(next)
}

fun ExpressionBuilder.field(f: EntityFieldInfo): ExpressionBuilder {
    val column = EntityBaseDSL.getColumnModelFromEntityFieldInfo(f)
    return this.column(column, true)
}

class case {
    private val builder = CaseWhenBuilder()

    fun on(expr: ExpressionBuilder): case {
        builder.`when`(expr)
        return this
    }

    fun then(p: SQLPart): case {
        builder.then(p)
        return this
    }

    fun then(l: Int): case {
        builder.then(ExpressionBuilder().parameter(l))
        return this
    }

    fun then(l: Long): case {
        builder.then(ExpressionBuilder().parameter(l))
        return this
    }

    fun then(l: Boolean): case {
        builder.then(ExpressionBuilder().parameter(l))
        return this
    }

    fun then(l: String): case {
        builder.then(ExpressionBuilder().parameter(l))
        return this
    }

    fun then(l: Double): case {
        builder.then(ExpressionBuilder().parameter(l))
        return this
    }

    fun otherwise(p: SQLPart): CaseWhenBuilder {
        builder.elseThen(p)
        return builder
    }

    fun otherwise(p: Int): CaseWhenBuilder {
        builder.elseThen(ExpressionBuilder().parameter(p))
        return builder
    }

    fun otherwise(p: Long): CaseWhenBuilder {
        builder.elseThen(ExpressionBuilder().parameter(p))
        return builder
    }

    fun otherwise(p: Boolean): CaseWhenBuilder {
        builder.elseThen(ExpressionBuilder().parameter(p))
        return builder
    }

    fun otherwise(p: String): CaseWhenBuilder {
        builder.elseThen(ExpressionBuilder().parameter(p))
        return builder
    }

    fun otherwise(p: Double): CaseWhenBuilder {
        builder.elseThen(ExpressionBuilder().parameter(p))
        return builder
    }
}
