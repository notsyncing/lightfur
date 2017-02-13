package io.github.notsyncing.lightfur.entity

import io.vertx.core.json.JsonObject

class EntityDataMapper : DataMapper() {
    override fun <T : Any> mapSingleRow(clazz: Class<T>?, row: JsonObject?): T? {
        if (clazz == null) {
            return null
        }

        val o = clazz.newInstance() as EntityModel

        o.fieldMap.forEach {
            val v = valueToType(it.value.fieldType, row?.getValue(o.fieldMap[it.key]?.info?.inner?.dbColumn))
            (it.value as EntityField<Any?>).data = v
        }

        return o as T
    }
}