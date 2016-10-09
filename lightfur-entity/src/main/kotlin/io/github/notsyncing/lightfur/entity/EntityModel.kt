package io.github.notsyncing.lightfur.entity

import io.github.notsyncing.lightfur.entity.dsl.EntityDSL
import io.github.notsyncing.lightfur.entity.dsl.EntityInsertDSL
import io.github.notsyncing.lightfur.sql.models.TableModel
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KProperty0

abstract class EntityModel(val database: String? = null,
                           val schema: String? = null,
                           val table: String) {
    val changedDataMap = ConcurrentHashMap<KProperty0<*>, Any?>()
    val fieldMap = ConcurrentHashMap<KProperty0<*>, EntityField<*>>()
    val fieldInfo = ConcurrentHashMap<KProperty0<*>, EntityFieldInfo>()

    init {
        if (!EntityGlobal.tableModels.contains(this.javaClass)) {
            val t = TableModel()
            t.name = table
            t.database = database
            t.schema = schema

            EntityGlobal.tableModels[this.javaClass] = t
        }
    }

    private fun getPrimaryKeyField() = fieldInfo.entries.firstOrNull() { p -> p.value.dbPrimaryKey }?.key

    protected fun <T> field(property: KProperty0<T>, column: String? = null, type: String = "", length: Int = 0,
                            nullable: Boolean = false, defaultValue: String = "",
                            primaryKey: Boolean = false, autoGenerated: Boolean = false): EntityField<T> {
        val info = EntityFieldInfo(this, property.name, column ?: property.name, type, length, nullable, defaultValue,
                primaryKey, autoGenerated)
        fieldInfo[property] = info

        val f = EntityField(property)
        fieldMap[property] = f

        return f
    }

    infix fun F(property: KProperty0<*>): EntityFieldInfo = fieldInfo[property]!!

    fun assumeNoChange() {
        changedDataMap.clear()
    }

    fun assumeAllChanged() {
        fieldMap.keys().iterator().forEach { p -> changedDataMap[p] = p.get() }
    }

    fun select() = EntityDSL.select(this).from()

    fun insert(): EntityInsertDSL {
        assumeAllChanged()

        val pf = getPrimaryKeyField()
        val pfInfo = fieldInfo[pf]

        if ((pf != null) && (pfInfo != null)) {
            if (pfInfo.dbAutoGenerated) {
                changedDataMap.remove(pf)
            }
        }

        return EntityDSL.insert(this).values()
    }

    fun update() = EntityDSL.update(this)

    fun delete() = EntityDSL.delete(this)
}