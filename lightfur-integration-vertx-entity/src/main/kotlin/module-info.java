module lightfur.integration.vertx.entity {
    requires vertx.core;
    requires lightfur.entity;
    requires lightfur.integration.vertx;
    requires kotlin.stdlib;
    requires vertx.sql.common;
    requires lightfur.core;
    requires kotlinx.coroutines.jdk8;
    requires fastjson;

    exports io.github.notsyncing.lightfur.integration.vertx.entity;
}