package io.github.notsyncing.lightfur.integration.jdbc.entity

import com.alibaba.fastjson.JSONObject
import io.github.notsyncing.lightfur.DataSession
import io.github.notsyncing.lightfur.entity.EntityQueryExecutor
import io.github.notsyncing.lightfur.entity.dsl.EntityBaseDSL
import io.github.notsyncing.lightfur.models.ExecutionResult
import io.github.notsyncing.lightfur.sql.builders.UpdateQueryBuilder
import kotlinx.coroutines.experimental.future.await
import kotlinx.coroutines.experimental.future.future
import java.sql.Connection
import java.sql.ResultSet
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KMutableProperty

class JdbcEntityQueryExecutor : EntityQueryExecutor<Connection, ResultSet, ExecutionResult> {
    override fun execute(dsl: EntityBaseDSL<*>, session: DataSession<Connection, ResultSet, ExecutionResult>?): CompletableFuture<Pair<List<Any?>, Int>> {
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
                    if (dsl.finalModel!!.primaryKeyFieldInfos.isNotEmpty()) {
                        val rs = db.executeWithReturning(sql, *params).await()
                        var count = 0

                        while (rs.next()) {
                            for ((i, pkf) in dsl.finalModel!!.primaryKeyFields.withIndex()) {
                                val p = pkf as KMutableProperty<Any>
                                p.setter.call(dsl.finalModel,
                                        rs.getObject(dsl.finalModel!!.primaryKeyFieldInfos[i].inner.dbColumn))
                            }

                            count++
                        }

                        r = listOf(dsl.finalModel!!)
                        c = count
                    } else {
                        val u = db.update(sql, *params).await()

                        r = if (dsl.finalModel == null) emptyList() else listOf(dsl.finalModel)
                        c = u.updated.toInt()
                    }
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

    override fun queryRaw(dsl: EntityBaseDSL<*>, session: DataSession<Connection, ResultSet, ExecutionResult>?) = future {
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

    override fun queryJson(dsl: EntityBaseDSL<*>, session: DataSession<Connection, ResultSet, ExecutionResult>?) = future {
        val result = queryRaw(dsl, session).await()

        val list = mutableListOf<JSONObject>()

        while (result.next()) {
            val o = JSONObject()

            for (i in 1..result.metaData.columnCount) {
                o.put(result.metaData.getColumnLabel(i), result.getObject(i))
            }

            list.add(o)
        }

        list.toList()
    }
}