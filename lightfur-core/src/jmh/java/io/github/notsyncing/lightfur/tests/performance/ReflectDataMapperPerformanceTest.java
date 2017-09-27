package io.github.notsyncing.lightfur.tests.performance;

import io.github.notsyncing.lightfur.annotations.entity.Column;
import io.github.notsyncing.lightfur.integration.vertx.ReflectDataMapper;
import io.github.notsyncing.lightfur.tests.DataMapperTest;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.ResultSet;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class ReflectDataMapperPerformanceTest
{
    public enum TestEnum
    {
        TypeA,
        TypeB
    }

    public static class TestObject
    {
        @Column("id")
        public int id;

        @Column("username")
        public String name;

        @Column("date")
        public LocalDateTime date;

        @Column("type")
        public DataMapperTest.TestEnum type;

        @Column("list")
        public int[] list;

        @Column("list2")
        public int[] list2;

        @Column("long_number")
        public BigDecimal longNumber;
    }

    @State(Scope.Benchmark)
    public static class BenchmarkStates
    {
        private ReflectDataMapper dataMapper = new ReflectDataMapper();
        private ResultSet r = new ResultSet();

        public BenchmarkStates()
        {
            r.setColumnNames(Arrays.asList("id", "username", "date", "type", "list", "list2", "long_number"));

            JsonArray arr = new JsonArray();
            arr.add(1);
            arr.add("test");
            arr.add("2015-04-03T11:35:29.384");
            arr.add(DataMapperTest.TestEnum.TypeB.ordinal());
            arr.add("{1,2,3}");
            arr.add(new JsonArray("[4,5,6]"));
            arr.add("92375947293472934923794729345345345433.2345345345");
            r.setResults(Arrays.asList(arr));
        }
    }

    public static void main(String[] args) throws RunnerException
    {
        Options opts = new OptionsBuilder().include(".*" + ReflectDataMapper.class.getSimpleName() + ".*")
                    .build();

        new Runner(opts).run();
    }

    @Benchmark
    @BenchmarkMode(Mode.All)
    @Warmup(iterations = 10)
    @Measurement(iterations = 10)
    @Fork(1)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void reflectDataMapperBenchmark(BenchmarkStates states) throws InstantiationException, IllegalAccessException
    {
        TestObject o = states.dataMapper.map(TestObject.class, states.r);
    }
}
