package org.larder.sharing.adapter.out.consentmanagement;

import java.util.Map;
import java.util.UUID;

import org.larder.platform.web.UpstreamClients;
import org.larder.sharing.application.ConsentPurpose;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestClient;

/**
 * Consent Management as seen from Sharing; base URL from
 * {@code larder.sharing.upstream.consent-management.base-url}. The consent texts Sharing checks:
 * <ul>
 *   <li>{@code larder.sharing.consents.mention-as-helper} - default {@value #MENTION_AS_HELPER},
 *       "I allow other cooks to mention me as helper in their thanks."</li>
 *   <li>{@code larder.sharing.consents.photos-in-public-thanks} - default {@value #PHOTOS_IN_PUBLIC_THANKS},
 *       "I allow to use my photos in public thanks and recipes."</li>
 * </ul>
 * Both are seeded by Consent Management's migration {@code V3__consent_texts.sql}.
 */
@Configuration(proxyBeanMethods = false)
class ConsentManagementClientConfiguration {

    static final String MENTION_AS_HELPER = "3c6e2b7a-9d41-4f0b-8e5a-2b7c9d1e4f60";
    static final String PHOTOS_IN_PUBLIC_THANKS = "5f8d8a1c-515d-4eae-a6b1-0a0313edfc31";

    @Bean
    ConsentManagementConsents sharingConsents(RestClient.Builder restClientBuilder, Environment environment) {
        return ConsentManagementConsents.create(restClientBuilder,
                UpstreamClients.baseUrl(environment, "sharing", "consent-management"),
                Map.of(ConsentPurpose.MENTION_AS_HELPER,
                        consentText(environment, "mention-as-helper", MENTION_AS_HELPER),
                        ConsentPurpose.PHOTOS_IN_PUBLIC_THANKS,
                        consentText(environment, "photos-in-public-thanks", PHOTOS_IN_PUBLIC_THANKS)));
    }

    private static UUID consentText(Environment environment, String name, String defaultId) {
        return UUID.fromString(environment.getProperty("larder.sharing.consents." + name, defaultId).trim());
    }
}
