module lightfur.integration.jdbc.entity {
    requires java.sql;
    requires lightfur.entity;
    requires lightfur.core;
    requires kotlinx.coroutines.jdk8;
    requires kotlin.stdlib;
    requires fastjson;
    requires lightfur.integration.jdbc;

    exports io.github.notsyncing.lightfur.integration.jdbc.entity;
}