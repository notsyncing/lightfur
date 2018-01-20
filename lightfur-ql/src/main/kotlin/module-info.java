module lightfur.ql {
    requires kotlin.stdlib;
    requires lightfur.entity;
    requires fastjson;
    requires kotlinx.coroutines.jdk8;
    requires lightfur.core;
    requires java.naming;

    exports io.github.notsyncing.lightfur.ql;
    exports io.github.notsyncing.lightfur.ql.permission;
}