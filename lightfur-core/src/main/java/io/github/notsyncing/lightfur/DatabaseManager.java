package io.github.notsyncing.lightfur;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.notsyncing.lightfur.annotations.GeneratedQueryContext;
import io.github.notsyncing.lightfur.dsl.IQueryContext;
import io.github.notsyncing.lightfur.dsl.Query;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.PostgreSQLClient;
import io.vertx.ext.sql.SQLConnection;

import java.util.concurrent.CompletableFuture;

/**
 * 数据库相关的各种操作
 */
public class DatabaseManager
{
    private static DatabaseManager instance = new DatabaseManager();

    private Vertx vertx;
    private AsyncSQLClient client;
    private JsonObject configs;

    private DatabaseManager()
    {
        new FastClasspathScanner()
                .matchClassesWithAnnotation(GeneratedQueryContext.class,
                        c -> Query.addQueryContextImplementation((Class<? extends IQueryContext>)c))
                .scan();

        VertxOptions opts = new VertxOptions()
                .setBlockedThreadCheckInterval(60 * 60 * 1000);

        vertx = Vertx.vertx(opts);
    }

    /**
     * 获取该类的实例
     * @return 数据库相关的各种操作的类
     */
    public static DatabaseManager getInstance()
    {
        return instance;
    }

    /**
     * 设置到数据库的连接参数
     * @param host 数据库主机名/IP地址
     * @param port 数据库端口号
     * @param username 数据库用户名
     * @param password 该数据库用户的密码
     * @param databaseName 要连接的数据库的名称
     */
    public void init(String host, int port, String username, String password, String databaseName)
    {
        configs = new JsonObject()
                .put("host", host)
                .put("port", port)
                .put("username", username)
                .put("password", password)
                .put("database", databaseName);

        client = PostgreSQLClient.createNonShared(vertx, configs);
    }

    /**
     * 设置到数据库的连接参数，主机默认为 localhost，端口默认为 5432
     * @param username 数据库用户名
     * @param password 该数据库用户的密码
     * @param databaseName 要连接的数据库的名称
     */
    public void init(String username, String password, String databaseName)
    {
        init("localhost", 5432, username, password, databaseName);
    }

    /**
     * 设置到数据库的连接参数，主机默认为 localhost，端口默认为 5432，用户名默认为 postgres，密码默认为空
     * @param databaseName 要连接的数据库的名称
     */
    public void init(String databaseName)
    {
        init("postgres", null, databaseName);
    }

    /**
     * 异步关闭数据库客户端
     * @return 指示数据库客户端关闭是否完成的 CompletableFuture 对象
     */
    public CompletableFuture close()
    {
        CompletableFuture f = new CompletableFuture();

        client.close(r -> {
            if (r.failed()) {
                f.completeExceptionally(r.cause());
                return;
            }

            f.complete(r.result());
        });

        return f;
    }

    /**
     * 异步获取数据库连接对象
     * @return 包含数据库连接对象的 CompletableFuture 对象
     */
    public CompletableFuture<SQLConnection> getConnection()
    {
        CompletableFuture<SQLConnection> future = new CompletableFuture<>();

        client.getConnection(r -> {
            if (r.succeeded()) {
                future.complete(r.result());
            } else {
                r.cause().printStackTrace();
                future.completeExceptionally(r.cause());
            }
        });

        return future;
    }

    /**
     * 异步创建数据库
     * @param databaseName 要创建的数据库名称
     * @param switchTo 创建完毕后是否切换到该数据库
     * @return 指示是否完成的 CompletableFuture 对象
     */
    public CompletableFuture<Void> createDatabase(String databaseName, boolean switchTo)
    {
        CompletableFuture<Void> f = new CompletableFuture<>();

        client.getConnection(r -> {
            if (!r.succeeded()) {
                f.completeExceptionally(r.cause());
                return;
            }

            SQLConnection c = r.result();
            c.execute("CREATE DATABASE \"" + databaseName + "\"", r2 -> {
                c.close();

                if (!r2.succeeded()) {
                    f.completeExceptionally(r2.cause());
                    return;
                }

                if (switchTo) {
                    setDatabase(databaseName);
                }

                f.complete(r2.result());
            });
        });

        return f;
    }

    /**
     * 异步删除数据库
     * @param databaseName 要删除的数据库名称
     * @param ifExists 仅在该数据库存在时删除之
     * @return 指示是否完成的 CompletableFuture 对象
     */
    public CompletableFuture<Void> dropDatabase(String databaseName, boolean ifExists)
    {
        setDatabase("postgres");

        CompletableFuture<Void> f = new CompletableFuture<>();

        client.getConnection(r -> {
            if (!r.succeeded()) {
                f.completeExceptionally(r.cause());
                return;
            }

            SQLConnection c = r.result();

            String sql = "DROP DATABASE";

            if (ifExists) {
                sql += " IF EXISTS";
            }

            c.execute(sql + " \"" + databaseName + "\"", r2 -> {
                c.close();

                if (!r2.succeeded()) {
                    f.completeExceptionally(r2.cause());
                    return;
                }

                f.complete(r2.result());
            });
        });

        return f;
    }

    /**
     * 异步删除数据库
     * @param databaseName 要删除的数据库名称
     * @return 指示是否完成的 CompletableFuture 对象
     */
    public CompletableFuture<Void> dropDatabase(String databaseName)
    {
        return dropDatabase(databaseName, false);
    }

    /**
     * 设置/切换要连接到的数据库
     * @param databaseName 要连接到的数据库名称
     */
    public void setDatabase(String databaseName)
    {
        configs.put("database", databaseName);
        client = PostgreSQLClient.createNonShared(vertx, configs);
    }
}
