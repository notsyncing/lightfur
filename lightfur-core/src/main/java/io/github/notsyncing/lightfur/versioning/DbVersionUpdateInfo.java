package io.github.notsyncing.lightfur.versioning;

import com.alibaba.fastjson.JSONObject;

import java.nio.file.Path;

public class DbVersionUpdateInfo
{
    private String database;
    private Path path;
    private String id;
    private int version;
    private boolean fullVersion;
    private JSONObject data;
    private String updateContent;

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

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    public boolean isFullVersion()
    {
        return fullVersion;
    }

    public void setFullVersion(boolean fullVersion)
    {
        this.fullVersion = fullVersion;
    }

    public JSONObject getData()
    {
        return data;
    }

    public void setData(JSONObject data)
    {
        this.data = data;

        this.version = data.getInteger("version");
        this.database = data.getString("database");
        this.id = data.getString("id");

        if (data.containsKey("full_version")) {
            this.fullVersion = data.getBoolean("full_version");
        } else {
            fullVersion = false;
        }
    }

    public String getUpdateContent()
    {
        return updateContent;
    }

    public void setUpdateContent(String updateContent)
    {
        this.updateContent = updateContent;
    }
}
