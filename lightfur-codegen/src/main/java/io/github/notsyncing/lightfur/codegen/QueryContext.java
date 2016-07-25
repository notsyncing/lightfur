package io.github.notsyncing.lightfur.codegen;

import io.github.notsyncing.lightfur.DataSession;
import io.github.notsyncing.lightfur.codegen.annotations.Generator;
import io.github.notsyncing.lightfur.codegen.generators.ExecuteGenerator;
import io.github.notsyncing.lightfur.codegen.generators.TakeGenerator;
import io.github.notsyncing.lightfur.dsl.IQueryContext;
import io.github.notsyncing.lightfur.entity.DataModel;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class QueryContext<T extends DataModel> implements IQueryContext<T>
{
    private Class<T> modelClass;
    private String tag;

    protected QueryContext(Class<T> modelClass, String tag)
    {
        this.modelClass = modelClass;
        this.tag = tag;
    }

    public String getTag()
    {
        return tag;
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
        return null;
    }

    public CompletableFuture<List<T>> execute(Object... parameters)
    {
        return execute(null, parameters);
    }
}
