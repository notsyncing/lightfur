package io.github.notsyncing.lightfur.core.read;

import io.github.notsyncing.lightfur.core.DataSession;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface SqlModelListReader<T> extends SqlModelReader<T> {
    default CompletableFuture<List<T>> getList(DataSession db) {
        return db.queryList(this.getClass(), sql(), params());
    }
}
