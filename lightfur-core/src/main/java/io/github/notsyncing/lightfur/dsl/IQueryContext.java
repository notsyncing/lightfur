package io.github.notsyncing.lightfur.dsl;

import io.github.notsyncing.lightfur.DataSession;
import io.github.notsyncing.lightfur.entity.DataModel;
import io.github.notsyncing.lightfur.sql.models.wrappers.LongWrapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface IQueryContext<T extends DataModel>
{
    IQueryContext<T> filter(Predicate<T> predicate);

    IQueryContext<T> sorted(Function<T, Object> field, boolean desc);

    IQueryContext<T> take(long n);

    IQueryContext<T> skip(long n);

    <R extends DataModel> IQueryContext<R> map(Class<R> targetClass, BiConsumer<T, R> mapper);

    IQueryContext<LongWrapper> count();

    CompletableFuture<List<T>> execute(DataSession db, Object... parameters);

    default CompletableFuture<List<T>> execute(Object... parameters)
    {
        return execute(null, parameters);
    }
}
