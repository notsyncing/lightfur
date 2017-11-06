package io.github.notsyncing.lightfur.integration.vertx.entity

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import io.github.notsyncing.lightfur.DataSession
import io.github.notsyncing.lightfur.entity.EntityQueryExecutor
import io.github.notsyncing.lightfur.entity.dsl.EntityBaseDSL
import io.github.notsyncing.lightfur.sql.builders.UpdateQueryBuilder
import io.vertx.ext.sql.ResultSet
import io.vertx.ext.sql.SQLConnection
import io.vertx.ext.sql.UpdateResult
import kotlinx.coroutines.experimental.future.await
import kotlinx.coroutines.experimental.future.future
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KMutableProperty

class VertxEntityQueryExecutor : EntityQueryExecutor<SQLConnection, ResultSet, UpdateResult> {
    override fun execute(dsl: EntityBaseDSL<*>, session: DataSession<SQLConnection, ResultSet, UpdateResult>?): CompletableFuture<Pair<List<Any?>, Int>> {
        val ex = Exception()

        return future<Pair<List<Any?>, Int>> {
            val sql = dsl.toSQL()

            if (sql == UpdateQueryBuilder.NOTHING_TO_UPDATE) {
                return@future Pair(emptyList(), 0)
            }

            val params = dsl.toSQLParameters().toTypedArray()
            val db = session ?: DataSession.start()

            try {
                val r: List<Any?>
                val c: Int

                if (dsl.isQuery) {
                    r = db.queryList(dsl.finalModel!!::class.java, sql, *params).await()
                    c = r.size
                } else if (dsl.isInsert) {
                    val rs = db.executeWithReturning(sql, *params).await()

                    if (rs.numRows == 1) {
                        for ((i, pkf) in dsl.finalModel!!.primaryKeyFields.withIndex()) {
                            val p = pkf as KMutableProperty<Any>
                            p.setter.call(dsl.finalModel,
                                    rs.rows[0].getValue(dsl.finalModel!!.primaryKeyFieldInfos[i].inner.dbColumn))
                        }
                    }

                    r = listOf(dsl.finalModel!!)
                    c = rs.numRows
                } else {
                    val u = db.update(sql, *params).await()

                    r = if (dsl.finalModel == null) emptyList() else listOf(dsl.finalModel)
                    c = u.updated.toInt()
                }

                return@future Pair(r, c)
            } catch (e: Exception) {
                ex.initCause(e)
                throw ex
            } finally {
                if (session == null) {
                    db.end().await()
                }
            }
        }
    }

    override fun queryRaw(dsl: EntityBaseDSL<*>, session: DataSession<SQLConnection, ResultSet, UpdateResult>?) = future {
        val sql = dsl.toSQL()
        val params = dsl.toSQLParameters().toTypedArray()
        val db = session ?: DataSession.start()

        try {
            val r: ResultSet

            if (dsl.isQuery) {
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

    override fun queryJson(dsl: EntityBaseDSL<*>, session: DataSession<SQLConnection, ResultSet, UpdateResult>?): CompletableFuture<List<JSONObject>> {
        return queryRaw(dsl, session)
                .thenApply { it.rows.map { JSON.parseObject(it.encode()) } }
    }
}