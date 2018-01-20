module lightfur.core {
    requires fastjson;
    requires java.logging;
    requires java.sql;
    requires fast.classpath.scanner;
    requires commons.io;

    exports io.github.notsyncing.lightfur.core;
    exports io.github.notsyncing.lightfur.core.annotations.entity;
    exports io.github.notsyncing.lightfur.core.common;
    exports io.github.notsyncing.lightfur.core.entity;
    exports io.github.notsyncing.lightfur.core.models;
    exports io.github.notsyncing.lightfur.core.read;
    exports io.github.notsyncing.lightfur.core.sql;
    exports io.github.notsyncing.lightfur.core.sql.base;
    exports io.github.notsyncing.lightfur.core.sql.builders;
    exports io.github.notsyncing.lightfur.core.sql.models;
    exports io.github.notsyncing.lightfur.core.utils;
    exports io.github.notsyncing.lightfur.core.versioning;
}