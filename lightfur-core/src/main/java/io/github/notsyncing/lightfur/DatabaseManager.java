package io.github.notsyncing.lightfur;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.FileMatchContentsProcessor;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.FileMatchProcessor;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.FileMatchProcessorWithContext;
import io.github.notsyncing.lightfur.annotations.GeneratedDataContext;
import io.github.notsyncing.lightfur.common.LightfurConfig;
import io.github.notsyncing.lightfur.common.LightfurConfigBuilder;
import io.github.notsyncing.lightfur.dsl.DataContext;
import io.github.notsyncing.lightfur.dsl.IQueryContext;
import io.github.notsyncing.lightfur.dsl.Query;
import io.github.notsyncing.lightfur.versioning.DatabaseVersionManager;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.PostgreSQLClient;
import io.vertx.ext.sql.SQLConnection;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * 数据库相关的各种操作
 */
public class DatabaseManager
{
    private static DatabaseManager instance = new DatabaseManager();

    private Vertx vertx;
    private AsyncSQLClient client;
    private LightfurConfig configs;
    private FastClasspathScanner cpScanner;

    private DatabaseManager()
    {
        cpScanner = new FastClasspathScanner()
                .matchClassesWithAnnotation(GeneratedDataContext.class,
                        c -> Query.addDataContextImplementation((Class<? extends DataContext>)c))
                .scan();
    }

    /**
     * 获取该类的实例
     * @return 数据库相关的各种操作的类
     */
    public static DatabaseManager getInstance()
    {
        return instance;
    }

    public LightfurConfig getConfigs()
    {
        return configs;
    }

    /**
     * 设置到数据库的连接参数
     * @param config 相关配置
     */
    public CompletableFuture<Void> init(LightfurConfig config)
    {
        VertxOptions opts = new VertxOptions()
                .setBlockedThreadCheckInterval(60 * 60 * 1000);

        vertx = Vertx.vertx(opts);

        configs = config;
        client = PostgreSQLClient.createNonShared(vertx, configs.toVertxConfig());

        if (config.isEnableDatabaseVersioning()) {
            DatabaseVersionManager versionManager = new DatabaseVersionManager(this, cpScanner);
            return versionManager.upgradeToLatest();
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * 设置到数据库的连接参数
     * @param host 数据库主机名/IP地址
     * @param port 数据库端口号
     * @param username 数据库用户名
     * @param password 该数据库用户的密码
     * @param databaseName 要连接的数据库的名称
     * @param maxPoolSize 连接池大小
     */
    public CompletableFuture<Void> init(String host, int port, String username, String password, String databaseName, int maxPoolSize)
    {
        LightfurConfig config = new LightfurConfigBuilder()
                .host(host)
                .port(port)
                .username(username)
                .password(password)
                .database(databaseName)
                .maxPoolSize(maxPoolSize)
                .build();

        return init(config);
    }

    /**
     * 设置到数据库的连接参数，主机默认为 localhost，端口默认为 5432
     * @param username 数据库用户名
     * @param password 该数据库用户的密码
     * @param databaseName 要连接的数据库的名称
     */
    public CompletableFuture<Void> init(String username, String password, String databaseName)
    {
        return init("localhost", 5432, username, password, databaseName, 10);
    }

    /**
     * 设置到数据库的连接参数，主机默认为 localhost，端口默认为 5432，用户名默认为 postgres，密码默认为空
     * @param databaseName 要连接的数据库的名称
     */
    public CompletableFuture<Void> init(String databaseName)
    {
        return init("postgres", null, databaseName);
    }

    /**
     * 异步关闭数据库客户端
     * @return 指示数据库客户端关闭是否完成的 CompletableFuture 对象
     */
    public CompletableFuture<Void> close()
    {
        CompletableFuture<Void> f = new CompletableFuture<>();

        client.close(r -> {
            if (r.failed()) {
                f.completeExceptionally(r.cause());
                return;
            }

            vertx.close(r2 -> {
                if (r2.failed()) {
                    f.completeExceptionally(r2.cause());
                    return;
                }

                f.complete(r2.result());
            });
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
    public CompletableFuture<Void> setDatabase(String databaseName)
    {
        CompletableFuture<Void> f = new CompletableFuture<>();

        if (client != null) {
            client.close(r -> {
                if (!r.succeeded()) {
                    f.completeExceptionally(r.cause());
                    return;
                }

                f.complete(r.result());
            });
        } else {
            f.complete(null);
        }

        return f.thenAccept(r -> {
            configs.setDatabase(databaseName);
            client = PostgreSQLClient.createNonShared(vertx, configs.toVertxConfig());
        });
    }
}
