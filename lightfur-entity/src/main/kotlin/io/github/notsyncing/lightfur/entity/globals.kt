package io.github.notsyncing.lightfur.entity

import io.github.notsyncing.lightfur.DataSession
import io.github.notsyncing.lightfur.entity.exceptions.NothingUpdatedException
import kotlinx.coroutines.experimental.*
import java.lang.reflect.ParameterizedType
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.CoroutineContext

interface DatabaseOperationCoroutineScope : CoroutineScope {
    val dataSession: DataSession<*, *, *>

    override val coroutineContext: CoroutineContext get() = context
    override val isActive: Boolean get() = context[Job]!!.isActive

    fun beginTransaction(): CompletableFuture<Unit> {
        return dataSession.beginTransaction()
                .thenApply {}
    }

    fun commit(): CompletableFuture<Unit> {
        return dataSession.commit()
                .thenApply {}
    }

    fun rollback(): CompletableFuture<Unit> {
        return dataSession.rollback()
                .thenApply {}
    }

    fun end(): CompletableFuture<Unit> {
        return dataSession.end()
                .thenApply {}
    }
}

class DatabaseOperationCoroutine<T>(override val context: CoroutineContext) :
        CompletableFuture<T>(), Continuation<T>, DatabaseOperationCoroutineScope {
    override val dataSession = DataSession.start<DataSession<Any, Any, Any>>()

    override fun resume(value: T) { 
        complete(value) 
    }
    
    override fun resumeWithException(exception: Throwable) { 
        completeExceptionally(exception) 
    }
}

fun <T> database(context: CoroutineContext = DefaultDispatcher, 
                 start: CoroutineStart = CoroutineStart.DEFAULT, 
                 parent: Job? = null, 
                 block: suspend DatabaseOperationCoroutineScope.() -> T): CompletableFuture<T> {
    require(!start.isLazy) { "$start start is not supported" }
    
    val newContext = newCoroutineContext(context, parent)
    val job = Job(newContext[Job])
    val future = DatabaseOperationCoroutine<T>(newContext + job)
    
    job.cancelFutureOnCompletion(future)
    future.whenComplete { _, exception -> job.cancel(exception) }
    
    start(block, receiver = future, completion = future)

    return future
            .thenCompose { v ->
                future.end()
                        .thenApply { v }
            }
            .exceptionally {
                future.end()
                throw it
            }
}

fun <T> CompletableFuture<Pair<List<T>, Int>>.throwsOnNothingUpdated(): CompletableFuture<Unit> {
    return this.thenApply { (_, c) ->
        if (c <= 0) {
            throw NothingUpdatedException()
        }
    }
}

fun <T> CompletableFuture<Pair<List<T>, Int>>.doesntCare(): CompletableFuture<Unit> {
    return this.thenApply {}
}

fun <T> CompletableFuture<Pair<List<T>, Int>>.toList(): CompletableFuture<List<T>> {
    return this.thenApply { (l, _) -> l }
}

fun <T, I> CompletableFuture<Pair<List<T>, Int>>.toList(clazz: Class<I>): CompletableFuture<List<I>> {
    return this.thenApply { (l, _) -> l.map { it as I } }
}

inline fun <reified T: PersistBy<*>> createModel(): T {
    val persistBy = T::class.java.genericInterfaces
            .filter { it is ParameterizedType }
            .map { it as ParameterizedType }
            .firstOrNull { it.rawType == PersistBy::class.java }

    if (persistBy == null) {
        throw UnsupportedOperationException("Model type ${T::class.java} has no ${PersistBy::class.java} interface!")
    }

    val modelClass = persistBy.actualTypeArguments[0] as Class<*>
    return modelClass.newInstance() as T
}