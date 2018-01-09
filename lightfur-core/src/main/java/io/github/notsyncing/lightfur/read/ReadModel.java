package io.github.notsyncing.lightfur.read;

import io.github.notsyncing.lightfur.DataSession;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ReadModel<T> {
    default CompletableFuture<T> get(DataSession db) {
        throw new UnsupportedOperationException("Not implemented");
    }

    default CompletableFuture<List<T>> getList(DataSession db) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
