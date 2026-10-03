package org.larder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * Larder as one deployable. Every Bounded Context brings its own database access
 * (own schema, own user), so the single shared DataSource and Flyway of Spring Boot
 * are switched off.
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, FlywayAutoConfiguration.class})
public class LarderApplication {

    public static void main(String[] args) {
        SpringApplication.run(LarderApplication.class, args);
    }
}
