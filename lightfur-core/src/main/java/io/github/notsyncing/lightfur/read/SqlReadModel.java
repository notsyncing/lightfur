package io.github.notsyncing.lightfur.read;

import io.github.notsyncing.lightfur.DataSession;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface SqlReadModel<T> extends ReadModel<T> {
    String sql();
    Object[] params();

    @Override
    default CompletableFuture<T> get(DataSession db) {
        return db.queryFirst(this.getClass(), sql(), params());
    }

    @Override
    default CompletableFuture<List<T>> getList(DataSession db) {
        return db.queryList(this.getClass(), sql(), params());
    }
}
