package io.github.notsyncing.lightfur.read;

public interface SqlModelReader<T> extends ModelReader<T> {
    String sql();
    Object[] params();
}
