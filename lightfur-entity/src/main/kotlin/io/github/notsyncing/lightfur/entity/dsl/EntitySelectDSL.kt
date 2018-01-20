package io.github.notsyncing.lightfur.entity.dsl

import io.github.notsyncing.lightfur.entity.EntityField
import io.github.notsyncing.lightfur.entity.EntityModel
import io.github.notsyncing.lightfur.sql.base.ExpressionBuilder
import io.github.notsyncing.lightfur.sql.base.SQLPart
import io.github.notsyncing.lightfur.sql.builders.SelectQueryBuilder
import io.github.notsyncing.lightfur.sql.models.OrderByColumnInfo
import kotlin.reflect.KProperty0

class EntitySelectDSL<F : EntityModel>(val resultModel: F) : EntityQueryDSL<F>(resultModel) {
    override val builder = SelectQueryBuilder()

    init {
        resultModel.fieldMap.map { it.value }
                .sortedBy { it.name }
                .forEach { builder.select(getColumnModelFromEntityField(it)) }
    }

    fun customColumns(): EntitySelectDSL<F> {
        builder.selectColumns.clear()

        return this
    }

    fun columns(columnList: List<EntityField<*>>): EntitySelectDSL<F> {
        builder.selectColumns.clear()
        columnList.forEach { builder.select(getColumnModelFromEntityField(it)) }

        return this
    }

    fun column(col: EntityField<*>): EntitySelectDSL<F> {
        builder.select(getColumnModelFromEntityField(col))

        return this
    }

    fun from(tableModel: EntityModel? = null): EntitySelectDSL<F> {
        var m = tableModel

        if (m == null) {
            m = resultModel
        } else {
            builder.selectColumns.clear()
        }

        builder.from(getTableModelFromEntityModel(m))

        return this
    }

    fun from(subQuery: EntitySelectDSL<*>): EntitySelectDSL<F> {
        val m = getTableModelFromSubQuery(subQuery)

        builder.selectColumns.clear()
        builder.from(m)

        return this
    }

    fun leftJoin(model: EntityModel, conditions: () -> ExpressionBuilder): EntitySelectDSL<F> {
        builder.leftJoin(getTableModelFromEntityModel(model), conditions())

        return this
    }

    fun leftJoin(subQuery: EntitySelectDSL<*>, conditions: () -> ExpressionBuilder): EntitySelectDSL<F> {
        builder.leftJoin(getTableModelFromSubQuery(subQuery), conditions())

        return this
    }

    fun rightJoin(model: EntityModel, conditions: () -> ExpressionBuilder): EntitySelectDSL<F> {
        builder.rightJoin(getTableModelFromEntityModel(model), conditions())

        return this
    }

    fun rightJoin(subQuery: EntitySelectDSL<*>, conditions: () -> ExpressionBuilder): EntitySelectDSL<F> {
        builder.rightJoin(getTableModelFromSubQuery(subQuery), conditions())

        return this
    }

    fun innerJoin(model: EntityModel, conditions: () -> ExpressionBuilder): EntitySelectDSL<F> {
        builder.innerJoin(getTableModelFromEntityModel(model), conditions())

        return this
    }

    fun innerJoin(subQuery: EntitySelectDSL<*>, conditions: () -> ExpressionBuilder): EntitySelectDSL<F> {
        builder.innerJoin(getTableModelFromSubQuery(subQuery), conditions())

        return this
    }

    fun where(conditions: () -> ExpressionBuilder): EntitySelectDSL<F> {
        builder.where(conditions())

        return this
    }

    fun groupBy(vararg columns: EntityField<*>): EntitySelectDSL<F> {
        builder.groupBy(*columns.map { getColumnModelFromEntityField(it) }.toTypedArray())

        return this
    }

    fun orderBy(vararg columns: OrderByColumnInfo): EntitySelectDSL<F> {
        builder.orderBy(*columns)

        return this
    }

    fun orderBy(column: EntityField<*>, isDesc: Boolean = false): EntitySelectDSL<F> {
        builder.orderBy(OrderByColumnInfo(getColumnModelFromEntityField(column), isDesc))

        return this
    }

    fun having(conditions: () -> ExpressionBuilder): EntitySelectDSL<F> {
        builder.having(conditions())

        return this
    }

    fun skip(count: Int): EntitySelectDSL<F> {
        builder.offset(count)

        return this
    }

    fun take(count: Int): EntitySelectDSL<F> {
        builder.limit(count)

        return this
    }

    fun map(sourceColumn: EntityField<*>, asColumn: EntityField<*>): EntitySelectDSL<F> {
        val sc = getColumnModelFromEntityField(sourceColumn)
        val asc = getColumnModelFromEntityField(asColumn)
        builder.selectAs(sc, asc)

        return this
    }

    fun map(sourceColumn: EntityField<*>, asColumn: KProperty0<*>): EntitySelectDSL<F> {
        return map(sourceColumn, resultModel.fieldMap[asColumn.name]!!)
    }

    fun map(sourceExpr: SQLPart, asColumn: EntityField<*>): EntitySelectDSL<F> {
        val asc = getColumnModelFromEntityField(asColumn)
        builder.selectAs(sourceExpr, asc)

        return this
    }

    fun map(sourceExpr: SQLPart, asColumn: KProperty0<*>): EntitySelectDSL<F> {
        return map(sourceExpr, resultModel.fieldMap[asColumn.name]!!)
    }
}