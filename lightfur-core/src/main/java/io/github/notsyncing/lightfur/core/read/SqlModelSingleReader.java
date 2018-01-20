package io.github.notsyncing.lightfur.core.read;

import io.github.notsyncing.lightfur.core.DataSession;

import java.util.concurrent.CompletableFuture;

public interface SqlModelSingleReader<T> extends SqlModelReader<T> {
    default CompletableFuture<T> get(DataSession db) {
        return db.queryFirst(this.getClass(), sql(), params());
    }
}
