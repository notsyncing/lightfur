package io.github.notsyncing.lightfur.dsl;

import io.github.notsyncing.lightfur.DataSession;
import io.github.notsyncing.lightfur.entity.TableDefineModel;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface IInsertContext<T extends TableDefineModel>
{
    IInsertContext<T> values(T data);

    IInsertContext<T> ignore(Function<T, Object> columnExpr);

    CompletableFuture<Object> execute(DataSession db, Object... parameters);

    default CompletableFuture<Object> execute(Object... parameters)
    {
        return execute(null, parameters);
    }
}
