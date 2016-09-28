package io.github.notsyncing.lightfur.entity.dsl

import io.github.notsyncing.lightfur.entity.EntityFieldInfo
import io.github.notsyncing.lightfur.entity.EntityModel
import io.github.notsyncing.lightfur.sql.base.ExpressionBuilder
import io.github.notsyncing.lightfur.sql.builders.SelectQueryBuilder
import io.github.notsyncing.lightfur.sql.models.OrderByColumnInfo
import io.github.notsyncing.lightfur.sql.models.TableModel
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KProperty0

// TODO: Implement aggregate functions and case-when clause
// TODO: Implement accessing of sub-query fields (possible through EntitySelectDSL::finalModel)
// TODO: Implement complex condition expression e.g. r.F(r::flag) + 1 > r.F(r::id)

class EntitySelectDSL(private var finalModel: EntityModel? = null) : EntityBaseDSL() {
    private var finalTableModel: TableModel? = null
    private val builder = SelectQueryBuilder()

    init {
        if (finalModel != null) {
            finalTableModel = getTableModelFromEntityModel(finalModel!!)
        }
    }

    fun from(model: EntityModel? = null): EntitySelectDSL {
        var m = model

        if (finalModel == null) {
            finalModel = model
        }

        if (model == null) {
            m = finalModel
        }

        if (m == null) {
            throw RuntimeException("You must at least specify one model in either select() or from()!")
        }

        builder.from(getTableModelFromEntityModel(m))

        return this
    }

    fun from(subQuery: EntitySelectDSL): EntitySelectDSL {
        val m = getTableModelFromSubQuery(subQuery)

        if (finalTableModel == null) {
            finalTableModel = m
        }

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