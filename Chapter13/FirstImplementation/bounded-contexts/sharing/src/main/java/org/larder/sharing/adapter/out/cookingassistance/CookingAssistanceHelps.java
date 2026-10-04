package org.larder.sharing.adapter.out.cookingassistance;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.larder.platform.security.BearerTokenRelay;
import org.larder.platform.web.UpstreamClients;
import org.larder.sharing.adapter.out.cookingassistance.client.ApiClient;
import org.larder.sharing.adapter.out.cookingassistance.client.api.HelpsApi;
import org.larder.sharing.application.Helps;
import org.larder.sharing.application.NotPermittedException;
import org.larder.sharing.application.UpstreamUnavailableException;
import org.larder.sharing.domain.CookId;
import org.larder.sharing.domain.Help;
import org.larder.sharing.domain.HelpId;
import org.larder.sharing.domain.HelperKind;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Anticorruption layer to Cooking Assistance: calls {@code GET /helps/{helpId}} through the client
 * generated from {@code contracts/openapi/cooking-assistance.openapi.yaml} with the calling cook's own
 * token, and keeps only who asked for the help and who gave it. Nothing of the generated model leaves
 * this package.
 */
class CookingAssistanceHelps implements Helps {

    /** Version of the Cooking Assistance contract this client is generated from. */
    static final String VERSION = "1.0.0";

    private final HelpsApi helps;

    CookingAssistanceHelps(HelpsApi helps) {
        this.helps = helps;
    }

    /** A client for Cooking Assistance at {@code baseUrl} that relays the caller's bearer token. */
    static CookingAssistanceHelps create(RestClient.Builder restClientBuilder, String baseUrl) {
        var apiClient = new ApiClient(UpstreamClients.restClient(withoutAnswers(restClientBuilder), baseUrl,
                new BearerTokenRelay()));
        // The generated client builds absolute URLs from its own base path (the contract's server URL).
        apiClient.setBasePath(baseUrl);
        return new CookingAssistanceHelps(new HelpsApi(apiClient));
    }

    /**
     * Sharing does not need the content of a help, and the generated client cannot read it: the answer kinds
     * of the contract's {@code oneOf} are generated as classes that do not extend {@code Answer}, so Jackson
     * fails on every help with an answer. The answer is therefore skipped when reading.
     */
    private static RestClient.Builder withoutAnswers(RestClient.Builder restClientBuilder) {
        return restClientBuilder.clone().messageConverters(converters -> converters.replaceAll(converter ->
                converter instanceof MappingJackson2HttpMessageConverter jackson
                        ? new MappingJackson2HttpMessageConverter(jackson.getObjectMapper().copy()
                                .addMixIn(org.larder.sharing.adapter.out.cookingassistance.client.model.Help.class,
                                        WithoutAnswer.class))
                        : converter));
    }

    @JsonIgnoreProperties("answer")
    private abstract static class WithoutAnswer {
    }

    @Override
    public Optional<Help> find(HelpId id) {
        org.larder.sharing.adapter.out.cookingassistance.client.model.Help help;
        try {
            help = helps.getHelpById(id.value(), VERSION);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (HttpClientErrorException.Forbidden e) {
            throw new NotPermittedException("Cooking Assistance does not show help " + id.value() + " to the cook");
        } catch (RestClientException e) {
            throw new UpstreamUnavailableException("Cooking Assistance cannot deliver help " + id.value() + ": "
                    + e.getMessage(), e);
        }
        return Optional.of(translate(id, help));
    }

    private static Help translate(HelpId id, org.larder.sharing.adapter.out.cookingassistance.client.model.Help help) {
        try {
            HelperKind kind = switch (help.getHelpProviderType()) {
                case COMMUNITY -> HelperKind.COMMUNITY_COOK;
                case CHEF -> HelperKind.CHEF;
                case GRANDMA_AVATAR -> HelperKind.GRANDMA_AVATAR;
            };
            return new Help(id, new CookId(help.getHelpRequester()), kind,
                    Optional.ofNullable(help.getHelpProvider()).map(CookId::new));
        } catch (RuntimeException e) {
            throw new UpstreamUnavailableException("Cooking Assistance delivered an unusable help " + id.value() + ": "
                    + e.getMessage(), e);
        }
    }
}
