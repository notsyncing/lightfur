package io.github.notsyncing.lightfur.codegen.contexts;

import io.github.notsyncing.lightfur.DataSession;
import io.github.notsyncing.lightfur.codegen.annotations.Generator;
import io.github.notsyncing.lightfur.codegen.generators.ExecuteGenerator;
import io.github.notsyncing.lightfur.codegen.generators.IgnoreGenerator;
import io.github.notsyncing.lightfur.codegen.generators.SetGenerator;
import io.github.notsyncing.lightfur.codegen.generators.ValuesGenerator;
import io.github.notsyncing.lightfur.dsl.DataContext;
import io.github.notsyncing.lightfur.dsl.IInsertContext;
import io.github.notsyncing.lightfur.entity.TableDefineModel;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class InsertContext<T extends TableDefineModel> extends DataContext implements IInsertContext<T>
{
    private Class<T> modelClass;

    protected InsertContext(Class<T> modelClass, String tag, String sql)
    {
        super(tag, sql);

        this.modelClass = modelClass;
    }

    public Class<T> getModelClass()
    {
        return modelClass;
    }

    @Override
    @Generator(ValuesGenerator.class)
    public IInsertContext<T> values(T data)
    {
        return this;
    }

    @Override
    @Generator(IgnoreGenerator.class)
    public IInsertContext<T> ignore(Function<T, Object> columnExpr)
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
