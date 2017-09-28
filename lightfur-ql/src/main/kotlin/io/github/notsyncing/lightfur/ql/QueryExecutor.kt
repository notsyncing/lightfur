package io.github.notsyncing.lightfur.ql

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import io.github.notsyncing.lightfur.entity.dsl.EntitySelectDSL
import io.github.notsyncing.lightfur.ql.permission.QueryPermissions
import kotlinx.coroutines.experimental.future.await
import kotlinx.coroutines.experimental.future.future
import java.util.concurrent.CompletableFuture

class QueryExecutor {
    companion object {
        private var processorCreator: (() -> RawQueryProcessor)? = null

        fun setRawQueryProcessor(p: () -> RawQueryProcessor) {
            processorCreator = p
        }
    }

    private val processor: RawQueryProcessor

    init {
        if (processorCreator == null) {
            throw RuntimeException("You must specify a RawQueryProcessor!")
        }

        processor = processorCreator!!.invoke()
    }

    val parser = QueryParser()

    private val _queryFunction: (EntitySelectDSL<*>) -> CompletableFuture<Any?> = { processor.query(it) }

    fun execute(query: JSONObject, permissions: QueryPermissions = QueryPermissions.ALL) = future {
        val resolvedQueries = parser.parse(query, permissions)
        val result = JSONObject()

        try {
            for ((key, q) in resolvedQueries.entries) {
                val r = _queryFunction(q).await()
                val data = aggregateResultSet(key, query.getJSONObject(key), r)

                result.put(key, data)
            }
        } finally {
            processor.end().await()
        }

        result
    }

    fun execute(query: String, permissions: QueryPermissions = QueryPermissions.ALL) = execute(JSON.parseObject(query), permissions)

    private fun aggregateResultSet(rootKey: String, currQuery: JSONObject, r: Any?): JSONArray {
        val data = processor.resultSetToList(r)

        return recursiveAggregateResults(rootKey, currQuery, data)
    }

    private fun recursiveAggregateResults(key: String, currQuery: JSONObject, data: List<Map<String, Any?>>): JSONArray {
        val currModelClassName = currQuery.getString("_from")
        val currModel = parser.modelMap["${currModelClassName}_$key"]!!
        val modelAliasPrefix = "${currModel::class.java.simpleName}_${currModel.hashCode()}_"

        val currLayerQueryFields = currQuery
                .filterKeys { !it.startsWith("_") }
                .filterValues { if (it is JSONObject) !it.containsKey("_from") else true }
                .keys

        val currLayerData = data.groupBy { row ->
            val currLayerObject = JSONObject(true)

            currLayerQueryFields.forEach {
                var currKey = it

                if (currQuery[it] is JSONObject) {
                    val currQueryInfo = currQuery[it] as JSONObject

                    if (currQueryInfo.containsKey("_aliasOf")) {
                        currKey = currQueryInfo.getString("_aliasOf")
                    }
                }

                val value = processor.processValue(row[modelAliasPrefix + currKey])
                currLayerObject.put(it, value)
            }

            currLayerObject
        }

        val innerQuery = currQuery.filterValues { it is JSONObject }
                .mapValues { it.value as JSONObject }
                .filterValues { it.containsKey("_from") }
                .map { (k, v) -> Pair(k, v) }
                .firstOrNull()

        if (innerQuery != null) {
            val (innerKey, q) = innerQuery

            currLayerData.forEach { parentObj, innerData ->
                parentObj.put(innerKey, recursiveAggregateResults("$key.$innerKey", q, innerData))
            }
        }

        return JSON.toJSON(currLayerData.keys) as JSONArray
    }
}