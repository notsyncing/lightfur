package io.github.notsyncing.lightfur.dsl;

import io.github.notsyncing.lightfur.DataSession;
import io.github.notsyncing.lightfur.entity.DataModel;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface IQueryContext<T extends DataModel>
{
    String getTag();

    Class<T> getModelClass();

    IQueryContext<T> filter(Predicate<T> predicate);

    IQueryContext<T> sorted(Consumer<T> field);

    IQueryContext<T> take(long n);

    IQueryContext<T> skip(long n);

    <R> IQueryContext<T> map(Class<R> targetClass, BiConsumer<T, R> mapper);

    IQueryContext<T> count();

    CompletableFuture<List<T>> execute(DataSession db, Object... parameters);

    default CompletableFuture<List<T>> execute(Object... parameters)
    {
        return execute(null, parameters);
    }
}
