package io.github.notsyncing.lightfur.entity

import com.alibaba.fastjson.annotation.JSONField
import io.github.notsyncing.lightfur.entity.dsl.EntityDSL
import io.github.notsyncing.lightfur.entity.dsl.EntityInsertDSL
import io.github.notsyncing.lightfur.sql.models.TableModel
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

abstract class EntityModel(@JSONField(serialize = false, deserialize = false) val database: String? = null,
                           @JSONField(serialize = false, deserialize = false) val schema: String? = null,
                           @JSONField(serialize = false, deserialize = false) val table: String) {
    companion object {
        @JSONField(serialize = false, deserialize = false)
        val primaryKeyFieldCache = ConcurrentHashMap<Class<EntityModel>, MutableSet<KProperty<*>>>()

        fun getPrimaryKeyFieldsFromCache(modelClass: Class<EntityModel>): MutableSet<KProperty<*>> {
            var l = primaryKeyFieldCache[modelClass]

            if (l == null) {
                l = mutableSetOf()
                primaryKeyFieldCache[modelClass] = l
            }

            return l
        }
    }

    @JSONField(serialize = false, deserialize = false)
    lateinit var primaryKeyFields: Set<KProperty<*>>

    @JSONField(serialize = false, deserialize = false)
    val fieldMap = ConcurrentHashMap<String, EntityField<*>>()

    @JSONField(serialize = false, deserialize = false)
    val primaryKeyFieldInfos = ArrayList<EntityFieldInfo>()

    @JSONField(serialize = false, deserialize = false)
    var skipTableName = false

    @JSONField(serialize = false, deserialize = false)
    var skipTableAlias = false

    init {
        if (!EntityGlobal.tableModels.containsKey(this::class.java)) {
            val t = TableModel()
            t.name = table
            t.database = database
            t.schema = schema

            EntityGlobal.tableModels[this::class.java as Class<EntityModel>] = t
        }

        if (!EntityGlobal.fieldInfoInners.containsKey(this::class.java)) {
            EntityGlobal.fieldInfoInners[this::class.java as Class<EntityModel>] = ConcurrentHashMap()
        }
    }

    protected inline fun <reified T> field(column: String? = null, type: String = "",
                                           length: Int = 0, nullable: Boolean = false, defaultValue: String = "",
                                           primaryKey: Boolean = false, autoGenerated: Boolean = false): EntityField<T> {
        return EntityField(T::class.java, column, type, length, nullable, defaultValue, primaryKey, autoGenerated)
    }

    infix fun F(property: KProperty0<*>): EntityFieldInfo = fieldMap[property.name]!!.info

    fun assumeNoChange() {
        fieldMap.forEach { it.value.changed = false }
    }

    fun assumeAllChanged() {
        fieldMap.forEach { it.value.changed = true }
    }

    fun select() = EntityDSL.select(this).from()

    fun insert(): EntityInsertDSL<EntityModel> {
        assumeAllChanged()

        for ((i, v) in primaryKeyFieldInfos.withIndex()) {
            if (v.inner.dbAutoGenerated == true) {
                fieldMap[v.inner.name]!!.changed = false
            }
        }

        return EntityDSL.insert(this).values()
    }

    fun update() = EntityDSL.update(this).set()

    fun delete() = EntityDSL.delete(this)
}