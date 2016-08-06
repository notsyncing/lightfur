package io.github.notsyncing.lightfur.versioning;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.FileMatchProcessorWithContext;
import io.github.notsyncing.lightfur.DatabaseManager;
import io.github.notsyncing.lightfur.utils.FutureUtils;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DatabaseVersionManager
{
    private DatabaseManager db;
    private SQLConnection conn;
    private FastClasspathScanner scanner;
    private JsonObject versionData;

    public DatabaseVersionManager(DatabaseManager db, FastClasspathScanner scanner)
    {
        this.db = db;
        this.scanner = scanner;
    }

    public CompletableFuture<Void> upgradeToLatest()
    {
        String dbName = db.getConfigs().getDatabase();

        return db.setDatabase("postgres")
                .thenCompose(r -> db.getConnection())
                .thenAccept(r -> conn = r)
                .thenCompose(r -> createDatabaseIfNotExists(dbName))
                .thenCompose(r -> {
                    conn.close();
                    return db.setDatabase(dbName);
                })
                .thenCompose(r -> db.getConnection())
                .thenCompose(r -> {
                    conn = r;

                    return initLightfurTables();
                })
                .thenCompose(r -> getCurrentDatabaseVersionData())
                .thenCompose(r -> {
                    versionData = r;
                    final int[] currVer = { r.getInteger("version") };

                    List<DbVersionUpdateInfo> updates = collectUpdateFiles(dbName);

                    CompletableFuture<Void> f = CompletableFuture.completedFuture(null);

                    for (DbVersionUpdateInfo u : updates) {
                        if (u.getVersion() <= currVer[0]) {
                            continue;
                        }

                        f = f.thenCompose(r2 -> {
                            try {
                                return doUpdate(u).thenAccept(r3 -> currVer[0] = r3.getVersion());
                            } catch (IOException e) {
                                e.printStackTrace();

                                return FutureUtils.failed(e);
                            }
                        });
                    }

                    return f;
                })
                .thenCompose(r -> {
                    CompletableFuture<Void> f = new CompletableFuture<>();

                    conn.close(r2 -> {
                        if (r2.failed()) {
                            f.completeExceptionally(r2.cause());
                            return;
                        }

                        f.complete(null);
                    });

                    return f;
                })
                .exceptionally(ex -> {
                    conn.close();

                    ex.printStackTrace();

                    return null;
                });
    }

    private CompletableFuture<Void> createDatabaseIfNotExists(String name)
    {
        CompletableFuture<Void> f = new CompletableFuture<>();

        conn.queryWithParams("SELECT 1 FROM pg_database WHERE datname = ?", new JsonArray().add(name), r -> {
            if (r.failed()) {
                f.completeExceptionally(r.cause());
                return;
            }

            ResultSet result = r.result();

            if (result.getNumRows() <= 0) {
                conn.query("CREATE DATABASE \"" + name + "\"", r2 -> {
                   if (r2.failed()) {
                       f.completeExceptionally(r2.cause());
                       return;
                   }

                   f.complete(null);
                });
            } else {
                f.complete(null);
            }
        });

        return f;
    }

    private CompletableFuture<Void> initLightfurTables()
    {
        JsonObject initData = new JsonObject();
        initData.put("version", -1);

        CompletableFuture<Void> f = new CompletableFuture<>();

        conn.query("CREATE SCHEMA IF NOT EXISTS lightfur", r -> {
            if (r.failed()) {
                f.completeExceptionally(r.cause());
                return;
            }

            conn.query("CREATE TABLE IF NOT EXISTS lightfur.version_data (data JSONB)", r2 -> {
                if (r2.failed()) {
                    f.completeExceptionally(r2.cause());
                    return;
                }

                String sql = "INSERT INTO lightfur.version_data (data)\n" +
                        "SELECT ?::jsonb\n" +
                        "WHERE NOT EXISTS (SELECT 1 FROM lightfur.version_data)";

                conn.queryWithParams(sql, new JsonArray().add(initData.toString()), r3 -> {
                    if (r3.failed()) {
                        f.completeExceptionally(r3.cause());
                        return;
                    }

                    f.complete(null);
                });
            });
        });

        return f;
    }

    private CompletableFuture<JsonObject> getCurrentDatabaseVersionData()
    {
        CompletableFuture<JsonObject> f = new CompletableFuture<>();

        conn.query("SELECT COALESCE(data::text, '{}') FROM lightfur.version_data LIMIT 1", r -> {
            if (r.failed()) {
                f.completeExceptionally(r.cause());
                return;
            }

            String s = r.result().getResults().get(0).getString(0);

            f.complete(new JsonObject(s));
        });

        return f;
    }

    private List<DbVersionUpdateInfo> collectUpdateFiles(String databaseName)
    {
        List<DbVersionUpdateInfo> files = new ArrayList<>();
        String startMagic = " LIGHTFUR {";
        String endMagic = "} END";

        scanner.matchFilenameExtension("sql", (absolutePath, relativePathStr, inputStream, lengthBytes) -> {
            char[] header = new char[1024];

            try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                reader.read(header, 0, 1024);
            }

            String s = new String(header);
            int start = s.indexOf(startMagic);

            if (start < 0) {
                return;
            }

            start += startMagic.length() - 1;
            int end = s.indexOf(endMagic);

            if (end < 0) {
                return;
            }

            String json = s.substring(start, end + 1);
            JsonObject data = new JsonObject(json);
            DbVersionUpdateInfo info = new DbVersionUpdateInfo();
            info.setData(data);
            info.setPath(absolutePath);

            if (!info.getDatabase().equals(databaseName)) {
                return;
            }

            files.add(info);
        }).scan();

        files.sort((c1, c2) -> c1.getVersion() - c2.getVersion());

        return files;
    }

    private CompletableFuture<DbVersionUpdateInfo> doUpdate(DbVersionUpdateInfo info) throws IOException
    {
        CompletableFuture<DbVersionUpdateInfo> f = new CompletableFuture<>();

        conn.query(info.getUpdateContent(), r -> {
            if (r.failed()) {
                f.completeExceptionally(r.cause());
                return;
            }

            versionData.put("version", info.getVersion());

            conn.updateWithParams("UPDATE lightfur.version_data SET data = ?::jsonb", new JsonArray().add(versionData.toString()), r2 -> {
                if (r2.failed()) {
                    f.completeExceptionally(r2.cause());
                    return;
                }

                f.complete(info);
            });
        });

        return f;
    }
}
