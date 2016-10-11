package io.github.notsyncing.lightfur.entity

import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

class EntityField<T>(val fieldType: KClass<*>,
                     val propertyRef: KProperty0<T>,
                     val info: EntityFieldInfo) {
    var data: T? = null
        get() = field
        set(value: T?) {
            field = value
            changed = true
        }

    var changed = false

    operator fun getValue(thisRef: EntityModel?, property: KProperty<*>): T {
        return data as T
    }

    operator fun setValue(thisRef: EntityModel?, property: KProperty<*>, value: T) {
        if (thisRef == null) {
            return
        }

        data = value
        changed = true
    }
}
