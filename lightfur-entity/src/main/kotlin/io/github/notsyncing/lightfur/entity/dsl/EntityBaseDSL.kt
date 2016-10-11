package io.github.notsyncing.lightfur.entity.dsl

import io.github.notsyncing.lightfur.DataSession
import io.github.notsyncing.lightfur.entity.*
import io.github.notsyncing.lightfur.sql.base.SQLPart
import io.github.notsyncing.lightfur.sql.builders.SelectQueryBuilder
import io.github.notsyncing.lightfur.sql.models.ColumnModel
import io.github.notsyncing.lightfur.sql.models.TableModel
import kotlinx.coroutines.async
import javax.swing.text.html.parser.Entity
import kotlin.reflect.KMutableProperty0

abstract class EntityBaseDSL<F: EntityModel>(private val finalModel: F,
                                             private val isQuery: Boolean = false,
                                             private val isInsert: Boolean = false,
                                             private val cacheTag: String? = null) {
    abstract protected val builder: SQLPart
    private var cachedSQL: String? = null

    protected var cached: Boolean = false
        get() = cachedSQL != null

    companion object {
        @JvmStatic
        protected fun getTableModelFromEntityModel(m: EntityModel): TableModel {
            val table = TableModel()
            table.name = m.table
            table.database = m.database
            table.schema = m.schema
            table.alias = "${m.javaClass.simpleName}_${m.hashCode()}"

            return table
        }

        @JvmStatic
        protected fun getTableModelFromSubQuery(s: EntitySelectDSL<*>): TableModel {
            val table = TableModel()
            table.subQuery = s.toSQLPart() as SelectQueryBuilder
            table.alias = "${s.javaClass.simpleName}_${s.hashCode()}"

            return table
        }

        @JvmStatic
        fun getColumnModelFromEntityFieldInfo(info: EntityFieldInfo): ColumnModel {
            val c = ColumnModel()
            c.table = EntityGlobal.tableModels[info.entity.javaClass]!!.clone()
            c.table.alias = "${info.entity.javaClass.simpleName}_${info.entity.hashCode()}"
            c.modelType = info.entity.javaClass.canonicalName
            c.column = info.inner.dbColumn
            c.fieldName = info.inner.name
            c.isAutoGenerated = info.inner.dbAutoGenerated
            c.isPrimaryKey = info.inner.dbPrimaryKey

            return c
        }
    }

    init {
        if (cacheTag != null) {
            cachedSQL = EntityGlobal.sqlCache[cacheTag]
        }
    }

    fun execute(session: DataSession? = null) = async<Pair<List<F>, Int>> {
        val sql = toSQL()
        val params = toSQLParameters().toTypedArray()
        val db = session ?: DataSession(EntityDataMapper())

        if (isQuery) {
            val r = await(db.queryList(finalModel.javaClass, sql, *params))
            return@async Pair(r, r.size)
        } else if (isInsert) {
            val r = await(db.executeWithReturning(sql, *params))

            if (r.numRows == 1) {
                for ((i, pkf) in finalModel.primaryKeyFields.withIndex()) {
                    val p = pkf as KMutableProperty0<Any>
                    p.set(r.rows[0].getValue(finalModel.primaryKeyFieldInfos[i].inner.dbColumn))
                }
            }

            return@async Pair(listOf(finalModel), r.numRows)
        } else {
            val u = await(db.execute(sql, *params))

            return@async Pair(listOf(finalModel), u.updated)
        }
    }

    open fun toSQLPart() = builder

    open fun toSQL() = cachedSQL ?: builder.toString()

    open fun toSQLParameters() = builder.parameters
}