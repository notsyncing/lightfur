package io.github.notsyncing.lightfur.ql

interface RawQueryResultProcessor {
    fun resultSetToList(resultSet: Any?): List<Map<String, Any?>>

    fun processValue(value: Any?): Any?
}