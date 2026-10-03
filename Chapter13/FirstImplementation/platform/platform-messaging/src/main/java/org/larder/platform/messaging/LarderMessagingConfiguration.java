package org.larder.platform.messaging;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.ObjectMapper;

/** All messages are JSON ({@code defaultContentType: application/json} in every AsyncAPI). */
@AutoConfiguration
public class LarderMessagingConfiguration {

    @Bean
    MessageConverter larderMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
