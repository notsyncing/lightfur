package io.github.notsyncing.lightfur.codegen.tests.toys;

import io.github.notsyncing.lightfur.annotations.DataRepository;
import io.github.notsyncing.lightfur.dsl.Query;
import io.vertx.ext.sql.ResultSet;

import java.util.concurrent.CompletableFuture;

@DataRepository
public class TestDeleteDataRepository
{
    public CompletableFuture<ResultSet> deleteSimpleData()
    {
        return Query.remove(TestModel.class, "simpleData")
                .filter(m -> m.id > 1)
                .execute()
                .thenApply(o -> (ResultSet) o);
    }
}
