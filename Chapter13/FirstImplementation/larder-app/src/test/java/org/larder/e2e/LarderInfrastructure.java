package org.larder.e2e;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

/**
 * The infrastructure of {@code infra/docker-compose.yml} as Testcontainers - minus Keycloak, whose
 * tokens {@link AccessTokens} mints itself:
 * <ul>
 *   <li>PostgreSQL 17 initialised with the real {@code infra/postgres/01-schemas-and-users.sql}
 *       (one schema and one user per Bounded Context, ADR0002);</li>
 *   <li>RabbitMQ 4 (ADR0003);</li>
 *   <li>Adobe S3Mock as Media's bucket.</li>
 * </ul>
 * The containers start once per JVM; Ryuk removes them afterwards.
 */
final class LarderInfrastructure {

    /** Every Bounded Context with its own {@code larder.<context>.database.*} in application.yml. */
    static final List<String> BOUNDED_CONTEXTS = List.of("recipe-catalog", "meal-planning", "meal-preparation",
            "cooking-assistance", "grandma-avatar-ai", "notification", "media", "sharing", "cook-profile",
            "consent-management");

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("larder")
            .withUsername("larder_admin")
            .withPassword("larder_admin")
            .withCopyFileToContainer(MountableFile.forHostPath(projectFile("infra/postgres/01-schemas-and-users.sql")),
                    "/docker-entrypoint-initdb.d/01-schemas-and-users.sql");

    static final RabbitMQContainer RABBITMQ = new RabbitMQContainer("rabbitmq:4-management");

    static final GenericContainer<?> S3 = new GenericContainer<>("adobe/s3mock:5.2.3")
            .withExposedPorts(9090)
            .waitingFor(Wait.forListeningPort());

    /** The application's HTTP port: the contexts call each other at http://localhost:${server.port}/<context>. */
    static final int APP_PORT = freePort();

    private LarderInfrastructure() {
    }

    static synchronized void start() {
        if (!POSTGRES.isRunning()) {
            POSTGRES.start();
            RABBITMQ.start();
            S3.start();
        }
    }

    /** Points the whole application at the containers and at its own free port. */
    static void register(DynamicPropertyRegistry registry) {
        start();
        registry.add("server.port", () -> APP_PORT);
        registry.add("LARDER_DB_URL", POSTGRES::getJdbcUrl);
        BOUNDED_CONTEXTS.forEach(context ->
                registry.add("larder." + context + ".database.url", POSTGRES::getJdbcUrl));
        registry.add("spring.rabbitmq.host", RABBITMQ::getHost);
        registry.add("spring.rabbitmq.port", RABBITMQ::getAmqpPort);
        registry.add("spring.rabbitmq.username", RABBITMQ::getAdminUsername);
        registry.add("spring.rabbitmq.password", RABBITMQ::getAdminPassword);
        registry.add("larder.media.storage.endpoint", () -> "http://" + S3.getHost() + ":" + S3.getMappedPort(9090));
    }

    /** Messages waiting in all {@code *.dlq} queues of the broker. */
    static int deadLetteredMessages() {
        try {
            var result = RABBITMQ.execInContainer("rabbitmqctl", "list_queues", "name", "messages", "-q", "--no-table-headers");
            return result.getStdout().lines()
                    .map(line -> line.trim().split("\\s+"))
                    .filter(columns -> columns.length == 2 && columns[0].endsWith(".dlq"))
                    .mapToInt(columns -> Integer.parseInt(columns[1]))
                    .sum();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot list the queues of RabbitMQ", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    /** Names of the {@code *.dlq} queues - proves the topology was declared at all. */
    static List<String> deadLetterQueues() {
        try {
            var result = RABBITMQ.execInContainer("rabbitmqctl", "list_queues", "name", "-q", "--no-table-headers");
            return result.getStdout().lines().map(String::trim).filter(name -> name.endsWith(".dlq")).sorted().toList();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot list the queues of RabbitMQ", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private static int freePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            int port = socket.getLocalPort();
            if (port == 8080) {
                throw new IllegalStateException("8080 belongs to the manually started app");
            }
            return port;
        } catch (IOException e) {
            throw new IllegalStateException("No free port", e);
        }
    }

    private static Path projectFile(String relative) {
        Path dir = Path.of("").toAbsolutePath();
        while (dir != null) {
            Path candidate = dir.resolve(relative);
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
            dir = dir.getParent();
        }
        throw new IllegalStateException(relative + " not found above " + Path.of("").toAbsolutePath());
    }
}
