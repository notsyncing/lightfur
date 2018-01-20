package io.github.notsyncing.lightfur.core;

@FunctionalInterface
public interface DataSessionCreator<C, R, U> {
    DataSession<C, R, U> create(Exception createStack);
}
