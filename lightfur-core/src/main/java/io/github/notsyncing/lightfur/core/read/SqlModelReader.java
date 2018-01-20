package io.github.notsyncing.lightfur.core.read;

public interface SqlModelReader<T> extends ModelReader<T> {
    String sql();
    Object[] params();
}
