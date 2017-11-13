package io.github.notsyncing.lightfur.entity.dsl

import com.alibaba.fastjson.JSONObject
import io.github.notsyncing.lightfur.DataSession
import io.github.notsyncing.lightfur.models.ExecutionResult
import kotlinx.coroutines.experimental.future.await
import kotlinx.coroutines.experimental.future.future
import java.util.concurrent.CompletableFuture

open class SqlTemplate {
    val params = mutableListOf<Any?>()

    fun p(param: Any?, castToType: String? = null): String {
        params.add(param)

        if (castToType != null) {
            return "?::$castToType"
        } else {
            return "?"
        }
    }
}

class SqlTypedTemplate<T>(val type: Class<T>) : SqlTemplate() {

}

open class SqlStatement(val sql: String,
                        val params: List<Any?>) {
    operator fun component1() = sql

    operator fun component2() = params

    infix fun query(db: DataSession<*, *, *>): CompletableFuture<List<JSONObject>> {
        return db.queryJson(sql, *params.toTypedArray())
    }

    infix fun queryV(db: DataSession<*, *, *>): CompletableFuture<Any?> {
        return db.queryFirstValue(sql, *params.toTypedArray())
    }

    infix fun update(db: DataSession<*, *, *>): CompletableFuture<ExecutionResult> {
        return db.update(sql, *params.toTypedArray())
    }

    infix fun updateWithReturningFirst(db: DataSession<*, *, *>): CompletableFuture<Any?> {
        return db.executeWithReturningFirst(sql, *params.toTypedArray())
    }
}

open class SqlTypedStatement<T>(sql: String, params: List<Any?>,
                                val type: Class<T>) : SqlStatement(sql, params) {
    infix fun queryT(db: DataSession<*, *, *>): CompletableFuture<List<T>> {
        return db.queryList(type, sql, *params.toTypedArray())
    }

    infix fun query1(db: DataSession<*, *, *>): CompletableFuture<T> {
        return db.queryFirst(type, sql, *params.toTypedArray())
    }
}

fun sql(body: SqlTemplate.() -> String): SqlStatement {
    val template = SqlTemplate()
    val r = template.body().trimIndent()

    return SqlStatement(r, template.params)
}

fun <T> sql(type: Class<T>, body: SqlTypedTemplate<T>.() -> String): SqlTypedStatement<T> {
    val template = SqlTypedTemplate(type)
    val r = template.body().trimIndent()

    return SqlTypedStatement(r, template.params, type)
}

class SqlTransaction(val db: DataSession<*, *, *>) {
    fun begin(): CompletableFuture<Unit> {
        return db.beginTransaction()
                .thenApply {}
    }

    fun commit(): CompletableFuture<Unit> {
        return db.commit()
                .thenApply {}
    }

    fun rollback(): CompletableFuture<Unit> {
        return db.rollback()
                .thenApply {}
    }

    fun end(): CompletableFuture<Unit> {
        if (db.isInTransaction) {
            return db.rollback()
                    .thenCompose { db.end() }
                    .thenApply {}
        }

        return db.end()
                .thenApply {}
    }

    fun split(): CompletableFuture<Unit> {
        return commit()
                .thenCompose { begin() }
    }
}

fun <T> transaction(body: SqlTransaction.() -> CompletableFuture<T>) = future {
    val trans = SqlTransaction(DataSession.start())

    trans.begin().await()

    try {
        trans.body().await()
    } finally {
        trans.end().await()
    }
}