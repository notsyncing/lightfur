package io.github.notsyncing.lightfur;

@FunctionalInterface
public interface DataSessionCreator<R, U> {
    DataSession<R, U> create();
}
