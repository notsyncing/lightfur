package io.github.notsyncing.lightfur.core.common;

public class LightfurConfigBuilder
{
    private LightfurConfig config = new LightfurConfig();

    public LightfurConfigBuilder()
    {
        config.setDatabase("postgres");
        config.setEnableDatabaseVersioning(false);
        config.setMaxPoolSize(10);
        config.setUsername("postgres");
        config.setPassword(null);
        config.setHost("localhost");
        config.setPort(5432);
        config.setEnableDataSessionLeakChecking(true);
        config.setDataSessionLeakCheckingInterval(5000);
    }

    public LightfurConfigBuilder host(String host)
    {
        config.setHost(host);
        return this;
    }

    public LightfurConfigBuilder port(int port)
    {
        config.setPort(port);
        return this;
    }

    public LightfurConfigBuilder username(String username)
    {
        config.setUsername(username);
        return this;
    }

    public LightfurConfigBuilder password(String password)
    {
        config.setPassword(password);
        return this;
    }

    public LightfurConfigBuilder database(String database)
    {
        config.setDatabase(database);
        return this;
    }

    public LightfurConfigBuilder maxPoolSize(int size)
    {
        config.setMaxPoolSize(size);
        return this;
    }

    public LightfurConfigBuilder databaseVersioning(boolean enable)
    {
        config.setEnableDatabaseVersioning(enable);
        return this;
    }

    public LightfurConfigBuilder dataSessionLeakChecking(boolean enable) {
        config.setEnableDataSessionLeakChecking(enable);
        return this;
    }

    public LightfurConfigBuilder dataSessionLeakCheckingInterval(int ms) {
        config.setDataSessionLeakCheckingInterval(ms);
        return this;
    }

    public LightfurConfig build()
    {
        return config;
    }
}
