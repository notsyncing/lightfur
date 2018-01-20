module lightfur.integration.vertx.ql {
    requires vertx.core;
    requires fastjson;
    requires vertx.sql.common;
    requires lightfur.entity;
    requires kotlin.stdlib;
    requires lightfur.ql;

    exports io.github.notsyncing.lightfur.integration.vertx.ql;
}