package io.github.notsyncing.lightfur.codegen.tests.toys;

import io.github.notsyncing.lightfur.annotations.DataRepository;
import io.github.notsyncing.lightfur.dsl.Query;
import io.vertx.ext.sql.ResultSet;

import java.util.concurrent.CompletableFuture;

@DataRepository
public class TestUpdateDataRepository
{
    public CompletableFuture<ResultSet> updateSimpleData()
    {
        return Query.update(TestModel.class, "simpleData")
                .filter(m -> m.id > 1)
                .set(m -> m.name = "tested")
                .execute()
                .thenApply(o -> (ResultSet) o);
    }

    public CompletableFuture<ResultSet> updateSimpleDataWithOuterVariable()
    {
        String s = "tested";

        return Query.update(TestModel.class, "simpleData_outVar")
                .filter(m -> m.id > 1)
                .set(m -> m.name = s)
                .execute(s)
                .thenApply(o -> (ResultSet)o);
    }
}
