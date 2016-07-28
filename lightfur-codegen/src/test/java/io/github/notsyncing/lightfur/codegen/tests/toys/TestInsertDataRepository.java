package io.github.notsyncing.lightfur.codegen.tests.toys;

import io.github.notsyncing.lightfur.annotations.DataRepository;
import io.github.notsyncing.lightfur.dsl.Query;
import io.vertx.ext.sql.ResultSet;

import java.util.concurrent.CompletableFuture;

@DataRepository
public class TestInsertDataRepository
{
    public CompletableFuture<ResultSet> insertSimpleData()
    {
        TestModel m = new TestModel();
        m.id = 3;
        m.name = "test";
        m.flag = 2;

        return Query.add(TestModel.class, "simpleData")
                .values(m)
                .execute(m)
                .thenApply(o -> (ResultSet) o);
    }

    public CompletableFuture<ResultSet> insertSimpleDataWithIgnore()
    {
        TestModel m = new TestModel();
        m.id = 3;
        m.name = "test";
        m.flag = 2;

        return Query.add(TestModel.class, "simpleData_ignore")
                .values(m)
                .ignore(f -> f.name)
                .execute(m)
                .thenApply(o -> (ResultSet) o);
    }
}
