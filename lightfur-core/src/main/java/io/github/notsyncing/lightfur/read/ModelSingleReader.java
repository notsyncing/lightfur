package io.github.notsyncing.lightfur.read;

import io.github.notsyncing.lightfur.DataSession;

import java.util.concurrent.CompletableFuture;

public interface ModelSingleReader<T> extends ModelReader<T> {
    default CompletableFuture<T> get(DataSession db) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
