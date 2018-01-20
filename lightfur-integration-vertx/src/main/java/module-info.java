module lightfur.integration.vertx {
    requires vertx.core;
    requires vertx.sql.common;
    requires fastjson;
    requires scala.library;
    requires lightfur.core;
    requires vertx.mysql.postgresql.client;
    requires java.logging;

    exports io.github.notsyncing.lightfur.integration.vertx;
}