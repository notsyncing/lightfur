package io.github.notsyncing.lightfur.entity

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

open class EntityReference<T>(val fieldType: Class<T>,
                              val fieldComponentType: Class<out EntityModel>? = null,
                              val refFieldName: String,
                              val keyFieldName: String? = null) : ReadWriteProperty<EntityModel, T> {
    class Info(val entity: EntityModel, val inner: Inner) {
        data class Inner(val name: String, val targetClass: Class<EntityModel>,
                         val componentClass: Class<EntityModel>?, val refFieldName: String,
                         val keyFieldName: String?, val dbKeyColumn: String?)
    }

    var data: T? = null
        get() = field
        set(value) {
            changed = field != value
            field = value
        }

    var changed = false

    lateinit var info: Info

    override fun getValue(thisRef: EntityModel, property: KProperty<*>): T {
        return data as T
    }

    override fun setValue(thisRef: EntityModel, property: KProperty<*>, value: T) {
        if (fieldComponentType != null) {
            throw UnsupportedOperationException("You cannot modify the variable of a one-to-many reference. " +
                    "Modify the collection of this field instead.")
        }

        changed = data != value
        data = value
    }

    operator fun provideDelegate(thisRef: EntityModel, property: KProperty<*>): ReadWriteProperty<EntityModel, T> {
        if (fieldComponentType != null) {
            data = mutableListOf<Any>() as T
        }

        val propertyName = property.name
        var inner = EntityGlobal.referenceInfoInners[thisRef::class.java]!![propertyName]

        if (inner == null) {
            inner = Info.Inner(propertyName, fieldType as Class<EntityModel>, fieldComponentType as Class<EntityModel>?,
                    refFieldName, keyFieldName, if (keyFieldName == null) null else thisRef.fieldMap[keyFieldName]?.column)

            EntityGlobal.referenceInfoInners[thisRef::class.java]!![propertyName] = inner
        }

        info = Info(thisRef, inner)

        thisRef.referenceMap[propertyName] = this

        return this
    }
}