package io.github.notsyncing.lightfur.codegen.tests.toys;

import io.github.notsyncing.lightfur.annotations.DataRepository;
import io.github.notsyncing.lightfur.dsl.Query;
import io.vertx.ext.sql.ResultSet;

import java.util.concurrent.CompletableFuture;

@DataRepository
public class TestDataRepository
{
    public CompletableFuture<TestModel> getSimpleData()
    {
        return Query.get(TestModel.class, "simpleData")
                .take(1)
                .execute()
                .thenApply(l -> l.size() > 0 ? l.get(0) : null);
    }

    public CompletableFuture<TestModel> getSimpleMappedData()
    {
        return Query.get(TestModel.class, "simpleMappedData")
                .map(TestModel.class, (m, r) -> {
                    m.id = r.id;
                    m.name = r.name;
                })
                .take(1)
                .execute()
                .thenApply(l -> l.size() > 0 ? l.get(0) : null);
    }

    public CompletableFuture<ResultSet> updateSimpleData()
    {
        return Query.update(TestModel.class, "simpleData")
                .filter(m -> m.id > 1)
                .set(m -> m.name = "tested")
                .execute()
                .thenApply(o -> (ResultSet) o);
    }

    public CompletableFuture<Integer> updateSimpleDataWithOuterVariable()
    {
        String s = "tested";

        return Query.update(TestModel.class, "simpleData")
                .filter(m -> m.id > 1)
                .set(m -> m.name = s)
                .execute()
                .thenApply(o -> (Integer)o);
    }
}
