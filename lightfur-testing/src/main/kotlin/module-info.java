module lightfur.testing {
    requires lightfur.core;
    requires lightfur.entity;
    requires lightfur.ql;
    requires lightfur.integration.jdbc;
    requires lightfur.integration.jdbc.entity;
    requires lightfur.integration.jdbc.ql;
    exports io.github.notsyncing.lightfur.testing;
}