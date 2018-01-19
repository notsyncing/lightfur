package io.github.notsyncing.lightfur.ql

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import io.github.notsyncing.lightfur.entity.*
import io.github.notsyncing.lightfur.entity.dsl.EntitySelectDSL
import io.github.notsyncing.lightfur.ql.permission.EntityPermission
import io.github.notsyncing.lightfur.ql.permission.QueryPermissions
import io.github.notsyncing.lightfur.sql.base.ExpressionBuilder
import java.security.InvalidParameterException
import javax.naming.NoPermissionException

class QueryParser {
    val modelMap = mutableMapOf<String, EntityModel>()

    val neededModels get() = modelMap.values

    fun parse(query: JSONObject, permissions: QueryPermissions = QueryPermissions.ALL): Map<String, EntitySelectDSL<*>> {
        val map = mutableMapOf<String, EntitySelectDSL<*>>()

        for (key in query.keys) {
            val queryTarget = query.getJSONObject(key)
            val parsedQuery = recursiveParse(key, queryTarget, permissions, null, null)

            map[key] = parsedQuery
        }

        return map
    }

    fun parse(query: String, permissions: QueryPermissions = QueryPermissions.ALL) = parse(JSON.parseObject(query), permissions)

    private fun recursiveParse(path: String, query: JSONObject, permissions: QueryPermissions, parentQuery: JSONObject?,
                               currDsl: EntitySelectDSL<*>? = null): EntitySelectDSL<*> {
        val fromClassName = query.getString("_from")

        var allowEntity: EntityPermission<*>? = null

        if (permissions != QueryPermissions.ALL) {
            allowEntity = permissions.allowEntities.firstOrNull { it.entityModel.javaClass.name == fromClassName }

            if (allowEntity == null) {
                throw NoPermissionException("Current permissions does not contain entity model $fromClassName")
            }
        }

        val fromClass = Class.forName(fromClassName)
        val fromModel = modelMap["${fromClassName}_$path"] ?: fromClass.newInstance() as EntityModel
        val dsl = currDsl ?: fromModel.select().customColumns()

        fromModel.modelAliasBeforeColumnName = true

        if (!modelMap.containsKey("${fromClassName}_$path")) {
            modelMap["${fromClassName}_$path"] = fromModel
        }

        var conditionList: Pair<String, JSONArray> = Pair("", JSONArray())
        val permissionConditionList: MutableList<Pair<String, JSONArray>> = mutableListOf()
        var orderByList: JSONArray = JSONArray()
        var joinType: String? = null
        var joinOuter: String? = null
        var joinInner: String? = null
        val innerList = mutableListOf<Pair<String, JSONObject>>()

        for (key in query.keys) {
            if (key == "_from") {
                continue
            } else if (key == "_conditions") {
                conditionList = Pair(path, query.getJSONArray(key))
            } else if (key == "_skip") {
                dsl.skip(query.getInteger(key))
            } else if (key == "_take") {
                dsl.take(query.getInteger(key))
            } else if (key == "_orderBy") {
                orderByList = query.getJSONArray(key)
            } else if (key == "_joinType") {
                joinType = query.getString(key)
            } else if (key == "_joinOuter") {
                joinOuter = query.getString(key)
            } else if (key == "_joinInner") {
                joinInner = query.getString(key)
            } else {
                val p = query.get(key)
                var realKey = key
                var isActualField = true

                if (p is JSONObject) {
                    if (p.containsKey("_from")) {
                        isActualField = false
                        innerList.add(Pair(key, p))
                    } else {
                        for (fieldKey in p.keys) {
                            if (fieldKey == "_aliasOf") {
                                realKey = p.getString(fieldKey)
                            }
                        }

                        if (!fromModel.fieldMap.containsKey(realKey)) {
                            throw NoSuchFieldException("Field $realKey not found on model $fromModel")
                        }

                        dsl.column(fromModel.fieldMap[realKey]!!)
                    }
                } else {
                    if (!fromModel.fieldMap.containsKey(key)) {
                        throw NoSuchFieldException("Field $key not found on model $fromModel")
                    }

                    dsl.column(fromModel.fieldMap[key]!!)
                }

                if ((isActualField) && (permissions != QueryPermissions.ALL)) {
                    val fieldPermissions = allowEntity!!.allowFields.firstOrNull { it.field.name == realKey }

                    if (fieldPermissions == null) {
                        throw NoPermissionException("Current permissions does not contain field $key of entity model $fromClassName")
                    } else {
                        if (fieldPermissions.allowRules.size > 0) {
                            permissionConditionList.add(Pair(path, fieldPermissions.allowRules))
                        }
                    }
                }
            }
        }

        if (joinType != null) {
            val pathSegments = path.split(".")
            val outerModelMapKey = parentQuery!!.getString("_from") + "_" + pathSegments.subList(0, pathSegments.size - 1).joinToString(".")
            val outerModel = modelMap[outerModelMapKey]!!
            val joinConnector = { outerModel.fieldMap[joinOuter]!! eq fromModel.fieldMap[joinInner]!! }

            when (joinType) {
                "inner" -> {
                    dsl.innerJoin(fromModel, joinConnector)
                }

                "left" -> {
                    dsl.leftJoin(fromModel, joinConnector)
                }

                "right" -> {
                    dsl.rightJoin(fromModel, joinConnector)
                }

                else -> {
                    throw InvalidParameterException("Unsupported join type $joinType in query path $path")
                }
            }
        }

        for ((key, inner) in innerList) {
            recursiveParse("$path.$key", inner, permissions, query, dsl)
        }

        for (orderByKey in orderByList) {
            if (orderByKey is JSONObject) {
                for (f in orderByKey.keys) {
                    dsl.orderBy(fromModel.fieldMap[f]!!, orderByKey.getString(f) == "desc")
                }
            } else {
                dsl.orderBy(fromModel.fieldMap[orderByKey]!!, false)
            }
        }

        val conds = resolveConditions(conditionList, query)

        var expBuilder: ExpressionBuilder? = null
        var permExpBuilder: ExpressionBuilder? = null

        if (conds.isNotEmpty()) {
            expBuilder = ExpressionBuilder()

            for (i in 0..conds.size - 2) {
                expBuilder.expr(conds[i]).and()
            }

            expBuilder.expr(conds.last())
        }

        if (permissionConditionList.isNotEmpty()) {
            if (permExpBuilder == null) {
                permExpBuilder = ExpressionBuilder()
            }

            val list = mutableListOf<ExpressionBuilder>()

            for (permCond in permissionConditionList) {
                val resolvedPermConds = resolveConditions(permCond, query)

                if (resolvedPermConds.isNotEmpty()) {
                    val resolvedPermCondExpr = ExpressionBuilder()

                    for (i in 0..resolvedPermConds.size - 2) {
                        resolvedPermCondExpr.expr(resolvedPermConds[i]).and()
                    }

                    resolvedPermCondExpr.expr(resolvedPermConds.last())
                    list.add(resolvedPermCondExpr)
                }
            }

            for (i in 0..list.size - 2) {
                permExpBuilder.expr(list[i]).and()
            }

            permExpBuilder.expr(list.last())
        }

        val finalExpBuilder = ExpressionBuilder()

        if (expBuilder != null) {
            finalExpBuilder.beginGroup()
                    .expr(expBuilder)
                    .endGroup()
        } else {
            return dsl
        }

        if (permExpBuilder != null) {
            finalExpBuilder.and()
                    .beginGroup()
                    .expr(permExpBuilder)
                    .endGroup()
        }

        dsl.where { finalExpBuilder }

        return dsl
    }

    private fun resolveConditions(conditionList: Pair<String, JSONArray>, fullQuery: JSONObject): List<ExpressionBuilder> {
        val list = mutableListOf<ExpressionBuilder>()
        val (rootKey, conditions) = conditionList

        for (cond in conditions) {
            if (cond !is JSONObject) {
                continue
            }

            val key = cond.entries.first().key

            if (key == "_and") {
                val andList = cond.getJSONArray(key)
                val andExpressions = resolveConditions(Pair(rootKey, andList), fullQuery)
                val expBuilder = ExpressionBuilder()

                for (i in 0..andExpressions.size - 2) {
                    expBuilder.expr(andExpressions[i]).and()
                }

                expBuilder.expr(andExpressions.last())

                list.add(expBuilder)
            } else if (key == "_or") {
                val orList = cond.getJSONArray(key)
                val orExpressions = resolveConditions(Pair(rootKey, orList), fullQuery)
                val expBuilder = ExpressionBuilder()

                for (i in 0..orExpressions.size - 2) {
                    expBuilder.expr(orExpressions[i]).or()
                }

                expBuilder.expr(orExpressions.last())

                list.add(expBuilder)
            } else {
                val fieldCond = cond.getJSONObject(key)
                val fieldCondInfo = resolveFieldInfo(rootKey, fullQuery, key)
                val fieldCondRelation = fieldCond.entries.first().key
                val fieldCondTarget = fieldCond[fieldCondRelation]

                val expBuilder = buildWhereClause(fieldCondRelation, fieldCondInfo, fieldCondTarget)

                if (expBuilder == null) {
                    throw InvalidParameterException("Unsupported conditional operator $fieldCondRelation in conditions $cond")
                }

                list.add(expBuilder)
            }
        }

        return list
    }

    private fun buildWhereClause(fieldCondRelation: String?, fieldCondInfo: EntityField<*>, fieldCondTarget: Any?): ExpressionBuilder? {
        val expBuilder = ExpressionBuilder()

        when (fieldCondRelation) {
            "_gt", ">" -> expBuilder.field(fieldCondInfo)
                    .gt()
                    .parameter(fieldCondTarget)

            "_gte", ">=" -> expBuilder.field(fieldCondInfo)
                    .gte()
                    .parameter(fieldCondTarget)

            "_lt", "<" -> expBuilder.field(fieldCondInfo)
                    .lt()
                    .parameter(fieldCondTarget)

            "_lte", "<=" -> expBuilder.field(fieldCondInfo)
                    .lte()
                    .parameter(fieldCondTarget)

            "_eq", "=" -> expBuilder.field(fieldCondInfo)
                    .apply { if (fieldCondTarget == null) this.eqNull() else this.eq().parameter(fieldCondTarget) }

            "_ne", "!=" -> expBuilder.field(fieldCondInfo)
                    .apply { if (fieldCondTarget == null) this.neNull() else this.ne().parameter(fieldCondTarget) }

            "_like" -> expBuilder.field(fieldCondInfo)
                    .like()
                    .parameter(fieldCondTarget)

            "_in" -> expBuilder.field(fieldCondInfo)
                    .eq()
                    .beginFunction("ANY")
                    .parameter((fieldCondTarget as JSONArray).toArray())
                    .endFunction()

            else -> return null
        }

        return expBuilder
    }

    private fun resolveFieldInfo(rootKey: String, fullQuery: JSONObject, path: String): EntityField<*> {
        val segments = path.split(".")

        var currQuery = fullQuery

        for (s in segments) {
            val o = currQuery.getJSONObject(s)

            if ((o == null) || (!o.containsKey("_from"))) {
                continue
            }

            currQuery = o
        }

        val modelClassName = currQuery.getString("_from")
        var modelMapKey = modelClassName + "_$rootKey"

        if (segments.size > 1) {
            modelMapKey += "." + segments.subList(0, segments.size - 1).joinToString(".")
        }

        val model = modelMap[modelMapKey]!!

        return model.fieldMap[segments.last()]!!
    }
}