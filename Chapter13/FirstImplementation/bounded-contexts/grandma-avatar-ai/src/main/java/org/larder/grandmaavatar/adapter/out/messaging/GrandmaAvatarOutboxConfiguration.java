package org.larder.grandmaavatar.adapter.out.messaging;

import java.time.Duration;

import org.larder.platform.messaging.Outbox;
import org.larder.platform.messaging.OutboxRelay;
import org.larder.platform.persistence.BoundedContextDatabase;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.fasterxml.jackson.databind.ObjectMapper;

/** Transactional outbox of Grandma Avatar in its own schema, and the relay publishing it. */
@Configuration(proxyBeanMethods = false)
class GrandmaAvatarOutboxConfiguration {

    static final String SOURCE = "grandma-avatar";

    @Bean
    Outbox grandmaavatarOutbox(BoundedContextDatabase grandmaavatarDatabase, ObjectMapper objectMapper) {
        return new Outbox(grandmaavatarDatabase.jdbcClient(), objectMapper, SOURCE);
    }

    @Bean
    OutboxRelay grandmaavatarOutboxRelay(BoundedContextDatabase grandmaavatarDatabase, RabbitTemplate rabbitTemplate,
                                         PlatformTransactionManager grandmaavatarTransactionManager) {
        return new OutboxRelay(SOURCE, grandmaavatarDatabase.jdbcClient(), rabbitTemplate,
                grandmaavatarTransactionManager, Duration.ofMillis(200));
    }

    @Bean
    OutboxHelpPublisher grandmaavatarHelpPublisher(Outbox grandmaavatarOutbox) {
        return new OutboxHelpPublisher(grandmaavatarOutbox);
    }
}
