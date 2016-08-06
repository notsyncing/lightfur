package io.github.notsyncing.lightfur.versioning;

import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DbVersionUpdateInfo
{
    private String database;
    private Path path;
    private int version;
    private JsonObject data;

    public String getDatabase()
    {
        return database;
    }

    public void setDatabase(String database)
    {
        this.database = database;
    }

    public Path getPath()
    {
        return path;
    }

    public void setPath(Path path)
    {
        this.path = path;
    }

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    public JsonObject getData()
    {
        return data;
    }

    public void setData(JsonObject data)
    {
        this.data = data;

        this.version = data.getInteger("version");
        this.database = data.getString("database");
    }

    public String getUpdateContent() throws IOException
    {
        return new String(Files.readAllBytes(path), "utf-8");
    }
}
