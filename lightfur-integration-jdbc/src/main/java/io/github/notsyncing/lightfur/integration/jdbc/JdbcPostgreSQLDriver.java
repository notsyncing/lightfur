package io.github.notsyncing.lightfur.integration.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.notsyncing.lightfur.DatabaseDriver;
import io.github.notsyncing.lightfur.common.LightfurConfig;
import org.postgresql.Driver;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class JdbcPostgreSQLDriver extends DatabaseDriver<Connection> {
    private HikariDataSource db;

    @Override
    public void init(LightfurConfig config) {
        try {
            Class.forName(Driver.class.getName());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get PostgreSQL JDBC driver class", e);
        }

        HikariConfig hc = new HikariConfig();
        hc.setJdbcUrl("jdbc:postgresql://" + config.getHost() + ":" + config.getPort() + "/" + config.getDatabase());
        hc.setUsername(config.getUsername());
        hc.setPassword(config.getPassword());
        hc.setMaximumPoolSize(config.getMaxPoolSize());

        db = new HikariDataSource(hc);
    }

    @Override
    public CompletableFuture<Void> close() {
        return CompletableFuture.runAsync(() -> db.close());
    }

    @Override
    public CompletableFuture<Void> recreate(LightfurConfig config) {
        return CompletableFuture.runAsync(() -> {
            db.close();

            init(config);
        });
    }

    @Override
    public CompletableFuture<Connection> getConnection() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return db.getConnection();
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> createDatabase(String name) {
        return CompletableFuture.runAsync(() -> {
            try {
                try (Connection c = db.getConnection();
                     Statement s = c.createStatement()) {
                    s.execute("CREATE DATABASE \"" + name + "\"");
                }
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> dropDatabase(String name, boolean ifExists) {
        String sql = "DROP DATABASE";

        if (ifExists) {
            sql += " IF EXISTS";
        }

        sql += " \"" + name + "\"";

        final String finalSql = sql;

        return CompletableFuture.runAsync(() -> {
            try {
                try (Connection c = db.getConnection();
                     Statement s = c.createStatement()) {
                    s.execute(finalSql);
                }
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });
    }
}
