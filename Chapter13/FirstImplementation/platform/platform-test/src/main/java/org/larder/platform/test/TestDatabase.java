package org.larder.platform.test;

import java.sql.DriverManager;
import java.sql.SQLException;

import org.larder.platform.persistence.BoundedContextDatabase;
import org.larder.platform.persistence.BoundedContextDatabaseProperties;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * One PostgreSQL container per test JVM. Each Bounded Context gets its schema and its
 * own user exactly as in {@code infra/postgres} (ADR0002), and its real Flyway migrations run.
 */
public final class TestDatabase {

    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17");

    private TestDatabase() {
    }

    /** Fresh schema for {@code schema}: dropped, recreated, migrated. */
    public static synchronized BoundedContextDatabase forSchema(String schema) {
        if (!POSTGRES.isRunning()) {
            POSTGRES.start();
        }
        try (var connection = DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
             var statement = connection.createStatement()) {
            statement.execute("DROP SCHEMA IF EXISTS " + schema + " CASCADE");
            statement.execute("DO $$ BEGIN CREATE USER " + schema + " WITH PASSWORD '" + schema
                    + "'; EXCEPTION WHEN duplicate_object THEN NULL; END $$");
            statement.execute("CREATE SCHEMA " + schema + " AUTHORIZATION " + schema);
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot prepare schema " + schema, e);
        }
        return BoundedContextDatabase.create(
                new BoundedContextDatabaseProperties(POSTGRES.getJdbcUrl(), schema, schema, schema, 2));
    }
}
