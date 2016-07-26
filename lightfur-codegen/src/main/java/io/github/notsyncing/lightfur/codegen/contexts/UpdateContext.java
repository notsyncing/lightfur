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

public class UpdateContext<T extends TableDefineModel> extends DataContext implements IUpdateContext<T>
{
    private Class<T> modelClass;

    protected UpdateContext(Class<T> modelClass, String tag, String sql)
    {
        super(tag, sql);

        this.modelClass = modelClass;
    }

    public Class<T> getModelClass()
    {
        return modelClass;
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
