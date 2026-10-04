package org.larder.platform.web;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Web conventions for all REST adapters generated from the contracts. */
@AutoConfiguration
@Import(ApiErrorAdvice.class)
public class LarderWebConfiguration implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(new ContractEnumConverterFactory());
    }
}
