module lightfur.entity {
    requires lightfur.core;
    requires fastjson;
    requires kotlin.stdlib;
    requires kotlinx.coroutines.jdk8;
    requires kotlinx.coroutines.core;

    exports io.github.notsyncing.lightfur.entity;
    exports io.github.notsyncing.lightfur.entity.dsl;
    exports io.github.notsyncing.lightfur.entity.events;
    exports io.github.notsyncing.lightfur.entity.exceptions;
    exports io.github.notsyncing.lightfur.entity.functions;
    exports io.github.notsyncing.lightfur.entity.read;
    exports io.github.notsyncing.lightfur.entity.utils;
}