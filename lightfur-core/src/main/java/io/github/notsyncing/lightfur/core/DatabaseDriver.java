package io.github.notsyncing.lightfur.core;

import io.github.notsyncing.lightfur.core.common.LightfurConfig;

import java.util.concurrent.CompletableFuture;

public abstract class DatabaseDriver<C> {
    public abstract void init(LightfurConfig config);

    public abstract CompletableFuture<Void> close();

    public abstract CompletableFuture<Void> recreate(LightfurConfig config);

    public abstract CompletableFuture<C> getConnection();

    public abstract CompletableFuture<Void> createDatabase(String name);

    public abstract CompletableFuture<Void> dropDatabase(String name, boolean ifExists);
}
