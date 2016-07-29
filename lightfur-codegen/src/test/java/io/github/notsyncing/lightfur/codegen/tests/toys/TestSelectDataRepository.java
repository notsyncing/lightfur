package io.github.notsyncing.lightfur.codegen.tests.toys;

import io.github.notsyncing.lightfur.annotations.DataRepository;
import io.github.notsyncing.lightfur.dsl.Query;
import io.github.notsyncing.lightfur.sql.models.wrappers.LongWrapper;
import io.vertx.ext.sql.ResultSet;

import java.util.concurrent.CompletableFuture;

@DataRepository
public class TestSelectDataRepository
{
    public CompletableFuture<TestModel> getSimpleData()
    {
        return Query.get(TestModel.class, "simpleData")
                .take(1)
                .execute()
                .thenApply(l -> l.size() > 0 ? l.get(0) : null);
    }

    public CompletableFuture<TestViewModel> getSimpleMappedData()
    {
        return Query.get(TestModel.class, "simpleData_mapped")
                .map(TestViewModel.class, (m, r) -> {
                    m.id = r.id;
                    m.name = r.name;
                })
                .take(1)
                .execute()
                .thenApply(l -> l.size() > 0 ? l.get(0) : null);
    }

    public CompletableFuture<TestModel> getSimpleDataWithCondition()
    {
        return Query.get(TestModel.class, "simpleData_cond")
                .filter(m -> m.id > 1)
                .execute()
                .thenApply(l -> l.size() > 0 ? l.get(0) : null);
    }

    public CompletableFuture<TestModel> getSimpleDataSorted()
    {
        return Query.get(TestModel.class, "simpleData_sorted")
                .sorted(m -> m.id, true)
                .execute()
                .thenApply(l -> l.size() > 0 ? l.get(0) : null);
    }

    public CompletableFuture<Long> getSimpleDataCount()
    {
        return Query.get(TestModel.class, "simpleData_count")
                .count()
                .execute()
                .thenApply(r -> r.size() > 0 ? r.get(0).getValue() : null);
    }
}
