package io.github.notsyncing.lightfur;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.notsyncing.lightfur.common.LightfurConfig;
import io.github.notsyncing.lightfur.common.LightfurConfigBuilder;
import io.github.notsyncing.lightfur.versioning.DatabaseVersionManager;
import io.github.notsyncing.lightfur.versioning.DbUpdateFileCollector;

import java.util.concurrent.CompletableFuture;

/**
 * 数据库相关的各种操作
 */
public class DatabaseManager
{
    private static DatabaseManager instance = new DatabaseManager();
    private static DatabaseDriver driver;

    public LightfurConfig configs;

    private FastClasspathScanner cpScanner;
    private String currentDatabase;

    private DatabaseManager()
    {
        cpScanner = new FastClasspathScanner("-com.github.mauricio", "-scala", "-kotlin");
    }

    /**
     * 获取该类的实例
     * @return 数据库相关的各种操作的类
     */
    public static DatabaseManager getInstance()
    {
        return instance;
    }

    public static void setDriver(DatabaseDriver driver) {
        DatabaseManager.driver = driver;
    }

    public LightfurConfig getConfigs()
    {
        return configs;
    }

    public String getCurrentDatabase() {
        return currentDatabase;
    }

    /**
     * 设置到数据库的连接参数
     * @param config 相关配置
     */
    public CompletableFuture<Void> init(LightfurConfig config)
    {
        configs = config;

        if (driver == null) {
            throw new RuntimeException("You must specify a DatabaseDriver!");
        }

        if ((config.isEnableDatabaseVersioning()) && (!config.getDatabase().equals("postgres"))) {
            String currentDatabase = config.getDatabase();
            config.setDatabase("postgres");
            driver.init(config);

            DatabaseVersionManager versionManager = new DatabaseVersionManager(this, cpScanner);
            return versionManager.upgradeToLatest(currentDatabase)
                    .thenCompose(r -> setDatabase(currentDatabase));
        } else {
            driver.init(config);
        }

        currentDatabase = config.getDatabase();

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
        return driver.close();
    }

    /**
     * 异步获取数据库连接对象
     * @return 包含数据库连接对象的 CompletableFuture 对象
     */
    public CompletableFuture<Object> getConnection()
    {
        return driver.getConnection();
    }

    /**
     * 异步创建数据库
     * @param databaseName 要创建的数据库名称
     * @param switchTo 创建完毕后是否切换到该数据库
     * @return 指示是否完成的 CompletableFuture 对象
     */
    public CompletableFuture<Void> createDatabase(String databaseName, boolean switchTo)
    {
        CompletableFuture<Void> f = driver.createDatabase(databaseName);

        if (!switchTo) {
            return f;
        } else {
            return f.thenCompose(x -> setDatabase(databaseName));
        }
    }

    /**
     * 异步删除数据库
     * @param databaseName 要删除的数据库名称
     * @param ifExists 仅在该数据库存在时删除之
     * @return 指示是否完成的 CompletableFuture 对象
     */
    public CompletableFuture<Void> dropDatabase(String databaseName, boolean ifExists)
    {
        return setDatabase("postgres")
                .thenCompose(x -> driver.dropDatabase(databaseName, ifExists));
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
        if (configs.getDatabase().equals(databaseName)) {
            return CompletableFuture.completedFuture(null);
        }

        configs.setDatabase(databaseName);

        return driver.recreate(configs)
                .thenAccept(r -> {
                    currentDatabase = databaseName;
                });
    }

    /**
     * 异步升级数据库到最新版本
     * @param databaseName 要升级的数据库名称
     * @return 指示是否完成升级的 CompletableFuture 对象
     */
    public CompletableFuture<Void> upgradeDatabase(String databaseName)
    {
        DatabaseVersionManager versionManager = new DatabaseVersionManager(this, cpScanner);
        return versionManager.upgradeToLatest(databaseName);
    }

    /**
     * 异步升级数据库到最新版本
     * @param databaseName 要升级的数据库名称
     * @param collector 升级文件收集器
     * @return 指示是否完成升级的 CompletableFuture 对象
     */
    public CompletableFuture<Void> upgradeDatabase(String databaseName, DbUpdateFileCollector collector) {
        DatabaseVersionManager versionManager = new DatabaseVersionManager(this, cpScanner);
        return versionManager.upgradeToLatest(databaseName, collector);
    }
}
