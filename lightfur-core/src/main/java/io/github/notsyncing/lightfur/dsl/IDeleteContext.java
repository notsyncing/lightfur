package io.github.notsyncing.lightfur.dsl;

import io.github.notsyncing.lightfur.DataSession;
import io.github.notsyncing.lightfur.entity.TableDefineModel;

import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public interface IDeleteContext<T extends TableDefineModel>
{
    IDeleteContext<T> filter(Predicate<T> predicate);

    CompletableFuture<Object> execute(DataSession db, Object... parameters);

    default CompletableFuture<Object> execute(Object... parameters)
    {
        return execute(null, parameters);
    }
}
