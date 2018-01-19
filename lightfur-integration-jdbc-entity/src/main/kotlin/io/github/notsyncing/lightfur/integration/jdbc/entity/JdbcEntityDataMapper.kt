package io.github.notsyncing.lightfur.integration.jdbc.entity

import io.github.notsyncing.lightfur.entity.EntityField
import io.github.notsyncing.lightfur.entity.EntityModel
import io.github.notsyncing.lightfur.integration.jdbc.JdbcDataMapper
import io.github.notsyncing.lightfur.integration.jdbc.ResultSetUtils
import java.sql.ResultSet

class JdbcEntityDataMapper : JdbcDataMapper() {
    private fun <T: Any?> mapCurrentRow(clazz: Class<T>?, results: ResultSet?): T {
        if ((clazz == null) || (results == null)) {
            return null as T
        }

        val o = clazz.newInstance() as EntityModel

        o.fieldMap.forEach {
            val columnIndex = ResultSetUtils.findColumnIndex(results, o.fieldMap[it.key]?.dbColumn)

            if (columnIndex <= 0) {
                return@forEach
            }

            val v = valueToType(it.value.fieldType, results.getObject(columnIndex))
            val field = it.value as EntityField<Any?>

            field.data = v
            field.changed = false
        }

        return o as T
    }

    override fun <T : Any?> map(clazz: Class<T>?, results: ResultSet?): T {
        if (results == null) {
            return null as T
        }

        if (results.next()) {
            return mapCurrentRow(clazz, results)
        } else {
            return null as T
        }
    }

    override fun <T : Any?> mapToList(clazz: Class<T>?, results: ResultSet?): MutableList<T> {
        val list = mutableListOf<T>()

        if (results == null) {
            return list
        }

        while (results.next()) {
            list.add(mapCurrentRow(clazz, results))
        }

        return list
    }
}