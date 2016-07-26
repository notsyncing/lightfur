package io.github.notsyncing.lightfur.codegen.contexts;

import io.github.notsyncing.lightfur.DataSession;
import io.github.notsyncing.lightfur.codegen.annotations.Generator;
import io.github.notsyncing.lightfur.codegen.generators.ExecuteGenerator;
import io.github.notsyncing.lightfur.codegen.generators.FilterGenerator;
import io.github.notsyncing.lightfur.dsl.DataContext;
import io.github.notsyncing.lightfur.dsl.IDeleteContext;
import io.github.notsyncing.lightfur.dsl.IUpdateContext;
import io.github.notsyncing.lightfur.entity.TableDefineModel;

import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class DeleteContext<T extends TableDefineModel> extends DataContext implements IDeleteContext<T>
{
    private Class<T> modelClass;

    protected DeleteContext(Class<T> modelClass, String tag, String sql)
    {
        super(tag, sql);

        this.modelClass = modelClass;
    }

    public Class<T> getModelClass()
    {
        return modelClass;
    }

    @Override
    @Generator(FilterGenerator.class)
    public DeleteContext<T> filter(Predicate<T> predicate)
    {
        return this;
    }

    @Override
    @Generator(ExecuteGenerator.class)
    public CompletableFuture<Object> execute(DataSession db, Object... parameters)
    {
        return CompletableFuture.completedFuture(null);
    }
}
