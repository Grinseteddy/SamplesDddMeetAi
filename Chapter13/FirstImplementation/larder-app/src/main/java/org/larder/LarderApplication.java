package org.larder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;

/**
 * Larder as one deployable. Every Bounded Context brings its own database access
 * (own schema, own user), so the single shared DataSource and Flyway of Spring Boot
 * are switched off.
 * <p>
 * Scanned beans are named by their fully qualified class name: Bounded Contexts are developed
 * independently and may well use the same simple class names (e.g. RecipeCatalogClientConfiguration).
 */
@SpringBootApplication(
        exclude = {DataSourceAutoConfiguration.class, FlywayAutoConfiguration.class},
        nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class)
public class LarderApplication {

    public static void main(String[] args) {
        SpringApplication.run(LarderApplication.class, args);
    }
}
