package io.github.notsyncing.lightfur.utils;

import java.util.concurrent.CompletableFuture;

public class FutureUtils
{
    public static <T> CompletableFuture<T> failed(Throwable e)
    {
        CompletableFuture<T> f = new CompletableFuture<>();
        f.completeExceptionally(e);
        return f;
    }
}
