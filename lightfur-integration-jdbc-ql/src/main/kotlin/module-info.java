module lightfur.integration.jdbc.ql {
    requires lightfur.core;
    requires java.sql;
    requires lightfur.entity;
    requires lightfur.integration.jdbc;
    requires kotlin.stdlib;
    requires kotlinx.coroutines.jdk8;
    requires lightfur.ql;

    exports io.github.notsyncing.lightfur.integration.jdbc.ql;
}