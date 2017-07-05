package io.github.notsyncing.lightfur.ql.permission

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import kotlin.reflect.KProperty

class EntityFieldPermission(val field: KProperty<*>, val allowRules: JSONArray = JSONArray()) {
    fun rule(r: JSONObject): EntityFieldPermission {
        allowRules.add(r)
        return this
    }
}