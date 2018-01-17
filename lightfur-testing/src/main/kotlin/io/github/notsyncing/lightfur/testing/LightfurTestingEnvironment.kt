package io.github.notsyncing.lightfur.testing

import io.github.notsyncing.lightfur.DataSession
import io.github.notsyncing.lightfur.DatabaseManager
import io.github.notsyncing.lightfur.common.LightfurConfigBuilder
import io.github.notsyncing.lightfur.entity.EntityGlobal
import io.github.notsyncing.lightfur.integration.jdbc.JdbcDataSession
import io.github.notsyncing.lightfur.integration.jdbc.JdbcPostgreSQLDriver
import io.github.notsyncing.lightfur.integration.jdbc.entity.JdbcEntityDataMapper
import io.github.notsyncing.lightfur.integration.jdbc.entity.JdbcEntityQueryExecutor
import io.github.notsyncing.lightfur.integration.jdbc.ql.JdbcRawQueryProcessor
import io.github.notsyncing.lightfur.ql.QueryExecutor

object LightfurTestingEnvironment {
    private var currentDatabaseName = "lightfur_test_${Math.abs(this.hashCode())}"

    init {
        DatabaseManager.setDriver(JdbcPostgreSQLDriver())
        DataSession.setCreator { JdbcDataSession(JdbcEntityDataMapper(), it) as DataSession<Any, Any, Any> }
        EntityGlobal.setQueryExecutor(JdbcEntityQueryExecutor())
        QueryExecutor.setRawQueryProcessor { JdbcRawQueryProcessor() }
    }

    fun create() {
        val conf = LightfurConfigBuilder()
                .database(currentDatabaseName)
                .databaseVersioning(true)
                .host("localhost")
                .port(5432)
                .maxPoolSize(10)
                .username("postgres")
                .password("")
                .build()

        val db = DatabaseManager.getInstance()
        db.init(conf).get()

        val conn = JdbcDataSession()

        try {
            conn.updateWithoutPreparing("""
CREATE OR REPLACE FUNCTION truncate_tables() RETURNS void AS \$\$
DECLARE
    statements CURSOR FOR
        SELECT tablename FROM pg_tables
        WHERE schemaname NOT IN ('pg_catalog', 'information_schema');
BEGIN
    FOR stmt IN statements LOOP
        EXECUTE 'TRUNCATE TABLE ' || quote_ident(stmt.tablename) || ' RESTART IDENTITY CASCADE;';
    END LOOP;
END;
\$\$ LANGUAGE plpgsql;
        """).get()
        } finally {
            conn.end().get()
        }
    }

    fun prepare() {
        val conn = JdbcDataSession()

        try {
            conn.updateWithoutPreparing("SELECT truncate_tables();").get()
        } finally {
            conn.end().get()
        }
    }

    fun destroy() {
        val db = DatabaseManager.getInstance()
        db.setDatabase("postgres").get()
        db.dropDatabase(currentDatabaseName).get()
        db.close().get()
    }
}