package io.github.notsyncing.lightfur.core.read;

import io.github.notsyncing.lightfur.core.DataSession;

import java.util.concurrent.CompletableFuture;

public interface ModelSingleReader<T> extends ModelReader<T> {
    default CompletableFuture<T> get(DataSession db) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
