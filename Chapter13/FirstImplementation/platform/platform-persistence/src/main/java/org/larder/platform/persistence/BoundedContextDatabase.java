package org.larder.platform.persistence;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.simple.JdbcClient;

import com.zaxxer.hikari.HikariDataSource;

/**
 * Database access of one Bounded Context (ADR0002): one schema, one database user,
 * migrations under {@code classpath:db/migration/<schema>}.
 */
public final class BoundedContextDatabase {

    private final DataSource dataSource;
    private final JdbcClient jdbcClient;

    private BoundedContextDatabase(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcClient = JdbcClient.create(dataSource);
    }

    /** Binds {@code larder.<boundedContext>.database.*} and migrates the schema. */
    public static BoundedContextDatabase fromEnvironment(Environment environment, String boundedContext) {
        var properties = Binder.get(environment)
                .bind("larder." + boundedContext + ".database", BoundedContextDatabaseProperties.class)
                .orElseThrow(() -> new IllegalStateException(
                        "Missing larder." + boundedContext + ".database.* configuration"));
        return create(properties);
    }

    public static BoundedContextDatabase create(BoundedContextDatabaseProperties properties) {
        HikariDataSource dataSource = DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .url(properties.url())
                .username(properties.username())
                .password(properties.password())
                .build();
        dataSource.setSchema(properties.schema());
        dataSource.setPoolName(properties.schema());
        dataSource.setMaximumPoolSize(properties.poolSize());
        dataSource.setMinimumIdle(1);

        Flyway.configure()
                .dataSource(dataSource)
                .schemas(properties.schema())
                .defaultSchema(properties.schema())
                .createSchemas(false)
                .locations("classpath:db/migration/" + properties.schema())
                .load()
                .migrate();

        return new BoundedContextDatabase(dataSource);
    }

    public DataSource dataSource() {
        return dataSource;
    }

    public JdbcClient jdbcClient() {
        return jdbcClient;
    }
}
