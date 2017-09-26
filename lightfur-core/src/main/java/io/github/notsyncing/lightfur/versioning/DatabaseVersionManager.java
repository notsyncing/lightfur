package io.github.notsyncing.lightfur.versioning;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.FileMatchProcessorWithContext;
import io.github.notsyncing.lightfur.DataSession;
import io.github.notsyncing.lightfur.DatabaseManager;
import io.vertx.core.json.JsonObject;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import static java.util.stream.Collectors.*;

// TODO: Add hooks during version update

public class DatabaseVersionManager
{
    private DatabaseManager db;
    private DataSession conn;
    private FastClasspathScanner scanner;
    private JsonObject versionData;
    private Logger log = Logger.getLogger(this.getClass().getSimpleName());

    public DatabaseVersionManager(DatabaseManager db, FastClasspathScanner scanner)
    {
        this.db = db;
        this.scanner = scanner;
    }

    public CompletableFuture<Void> upgradeToLatest(String dbName, DbUpdateFileCollector collector)
    {
        CompletableFuture<Void> ff;

        if (dbName.equals(db.getCurrentDatabase())) {
            ff = CompletableFuture.completedFuture(null);
        } else {
            ff = db.setDatabase("postgres")
                    .thenAccept(r -> conn = new DataSession())
                    .thenCompose(r -> createDatabaseIfNotExists(dbName))
                    .thenCompose(r -> conn.end())
                    .thenCompose(r -> db.setDatabase(dbName));
        }

        CompletableFuture<Void> fff = new CompletableFuture<>();

        ff.thenCompose(r -> {
                    conn = new DataSession();

                    return initLightfurTables();
                })
                .thenCompose(r -> getCurrentDatabaseVersionData())
                .thenCompose(r -> {
                    versionData = r;
                    List<DbVersionUpdateInfo> updates = collectUpdateFiles(dbName, collector);
                    Map<String, List<DbVersionUpdateInfo>> updateMap = updates.stream()
                            .collect(groupingBy(DbVersionUpdateInfo::getId, mapping(u -> u, toList())));

                    final CompletableFuture<Void>[] co = new CompletableFuture[]{CompletableFuture.completedFuture(null)};

                    updateMap.forEach((id, list) -> {
                        co[0] = co[0].thenCompose(r2 -> {
                            DbVersionUpdateInfo maxFullVersion = list.stream()
                                    .filter(DbVersionUpdateInfo::isFullVersion)
                                    .max(Comparator.comparingInt(DbVersionUpdateInfo::getVersion))
                                    .orElse(null);

                            final int[] currVersion = {-1};

                            if (versionData.containsKey(id)) {
                                currVersion[0] = versionData.getJsonObject(id).getInteger("version");
                            }

                            final CompletableFuture<Void>[] c = new CompletableFuture[]{null};

                            if ((currVersion[0] < 0) && (maxFullVersion != null)) {
                                c[0] = doUpdate(maxFullVersion).thenAccept(x -> currVersion[0] = maxFullVersion.getVersion());
                            } else {
                                c[0] = CompletableFuture.completedFuture(null);
                            }

                            c[0] = c[0].thenCompose(x -> {
                                final CompletableFuture<Void>[] f = new CompletableFuture[]{CompletableFuture.completedFuture(null)};

                                list.stream()
                                        .filter(u -> !u.isFullVersion())
                                        .sorted(Comparator.comparingInt(DbVersionUpdateInfo::getVersion))
                                        .forEach(u -> {
                                            if (u.getVersion() <= currVersion[0]) {
                                                return;
                                            }

                                            f[0] = f[0].thenCompose(x2 -> doUpdate(u));
                                        });

                                return f[0];
                            });

                            return c[0];
                        });
                    });

                    return co[0];
                })
                .thenCompose(r -> conn.end())
                .thenAccept(r -> fff.complete(null))
                .exceptionally(ex -> {
                    conn.end().thenAccept(r -> fff.completeExceptionally(ex));

                    return null;
                });

        return fff;
    }

    public CompletableFuture<Void> upgradeToLatest(String dbName) {
        return upgradeToLatest(dbName, null);
    }

    private CompletableFuture<Void> createDatabaseIfNotExists(String name)
    {
        return conn.query("SELECT 1 FROM pg_database WHERE datname = ?", name)
                .thenCompose(result -> {
                    if (result.getNumRows() <= 0) {
                        return conn.execute("CREATE DATABASE \"" + name + "\"");
                    } else {
                        return CompletableFuture.completedFuture(null);
                    }
                })
                .thenApply(r -> null);
    }

    private CompletableFuture<Void> initLightfurTables()
    {
        JsonObject initData = new JsonObject();

        CompletableFuture<Void> f = new CompletableFuture<>();

        conn.beginTransaction()
                .thenCompose(r -> conn.execute("CREATE SCHEMA IF NOT EXISTS lightfur"))
                .thenCompose(r -> conn.execute("CREATE TABLE IF NOT EXISTS lightfur.version_data (data JSONB)"))
                .thenCompose(r -> {
                    String sql = "INSERT INTO lightfur.version_data (data)\n" +
                            "SELECT ?::jsonb\n" +
                            "WHERE NOT EXISTS (SELECT 1 FROM lightfur.version_data)";

                    return conn.query(sql, initData.toString());
                })
                .thenCompose(r -> conn.commit(true))
                .thenAccept(r -> f.complete(null))
                .exceptionally(ex -> {
                    conn.rollback(true)
                            .thenAccept(r -> f.completeExceptionally(ex));

                    return null;
                });

        return f;
    }

    private CompletableFuture<JsonObject> getCurrentDatabaseVersionData()
    {
        return conn.query("SELECT COALESCE(data::text, '{}') FROM lightfur.version_data LIMIT 1")
                .thenApply(r -> {
                    String s = r.getResults().get(0).getString(0);
                    return new JsonObject(s);
                });
    }

    private List<DbVersionUpdateInfo> collectUpdateFiles(String databaseName, DbUpdateFileCollector collector)
    {
        List<DbVersionUpdateInfo> files = new ArrayList<>();
        String startMagic = " LIGHTFUR {";
        String endMagic = "} END";

        FileMatchProcessorWithContext handler = (absolutePath, relativePathStr, inputStream, lengthBytes) -> {
            char[] header = new char[1024];

            try (InputStream stream = new BOMInputStream(inputStream,
                    ByteOrderMark.UTF_8, ByteOrderMark.UTF_16BE, ByteOrderMark.UTF_16LE, ByteOrderMark.UTF_32BE,
                    ByteOrderMark.UTF_32LE);
                 InputStreamReader reader = new InputStreamReader(stream)) {
                int headerReadLength = reader.read(header, 0, 1024);

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
                info.setPath(absolutePath.toPath().resolve(relativePathStr));

                if ((!info.getDatabase().equals(databaseName)) && (!info.getDatabase().equals("$"))) {
                    return;
                }

                if (info.getDatabase().equals("$")) {
                    info.setDatabase(databaseName);
                }

                if (headerReadLength >= 1024) {
                    info.setUpdateContent(new String(header) + IOUtils.toString(reader));
                } else {
                    info.setUpdateContent(new String(Arrays.copyOfRange(header, 0, headerReadLength)));
                }

                files.add(info);
            }
        };

        if (collector == null) {
            scanner.matchFilenameExtension("sql", handler)
                    .scan();
        } else {
            collector.collect("sql", handler);
        }

        return files;
    }

    private CompletableFuture<Void> doUpdate(DbVersionUpdateInfo info)
    {
        CompletableFuture<Void> f = new CompletableFuture<>();
        String data = info.getUpdateContent();

        log.info(info.getId() + ": Updating database " + info.getDatabase() + " to version " +
                info.getVersion() + "...");

        conn.beginTransaction()
                .thenCompose(r -> conn.executeWithoutPreparing(data))
                .thenCompose(r -> {
                    if (!versionData.containsKey(info.getId())) {
                        versionData.put(info.getId(), new JsonObject());
                    }

                    versionData.getJsonObject(info.getId()).put("version", info.getVersion());

                    return conn.execute("UPDATE lightfur.version_data SET data = ?::jsonb", versionData.toString());
                })
                .thenCompose(r -> conn.commit(true))
                .thenAccept(r -> {
                    log.info(info.getId() + ": Updated database " + info.getDatabase() + " to version " +
                            info.getVersion() + " with script " + info.getPath());
                    f.complete(null);
                })
                .exceptionally(ex -> {
                    conn.rollback(true)
                            .thenAccept(r -> f.completeExceptionally(ex));

                    return null;
                });

        return f;
    }
}
