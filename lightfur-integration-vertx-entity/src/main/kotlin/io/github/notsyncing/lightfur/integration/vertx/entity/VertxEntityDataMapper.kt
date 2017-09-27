package io.github.notsyncing.lightfur.integration.vertx.entity

import io.github.notsyncing.lightfur.entity.EntityField
import io.github.notsyncing.lightfur.entity.EntityModel
import io.github.notsyncing.lightfur.integration.vertx.VertxDataMapper
import io.vertx.core.json.JsonObject

class VertxEntityDataMapper : VertxDataMapper() {
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