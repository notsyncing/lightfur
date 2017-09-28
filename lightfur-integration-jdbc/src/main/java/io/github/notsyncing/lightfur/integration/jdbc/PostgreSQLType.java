package io.github.notsyncing.lightfur.integration.jdbc;

import java.sql.SQLType;

public enum PostgreSQLType implements SQLType {
    JSONB(30000);

    private Integer type;

    PostgreSQLType(final Integer type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public String getVendor() {
        return "io.github.notsyncing.lightfur.integration.jdbc";
    }

    @Override
    public Integer getVendorTypeNumber() {
        return type;
    }
}
