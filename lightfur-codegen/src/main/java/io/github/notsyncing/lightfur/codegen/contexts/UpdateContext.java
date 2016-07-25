package io.github.notsyncing.lightfur.codegen.contexts;

import io.github.notsyncing.lightfur.DataSession;
import io.github.notsyncing.lightfur.codegen.annotations.Generator;
import io.github.notsyncing.lightfur.codegen.generators.ExecuteGenerator;
import io.github.notsyncing.lightfur.codegen.generators.FilterGenerator;
import io.github.notsyncing.lightfur.codegen.generators.SetGenerator;
import io.github.notsyncing.lightfur.dsl.DataContext;
import io.github.notsyncing.lightfur.dsl.IUpdateContext;
import io.github.notsyncing.lightfur.entity.TableDefineModel;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class UpdateContext<T extends TableDefineModel> implements IUpdateContext<T>, DataContext
{
    private Class<T> modelClass;
    private String tag;
    private String sql;

    protected UpdateContext(Class<T> modelClass, String tag, String sql)
    {
        this.modelClass = modelClass;
        this.tag = tag;
        this.sql = sql;
    }

    public String getTag()
    {
        return tag;
    }

    public Class<T> getModelClass()
    {
        return modelClass;
    }

    public String getSql()
    {
        return sql;
    }

    @Generator(SetGenerator.class)
    public UpdateContext<T> set(Consumer<T> setter)
    {
        return this;
    }

    @Generator(FilterGenerator.class)
    public UpdateContext<T> filter(Predicate<T> predicate)
    {
        return this;
    }

    @Generator(ExecuteGenerator.class)
    public CompletableFuture<Object> execute(DataSession db, Object... parameters)
    {
        return CompletableFuture.completedFuture(null);
    }
}
