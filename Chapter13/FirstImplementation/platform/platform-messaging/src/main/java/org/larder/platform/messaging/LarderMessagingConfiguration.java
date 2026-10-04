package org.larder.platform.messaging;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * All messages are JSON ({@code defaultContentType: application/json} in every AsyncAPI).
 * Listeners always read into the type of their own method parameter: messages carry the
 * contract's message name, never a Java class name of the publisher.
 */
@AutoConfiguration
public class LarderMessagingConfiguration {

    @Bean
    MessageConverter larderMessageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        converter.setAlwaysConvertToInferredType(true);
        return converter;
    }
}
