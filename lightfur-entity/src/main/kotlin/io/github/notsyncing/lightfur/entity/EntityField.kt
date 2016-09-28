package io.github.notsyncing.lightfur.entity

import io.github.notsyncing.lightfur.entity.utils.EntityFieldUtils
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KProperty

class EntityField<T>(private val property: KProperty<T>) {
    private var data: T? = null

    companion object {
        val fieldInfoCache = ConcurrentHashMap<KProperty<*>, EntityFieldInfo>()
    }

    init {
        fieldInfoCache[property] = EntityFieldUtils.getFieldInfo(property)
    }

    operator fun getValue(thisRef: EntityModel?, property: KProperty<*>): T {
        return data as T
    }

    operator fun setValue(thisRef: EntityModel?, property: KProperty<*>, value: T) {
        if (thisRef == null) {
            return
        }

        thisRef.changedDataMap[property.name] = value
        data = value
    }
}
