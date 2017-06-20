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
            val field = it.value as EntityField<Any?>

            field.data = v
            field.changed = false
        }

        return o as T
    }
}