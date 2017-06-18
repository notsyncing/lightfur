package io.github.notsyncing.lightfur.entity.dsl

import io.github.notsyncing.lightfur.DataSession
import io.github.notsyncing.lightfur.entity.EntityDataMapper
import io.github.notsyncing.lightfur.entity.EntityFieldInfo
import io.github.notsyncing.lightfur.entity.EntityGlobal
import io.github.notsyncing.lightfur.entity.EntityModel
import io.github.notsyncing.lightfur.sql.base.SQLPart
import io.github.notsyncing.lightfur.sql.builders.SelectQueryBuilder
import io.github.notsyncing.lightfur.sql.models.ColumnModel
import io.github.notsyncing.lightfur.sql.models.TableModel
import io.vertx.ext.sql.ResultSet
import kotlinx.coroutines.experimental.future.await
import kotlinx.coroutines.experimental.future.future
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KMutableProperty

abstract class EntityBaseDSL<F: EntityModel>(private val finalModel: F?,
                                             private val isQuery: Boolean = false,
                                             private val isInsert: Boolean = false,
                                             private val cacheTag: String? = null) {
    abstract protected val builder: SQLPart
    private var cachedSQL: String? = null

    protected var cached: Boolean = false
        get() = cachedSQL != null

    var requireTableAlias = false

    companion object {
        @JvmStatic
        protected fun getTableModelFromEntityModel(m: EntityModel): TableModel {
            val table = TableModel()
            table.name = m.table
            table.database = m.database
            table.schema = m.schema
            table.alias = "${m::class.java.simpleName}_${m.hashCode()}"

            return table
        }

        @JvmStatic
        protected fun getTableModelFromSubQuery(s: EntitySelectDSL<*>): TableModel {
            val table = TableModel()
            table.subQuery = s.toSQLPart() as SelectQueryBuilder
            table.alias = "${s::class.java.simpleName}_${s.hashCode()}"

            return table
        }

        @JvmStatic
        fun getColumnModelFromEntityFieldInfo(info: EntityFieldInfo): ColumnModel {
            val c = ColumnModel()

            c.table = EntityGlobal.tableModels[info.entity::class.java]!!.clone()

            if (info.entity.skipTableName) {
                c.table.name = null
            }

            if (!info.entity.skipTableAlias) {
                c.table.alias = "${info.entity::class.java.simpleName}_${info.entity.hashCode()}"
            }

            c.modelType = info.entity::class.java.canonicalName
            c.column = info.inner.dbColumn
            c.fieldName = info.inner.name
            c.isAutoGenerated = info.inner.dbAutoGenerated
            c.isPrimaryKey = info.inner.dbPrimaryKey

            if (info.entity.modelAliasBeforeColumnName) {
                c.alias = "${info.entity::class.java.simpleName}_${info.entity.hashCode()}_${c.fieldName}"
            }

            return c
        }
    }

    init {
        if (cacheTag != null) {
            cachedSQL = EntityGlobal.sqlCache[cacheTag]
        }
    }

    fun execute(session: DataSession? = null) = future<Pair<List<F>, Int>> {
        val sql = toSQL()
        val params = toSQLParameters().toTypedArray()
        val db = session ?: DataSession(EntityDataMapper())

        try {
            val r: List<F>
            val c: Int

            if (isQuery) {
                r = db.queryList(finalModel!!::class.java, sql, *params).await()
                c = r.size
            } else if (isInsert) {
                val rs = db.executeWithReturning(sql, *params).await()

                if (rs.numRows == 1) {
                    for ((i, pkf) in finalModel!!.primaryKeyFields.withIndex()) {
                        val p = pkf as KMutableProperty<Any>
                        p.setter.call(finalModel, rs.rows[0].getValue(finalModel.primaryKeyFieldInfos[i].inner.dbColumn))
                    }
                }

                r = listOf(finalModel!!)
                c = rs.numRows
            } else {
                val u = db.execute(sql, *params).await()

                r = if (finalModel == null) emptyList() else listOf(finalModel)
                c = u.updated
            }

            return@future Pair(r, c)
        } finally {
            if (session == null) {
                db.end().await()
            }
        }
    }

    fun executeFirst(session: DataSession? = null): CompletableFuture<F?> {
        return execute(session)
                .thenApply { (l, c) ->
                    if (c > 0) {
                        l[0]
                    } else {
                        null
                    }
                }
    }

    fun queryRaw(session: DataSession? = null) = future {
        val sql = toSQL()
        val params = toSQLParameters().toTypedArray()
        val db = session ?: DataSession(EntityDataMapper())

        try {
            val r: ResultSet

            if (isQuery) {
                r = db.query(sql, *params).await()
            } else {
                r = db.executeWithReturning(sql, *params).await()
            }

            r
        } finally {
            if (session == null) {
                db.end().await()
            }
        }
    }

    fun executeRaw(session: DataSession? = null) = future {
        val sql = toSQL()
        val params = toSQLParameters().toTypedArray()
        val db = session ?: DataSession(EntityDataMapper())

        try {
            db.execute(sql, *params).await()
        } finally {
            if (session == null) {
                db.end().await()
            }
        }
    }

    open fun toSQLPart() = builder

    open fun toSQL(): String {
        // FIXME: Implement parameter-only generation
        if (cachedSQL != null) {
            // This call is for parameter generation
            builder.toString()
        }

        return cachedSQL ?: builder.toString()
    }

    open fun toSQLParameters() = builder.parameters
}