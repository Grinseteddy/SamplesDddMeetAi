package org.larder.consentmanagement.adapter.out.persistence;

import org.larder.consentmanagement.application.ConsentService;
import org.larder.platform.persistence.BoundedContextDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/** Own schema {@code consentmanagement} with its own database user (ADR0002). */
@Configuration(proxyBeanMethods = false)
class ConsentManagementPersistenceConfiguration {

    @Bean
    BoundedContextDatabase consentmanagementDatabase(Environment environment) {
        return BoundedContextDatabase.fromEnvironment(environment, "consent-management");
    }

    @Bean(ConsentService.TRANSACTIONS)
    PlatformTransactionManager consentmanagementTransactionManager(BoundedContextDatabase consentmanagementDatabase) {
        return new DataSourceTransactionManager(consentmanagementDatabase.dataSource());
    }

    @Bean
    JdbcConsentRepository consentmanagementConsentRepository(BoundedContextDatabase consentmanagementDatabase) {
        return new JdbcConsentRepository(consentmanagementDatabase.jdbcClient());
    }

    @Bean
    JdbcConsentTexts consentmanagementConsentTexts(BoundedContextDatabase consentmanagementDatabase) {
        return new JdbcConsentTexts(consentmanagementDatabase.jdbcClient());
    }
}
