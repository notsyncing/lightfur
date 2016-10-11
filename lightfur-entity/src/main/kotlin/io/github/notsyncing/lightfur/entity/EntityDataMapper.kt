package io.github.notsyncing.lightfur.entity

import io.vertx.core.json.JsonObject

class EntityDataMapper : DataMapper() {
    override fun <T : Any> mapSingleRow(clazz: Class<T>?, row: JsonObject?): T? {
        if (clazz == null) {
            return null
        }

        val o = clazz.newInstance() as EntityModel

        o.fieldMap.forEach {
            val v = valueToType(it.value.fieldType.java, row?.getValue(o.fieldInfo[it.key]?.dbColumn))
            (it.value as EntityField<Any?>).setData(v)
        }

        return o as T
    }
}