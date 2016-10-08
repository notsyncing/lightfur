package io.github.notsyncing.lightfur.entity.dsl

import io.github.notsyncing.lightfur.entity.EntityFieldInfo
import io.github.notsyncing.lightfur.entity.EntityModel
import io.github.notsyncing.lightfur.sql.base.ExpressionBuilder
import io.github.notsyncing.lightfur.sql.builders.SelectQueryBuilder
import io.github.notsyncing.lightfur.sql.models.OrderByColumnInfo

// TODO: Implement SQL functions and case-when clause

class EntitySelectDSL(var resultModel: EntityModel) : EntityBaseDSL() {
    private val builder = SelectQueryBuilder()

    fun from(tableModel: EntityModel? = null): EntitySelectDSL {
        var m = tableModel

        if (m == null) {
            m = resultModel
        }

        builder.from(getTableModelFromEntityModel(m))

        return this
    }

    fun from(subQuery: EntitySelectDSL): EntitySelectDSL {
        val m = getTableModelFromSubQuery(subQuery)

        builder.from(m)

        return this
    }

    fun leftJoin(model: EntityModel, conditions: () -> ExpressionBuilder): EntitySelectDSL {
        builder.leftJoin(getTableModelFromEntityModel(model), conditions())

        return this
    }

    fun leftJoin(subQuery: EntitySelectDSL, conditions: () -> ExpressionBuilder): EntitySelectDSL {
        builder.leftJoin(getTableModelFromSubQuery(subQuery), conditions())

        return this
    }

    fun rightJoin(model: EntityModel, conditions: () -> ExpressionBuilder): EntitySelectDSL {
        builder.rightJoin(getTableModelFromEntityModel(model), conditions())

        return this
    }

    fun rightJoin(subQuery: EntitySelectDSL, conditions: () -> ExpressionBuilder): EntitySelectDSL {
        builder.rightJoin(getTableModelFromSubQuery(subQuery), conditions())

        return this
    }

    fun innerJoin(model: EntityModel, conditions: () -> ExpressionBuilder): EntitySelectDSL {
        builder.innerJoin(getTableModelFromEntityModel(model), conditions())

        return this
    }

    fun innerJoin(subQuery: EntitySelectDSL, conditions: () -> ExpressionBuilder): EntitySelectDSL {
        builder.innerJoin(getTableModelFromSubQuery(subQuery), conditions())

        return this
    }

    fun where(conditions: () -> ExpressionBuilder): EntitySelectDSL {
        builder.where(conditions())

        return this
    }

    fun groupBy(vararg columns: EntityFieldInfo): EntitySelectDSL {
        builder.groupBy(*columns.map { getColumnModelFromEntityFieldInfo(it) }.toTypedArray())

        return this
    }

    fun orderBy(vararg columns: OrderByColumnInfo): EntitySelectDSL {
        builder.orderBy(*columns)

        return this
    }

    fun having(conditions: () -> ExpressionBuilder): EntitySelectDSL {
        builder.having(conditions())

        return this
    }

    fun skip(count: Int): EntitySelectDSL {
        builder.offset(count)

        return this
    }

    fun take(count: Int): EntitySelectDSL {
        builder.limit(count)

        return this
    }

    override fun toSQLPart() = builder
}