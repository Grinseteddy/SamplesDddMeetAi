package org.larder.sharing.adapter.out.consentmanagement;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.larder.platform.security.BearerTokenRelay;
import org.larder.platform.web.UpstreamClients;
import org.larder.sharing.adapter.out.consentmanagement.client.ApiClient;
import org.larder.sharing.adapter.out.consentmanagement.client.api.ConsentsApi;
import org.larder.sharing.adapter.out.consentmanagement.client.model.Consent;
import org.larder.sharing.application.ConsentPurpose;
import org.larder.sharing.application.Consents;
import org.larder.sharing.application.NotPermittedException;
import org.larder.sharing.application.UpstreamUnavailableException;
import org.larder.sharing.domain.CookId;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Anticorruption layer to Consent Management: reads {@code GET /consents?subject={cookId}} through the
 * client generated from {@code contracts/openapi/consent-management.openapi.yaml} with the calling cook's
 * own token. A purpose is in force when the cook has a consent to the purpose's consent text without
 * {@code revokedAt}. Which consent text stands for which purpose is configuration
 * ({@link ConsentManagementClientConfiguration}).
 */
class ConsentManagementConsents implements Consents {

    /** Version of the Consent Management contract this client is generated from. */
    static final String VERSION = "1.0.0";

    private final ConsentsApi consents;
    private final Map<ConsentPurpose, UUID> consentTexts;

    ConsentManagementConsents(ConsentsApi consents, Map<ConsentPurpose, UUID> consentTexts) {
        this.consents = consents;
        this.consentTexts = new EnumMap<>(consentTexts);
        for (ConsentPurpose purpose : ConsentPurpose.values()) {
            if (this.consentTexts.get(purpose) == null) {
                throw new IllegalArgumentException("No consent text configured for " + purpose);
            }
        }
    }

    /** A client for Consent Management at {@code baseUrl} that relays the caller's bearer token. */
    static ConsentManagementConsents create(RestClient.Builder restClientBuilder, String baseUrl,
                                            Map<ConsentPurpose, UUID> consentTexts) {
        var apiClient = new ApiClient(UpstreamClients.restClient(restClientBuilder, baseUrl, new BearerTokenRelay()));
        // The generated client builds absolute URLs from its own base path (the contract's server URL).
        apiClient.setBasePath(baseUrl);
        return new ConsentManagementConsents(new ConsentsApi(apiClient), consentTexts);
    }

    @Override
    public boolean isInForce(CookId cook, ConsentPurpose purpose) {
        UUID consentText = consentTexts.get(purpose);
        List<Consent> given;
        try {
            given = consents.getConsents(cook.value(), VERSION);
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (HttpClientErrorException.Forbidden e) {
            throw new NotPermittedException("Consent Management does not show the consents of cook " + cook.value());
        } catch (RestClientException e) {
            throw new UpstreamUnavailableException("Consent Management cannot deliver the consents of cook "
                    + cook.value() + ": " + e.getMessage(), e);
        }
        return given != null && given.stream().anyMatch(consent -> cook.value().equals(consent.getSubject())
                && consent.getConsentText() != null
                && consentText.equals(consent.getConsentText().getConsentTextId())
                && consent.getRevokedAt() == null);
    }
}
