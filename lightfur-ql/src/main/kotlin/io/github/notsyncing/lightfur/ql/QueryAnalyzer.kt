package io.github.notsyncing.lightfur.ql

import com.alibaba.fastjson.JSONObject

class QueryAnalyzer(val query: JSONObject) {
    var models: List<String> = emptyList()
        private set

    init {
        models = query.collectValuesOfKey("_from")
    }

    private fun JSONObject.collectValuesOfKey(key: String): List<String> {
        val values = mutableListOf<String>()

        fun _recursive(o: JSONObject, l: MutableList<String>) {
            if (o.containsKey(key)) {
                l.add(o.getString(key))
            }

            for (c in o.values) {
                if (c is JSONObject) {
                    _recursive(c, l)
                }
            }
        }

        _recursive(this, values)

        return values
    }
}