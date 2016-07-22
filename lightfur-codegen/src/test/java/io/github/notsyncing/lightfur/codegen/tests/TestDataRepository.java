package io.github.notsyncing.lightfur.codegen.tests;

import io.github.notsyncing.lightfur.annotations.DataRepository;
import io.github.notsyncing.lightfur.dsl.Query;

import java.util.concurrent.CompletableFuture;

@DataRepository
public class TestDataRepository
{
    public CompletableFuture<TestModel> getSimpleData()
    {
        return Query.get(TestModel.class, "simpleData")
                .limit(1)
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
                .limit(1)
                .execute()
                .thenApply(l -> l.size() > 0 ? l.get(0) : null);
    }
}
