package io.github.notsyncing.lightfur.entity

import kotlin.reflect.KProperty

class EntityField<T> {
    private var data: T? = null

    operator fun getValue(thisRef: EntityModel?, property: KProperty<*>): T {
        return data as T
    }

    operator fun setValue(thisRef: EntityModel?, property: KProperty<*>, value: T) {
        if (thisRef == null) {
            return
        }

        thisRef.changedDataMap[property] = value
        data = value
    }
}
