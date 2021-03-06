package io.github.notsyncing.lightfur.entity.dsl

import com.alibaba.fastjson.JSONObject
import io.github.notsyncing.lightfur.DataSession
import io.github.notsyncing.lightfur.entity.EntityField
import io.github.notsyncing.lightfur.entity.EntityGlobal
import io.github.notsyncing.lightfur.entity.EntityModel
import io.github.notsyncing.lightfur.entity.EntityQueryExecutor
import io.github.notsyncing.lightfur.sql.base.SQLPart
import io.github.notsyncing.lightfur.sql.builders.SelectQueryBuilder
import io.github.notsyncing.lightfur.sql.builders.UpdateQueryBuilder
import io.github.notsyncing.lightfur.sql.models.ColumnModel
import io.github.notsyncing.lightfur.sql.models.TableModel
import io.github.notsyncing.lightfur.utils.FutureUtils
import kotlinx.coroutines.experimental.future.await
import kotlinx.coroutines.experimental.future.future
import java.util.concurrent.CompletableFuture

abstract class EntityBaseDSL<F: EntityModel>(val finalModel: F?,
                                             val isQuery: Boolean = false,
                                             val isInsert: Boolean = false,
                                             private val cacheTag: String? = null) {
    abstract protected val builder: SQLPart
    private var cachedSQL: String? = null

    protected var cached: Boolean = false
        get() = cachedSQL != null

    var requireTableAlias = false

    companion object {
        private var executor: EntityQueryExecutor<Any, Any, Any>? = null

        fun setQueryExecutor(e: EntityQueryExecutor<*, *, *>) {
            executor = e as EntityQueryExecutor<Any, Any, Any>
        }

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
        fun getColumnModelFromEntityField(info: EntityField<*>): ColumnModel {
            val c = ColumnModel()

            c.table = EntityGlobal.tableModels[info.entity::class.java]!!.clone()

            if (info.entity.skipTableName) {
                c.table.name = null
            }

            if (!info.entity.skipTableAlias) {
                c.table.alias = "${info.entity::class.java.simpleName}_${info.entity.hashCode()}"
            }

            c.modelType = info.entity::class.java.canonicalName
            c.column = info.dbColumn
            c.fieldName = info.name
            c.fieldType = info.dbType
            c.isAutoGenerated = info.dbAutoGenerated
            c.isPrimaryKey = info.dbPrimaryKey

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

    fun execute(session: DataSession<*, *, *>? = null): CompletableFuture<Pair<List<F>, Int>> {
        if (executor == null) {
            return FutureUtils.failed(RuntimeException("You must specify an EntityQueryExecutor!"))
        }

        return executor!!.execute(this, session as DataSession<Any, Any, Any>?) as CompletableFuture<Pair<List<F>, Int>>
    }

    fun executeFirst(session: DataSession<*, *, *>? = null): CompletableFuture<F?> {
        return execute(session)
                .thenApply { (l, c) ->
                    if (c > 0) {
                        l[0]
                    } else {
                        null
                    }
                }
    }

    fun queryRaw(session: DataSession<*, *, *>? = null): CompletableFuture<Any?> {
        if (executor == null) {
            return FutureUtils.failed(RuntimeException("You must specify an EntityQueryExecutor!"))
        }

        return executor!!.queryRaw(this, session as DataSession<Any, Any, Any>?) as CompletableFuture<Any?>
    }

    fun queryJson(session: DataSession<*, *, *>? = null): CompletableFuture<List<JSONObject>> {
        if (executor == null) {
            return FutureUtils.failed(RuntimeException("You must specify an EntityQueryExecutor!"))
        }

        return executor!!.queryJson(this, session as DataSession<Any, Any, Any>?)
    }

    fun executeRaw(session: DataSession<*, *, *>? = null) = future {
        val sql = toSQL()

        if (sql == UpdateQueryBuilder.NOTHING_TO_UPDATE) {
            return@future
        }

        val params = toSQLParameters().toTypedArray()
        val db = session ?: DataSession.start()

        try {
            db.update(sql, *params).await()
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