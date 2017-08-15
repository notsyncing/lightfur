package io.github.notsyncing.lightfur.entity.utils

import java.util.concurrent.ConcurrentHashMap

fun <K, V> ConcurrentHashMap<K, V>.removeIf(predicate: (Map.Entry<K, V>) -> Boolean) {
    val iter = this.iterator()

    while (iter.hasNext()) {
        if (predicate(iter.next())) {
            iter.remove()
        }
    }
}