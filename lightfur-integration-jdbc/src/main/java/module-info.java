module lightfur.integration.jdbc {
    requires java.sql;
    requires fastjson;
    requires postgresql;
    requires lightfur.core;
    requires HikariCP;

    exports io.github.notsyncing.lightfur.integration.jdbc;
}