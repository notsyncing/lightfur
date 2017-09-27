package io.github.notsyncing.lightfur.common;

public class LightfurConfig
{
    private String host;
    private int port;
    private String username;
    private String password;
    private String database;
    private int maxPoolSize;
    private boolean enableDatabaseVersioning;

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getDatabase()
    {
        return database;
    }

    public void setDatabase(String database)
    {
        this.database = database;
    }

    public int getMaxPoolSize()
    {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize)
    {
        this.maxPoolSize = maxPoolSize;
    }

    public boolean isEnableDatabaseVersioning()
    {
        return enableDatabaseVersioning;
    }

    public void setEnableDatabaseVersioning(boolean enableDatabaseVersioning)
    {
        this.enableDatabaseVersioning = enableDatabaseVersioning;
    }
}
