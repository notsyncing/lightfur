package io.github.notsyncing.lightfur.codegen.contexts;

import io.github.notsyncing.lightfur.DataSession;
import io.github.notsyncing.lightfur.codegen.annotations.Generator;
import io.github.notsyncing.lightfur.codegen.generators.ExecuteGenerator;
import io.github.notsyncing.lightfur.codegen.generators.TakeGenerator;
import io.github.notsyncing.lightfur.dsl.DataContext;
import io.github.notsyncing.lightfur.dsl.IQueryContext;
import io.github.notsyncing.lightfur.entity.DataModel;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class QueryContext<T extends DataModel> extends DataContext implements IQueryContext<T>
{
    private Class<T> modelClass;

    protected QueryContext(Class<T> modelClass, String tag, String sql)
    {
        super(tag, sql);

        this.modelClass = modelClass;
    }

    public Class<T> getModelClass()
    {
        return modelClass;
    }

    public QueryContext<T> filter(Predicate<T> predicate)
    {
        return this;
    }

    public QueryContext<T> sorted(Consumer<T> field)
    {
        return this;
    }

    @Generator(TakeGenerator.class)
    public QueryContext<T> take(long n)
    {
        return this;
    }

    public QueryContext<T> skip(long n)
    {
        return this;
    }

    public <R> QueryContext<T> map(Class<R> targetClass, BiConsumer<T, R> mapper)
    {
        return this;
    }

    public QueryContext<T> count()
    {
        return this;
    }

    @Generator(ExecuteGenerator.class)
    public CompletableFuture<List<T>> execute(DataSession db, Object... parameters)
    {
        return CompletableFuture.completedFuture(null);
    }
}
