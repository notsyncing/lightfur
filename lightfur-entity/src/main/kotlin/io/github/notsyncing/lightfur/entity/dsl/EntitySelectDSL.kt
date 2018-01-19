package io.github.notsyncing.lightfur.entity.dsl

import io.github.notsyncing.lightfur.entity.EntityFieldInfo
import io.github.notsyncing.lightfur.entity.EntityModel
import io.github.notsyncing.lightfur.sql.base.ExpressionBuilder
import io.github.notsyncing.lightfur.sql.base.SQLPart
import io.github.notsyncing.lightfur.sql.builders.SelectQueryBuilder
import io.github.notsyncing.lightfur.sql.models.OrderByColumnInfo
import kotlin.reflect.KProperty0

class EntitySelectDSL<F : EntityModel>(val resultModel: F) : EntityRawQueryDSL<F>(resultModel, "", emptyArray()) {
    override val builder = SelectQueryBuilder()

    init {
        resultModel.fieldMap.map { it.value.info }
                .sortedBy { it.inner.name }
                .forEach { builder.select(getColumnModelFromEntityFieldInfo(it)) }
    }

    fun customColumns(): EntitySelectDSL<F> {
        builder.selectColumns.clear()

        return this
    }

    fun columns(columnList: List<EntityFieldInfo>): EntitySelectDSL<F> {
        builder.selectColumns.clear()
        columnList.forEach { builder.select(getColumnModelFromEntityFieldInfo(it)) }

        return this
    }

    fun column(col: EntityFieldInfo): EntitySelectDSL<F> {
        builder.select(getColumnModelFromEntityFieldInfo(col))

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

    fun groupBy(vararg columns: EntityFieldInfo): EntitySelectDSL<F> {
        builder.groupBy(*columns.map { getColumnModelFromEntityFieldInfo(it) }.toTypedArray())

        return this
    }

    fun orderBy(vararg columns: OrderByColumnInfo): EntitySelectDSL<F> {
        builder.orderBy(*columns)

        return this
    }

    fun orderBy(column: EntityFieldInfo, isDesc: Boolean = false): EntitySelectDSL<F> {
        builder.orderBy(OrderByColumnInfo(getColumnModelFromEntityFieldInfo(column), isDesc))

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

    fun map(sourceColumn: EntityFieldInfo, asColumn: EntityFieldInfo): EntitySelectDSL<F> {
        val sc = getColumnModelFromEntityFieldInfo(sourceColumn)
        val asc = getColumnModelFromEntityFieldInfo(asColumn)
        builder.selectAs(sc, asc)

        return this
    }

    fun map(sourceColumn: EntityFieldInfo, asColumn: KProperty0<*>): EntitySelectDSL<F> {
        return map(sourceColumn, resultModel.fieldMap[asColumn.name]!!.info)
    }

    fun map(sourceExpr: SQLPart, asColumn: EntityFieldInfo): EntitySelectDSL<F> {
        val asc = getColumnModelFromEntityFieldInfo(asColumn)
        builder.selectAs(sourceExpr, asc)

        return this
    }

    fun map(sourceExpr: SQLPart, asColumn: KProperty0<*>): EntitySelectDSL<F> {
        return map(sourceExpr, resultModel.fieldMap[asColumn.name]!!.info)
    }
}