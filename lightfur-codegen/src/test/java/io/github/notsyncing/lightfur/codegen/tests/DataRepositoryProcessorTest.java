package io.github.notsyncing.lightfur.codegen.tests;

import com.google.testing.compile.JavaFileObjects;
import io.github.notsyncing.lightfur.codegen.tests.toys.TestDeleteDataRepository;
import io.github.notsyncing.lightfur.codegen.tests.toys.TestUpdateDataRepository;
import io.github.notsyncing.lightfur.dsl.DataContext;
import io.github.notsyncing.lightfur.codegen.DataRepositoryProcessor;
import io.github.notsyncing.lightfur.DataSession;
import io.github.notsyncing.lightfur.DatabaseManager;
import io.github.notsyncing.lightfur.codegen.tests.toys.TestSelectDataRepository;
import io.github.notsyncing.lightfur.codegen.tests.toys.TestModel;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(VertxUnitRunner.class)
public class DataRepositoryProcessorTest
{
    private static final String TEST_SOURCE_PATH = "D:/lightfur/lightfur-codegen/src/test/java/";
    private static final String TEST_DB = "lightfur_test_db";

    private DatabaseManager db;

    @Before
    public void setUp(TestContext context)
    {
        Async async = context.async();

        db = DatabaseManager.getInstance();
        db.init("postgres");
        db.dropDatabase(TEST_DB, true)
                .thenCompose(r -> db.createDatabase(TEST_DB, true))
                .thenCompose(r -> db.getConnection())
                .thenCompose(c -> {
                    CompletableFuture f = new CompletableFuture();

                    c.execute("CREATE TABLE test_table (id SERIAL PRIMARY KEY, name TEXT)", h -> {
                        c.close();

                        if (h.succeeded()) {
                            f.complete(h);
                        } else {
                            f.completeExceptionally(h.cause());
                        }
                    });

                    return f;
                })
                .thenAccept(r -> async.complete())
                .exceptionally(ex -> {
                    context.fail((Throwable) ex);
                    async.complete();
                    return null;
                });
    }

    @After
    public void tearDown(TestContext context)
    {
        Async async = context.async();

        db.close()
                .thenCompose((Function) r -> {
                    db.init("postgres");
                    return db.dropDatabase(TEST_DB, true);
                })
                .thenCompose((Function) r -> db.close())
                .thenAccept(r -> async.complete())
                .exceptionally(ex -> {
                    context.fail((Throwable) ex);
                    async.complete();
                    return null;
                });
    }

    private JavaFileObject createTestFile(String fullTypeName) throws IOException
    {
        String source = IOUtils.toString(Files.newInputStream(Paths.get(TEST_SOURCE_PATH, "/" + fullTypeName.replaceAll("\\.", "/") + ".java")),
                Charset.defaultCharset());
        return JavaFileObjects.forSourceString(fullTypeName, source);
    }

    @Test
    public void testCompile() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException, ExecutionException, InterruptedException
    {
        JavaFileObject file1 = createTestFile(TestSelectDataRepository.class.getTypeName());
        JavaFileObject file2 = createTestFile(TestUpdateDataRepository.class.getTypeName());
        JavaFileObject file3 = createTestFile(TestDeleteDataRepository.class.getTypeName());
        //JavaFileObject file4 = createTestFile(TestInsertDataRepository.class.getTypeName());

        DataRepositoryProcessor processor = new DataRepositoryProcessor();
        processor.addTestFile(file1);
        processor.addTestFile(file2);
        processor.addTestFile(file3);
        //processor.addTestFile(file4);
        processor.addTestFile(createTestFile(TestModel.class.getTypeName()));

        assert_().about(javaSources())
                .that(Arrays.asList(file1, file2, file3/*, file4*/))
                .processedWith(processor)
                .compilesWithoutError();

        String result = processor.getTestResultContent();
        String fullName = processor.getTestGeneratedClassFullName();

        Path temp = Files.createTempDirectory("lightfur_test_generated_");
        String pkgName = fullName.substring(0, fullName.lastIndexOf("."));
        String className = fullName.substring(fullName.lastIndexOf(".") + 1);
        String path = pkgName.replace(".", "/");
        Path classPath = temp.resolve(path);
        Files.createDirectories(classPath);

        Path f = Files.createFile(classPath.resolve(className + ".java"));
        Files.write(f, result.getBytes("utf-8"));

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(null, null, null, f.toAbsolutePath().toString());

        URLClassLoader loader = URLClassLoader.newInstance(new URL[] { temp.toUri().toURL() });
        Class<?> clazz = Class.forName(fullName, true, loader);

        assertNotNull(clazz);
        assertTrue(DataContext.class.isAssignableFrom(clazz));

        DataContext query = (DataContext) clazz.newInstance();
        assertNotNull(query);

        FileUtils.deleteDirectory(temp.toFile());
    }

    @Test
    public void testQueryContextExecute(TestContext context) throws ExecutionException, InterruptedException
    {
        TestSelectDataRepository repo = new TestSelectDataRepository();
        Async async = context.async();

        DataSession db = new DataSession();
        db.execute("INSERT INTO test_table (name) VALUES (?)", "test").get();
        db.end();

        repo.getSimpleData()
                .thenAccept(m -> {
                    context.assertNotNull(m);
                    context.assertEquals("test", m.name);

                    async.complete();
                })
                .exceptionally(ex -> {
                    context.fail(ex);
                    return null;
                });
    }

    @Test
    public void testUpdateContextExecute(TestContext context) throws ExecutionException, InterruptedException
    {
        TestUpdateDataRepository repo = new TestUpdateDataRepository();
        Async async = context.async();

        DataSession db = new DataSession();
        db.execute("INSERT INTO test_table (name) VALUES (?)", "test1").get();
        db.execute("INSERT INTO test_table (name) VALUES (?)", "test2").get();
        db.end();

        repo.updateSimpleDataWithOuterVariable()
                .thenAccept(m -> {
                    context.assertNotNull(m);

                    DataSession d = new DataSession();
                    d.query("SELECT * FROM test_table")
                            .thenAccept(r -> {
                                d.end();

                                JsonObject o1 = r.getRows().get(0);
                                JsonObject o2 = r.getRows().get(1);

                                context.assertEquals("test1", o1.getString("name"));
                                context.assertEquals("tested", o2.getString("name"));

                                async.complete();
                            })
                            .exceptionally(ex -> {
                                d.end();
                                context.fail(ex);
                                return null;
                            });
                })
                .exceptionally(ex -> {
                    context.fail(ex);
                    return null;
                });
    }
}
