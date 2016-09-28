package io.github.notsyncing.lightfur.entity.utils

import io.github.notsyncing.lightfur.entity.EntityFieldInfo
import io.github.notsyncing.lightfur.entity.annotations.EntityColumn
import kotlin.reflect.KProperty

object EntityFieldUtils {
    fun getFieldInfo(property: KProperty<*>): EntityFieldInfo {
        val a = property.javaClass.getAnnotation(EntityColumn::class.java)
        return EntityFieldInfo(property.name, a.name, a.type, a.length, a.nullable, a.defaultValue, a.primaryKey)
    }
}