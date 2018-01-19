package io.github.notsyncing.lightfur.entity

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

open class EntityReference<T>(val fieldType: Class<T>,
                              val fieldComponentType: Class<out EntityModel>? = null,
                              val refFieldName: String,
                              val keyFieldName: String? = null) : ReadWriteProperty<EntityModel, T> {
    var data: T? = null
        get() = field
        set(value) {
            changed = field != value
            field = value
        }

    var changed = false

    var name: String = ""
        private set

    val targetClass get() = fieldType as Class<EntityModel>
    val componentClass get() = fieldComponentType as Class<EntityModel>?

    var dbKeyColumn: String? = null
        private set

    lateinit var entity: EntityModel

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

        entity = thisRef

        val propertyName = property.name
        name = propertyName

        dbKeyColumn = if (keyFieldName == null) null else entity.fieldMap[keyFieldName]?.column

        thisRef.referenceMap[propertyName] = this

        return this
    }
}