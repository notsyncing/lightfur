package io.github.notsyncing.lightfur.dsl;

import io.github.notsyncing.lightfur.DataSession;
import io.github.notsyncing.lightfur.entity.TableDefineModel;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface IUpdateContext<T extends TableDefineModel>
{
    String getTag();

    Class<T> getModelClass();

    IUpdateContext<T> set(Consumer<T> setter);

    IUpdateContext<T> filter(Predicate<T> predicate);

    CompletableFuture<Object> execute(DataSession db, Object... parameters);

    default CompletableFuture<Object> execute(Object... parameters)
    {
        return execute(null, parameters);
    }
}
