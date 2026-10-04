package org.larder.cookingassistance.adapter.out.messaging;

import java.time.Duration;

import org.larder.cookingassistance.application.HelpRequestService;
import org.larder.platform.messaging.Outbox;
import org.larder.platform.messaging.OutboxRelay;
import org.larder.platform.persistence.BoundedContextDatabase;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.fasterxml.jackson.databind.ObjectMapper;

/** Outbox of this context (table {@code outbox} in schema {@code cookingassistance}) and its relay. */
@Configuration(proxyBeanMethods = false)
class CookingAssistanceMessagingConfiguration {

    static final String SOURCE = "cooking-assistance";

    @Bean
    Outbox cookingassistanceOutbox(BoundedContextDatabase cookingassistanceDatabase, ObjectMapper objectMapper) {
        return new Outbox(cookingassistanceDatabase.jdbcClient(), objectMapper, SOURCE);
    }

    @Bean
    OutboxHelpEvents cookingassistanceHelpEvents(Outbox cookingassistanceOutbox) {
        return new OutboxHelpEvents(cookingassistanceOutbox);
    }

    @Bean
    OutboxRelay cookingassistanceOutboxRelay(BoundedContextDatabase cookingassistanceDatabase, RabbitTemplate rabbitTemplate,
            @Qualifier(HelpRequestService.TRANSACTIONS) PlatformTransactionManager transactionManager) {
        return new OutboxRelay(SOURCE, cookingassistanceDatabase.jdbcClient(), rabbitTemplate, transactionManager,
                Duration.ofMillis(200));
    }
}
