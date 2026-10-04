package org.larder.consentmanagement.adapter.in.web;

import static org.hamcrest.Matchers.endsWith;
import static org.larder.consentmanagement.TestData.COOK;
import static org.larder.consentmanagement.TestData.NOW;
import static org.larder.consentmanagement.TestData.PHOTOS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.larder.consentmanagement.application.ConsentService;
import org.larder.consentmanagement.application.NotFoundException;
import org.larder.consentmanagement.application.NotPermittedException;
import org.larder.consentmanagement.domain.Consent;
import org.larder.consentmanagement.domain.ConsentId;
import org.larder.platform.security.LarderSecurityConfiguration;
import org.larder.platform.test.TestTokens;
import org.larder.platform.web.LarderWebConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest({ConsentsController.class, ConsentTextsController.class, ConsentManagementErrorAdvice.class})
@Import({LarderSecurityConfiguration.class, LarderWebConfiguration.class})
class ConsentsControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ConsentService service;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private static RequestPostProcessor cook(String... scopes) {
        return TestTokens.cook(COOK.value(), scopes);
    }

    @Test
    void listsTheConsentsOfASubjectInTheContractShape() throws Exception {
        Consent consent = Consent.give(COOK, PHOTOS, NOW);
        given(service.consentsOf(COOK)).willReturn(List.of(consent));

        mvc.perform(get("/consent-management/consents").param("subject", COOK.value().toString())
                        .header("version", "1.0.0").with(cook("consent:read")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].consentId").value(consent.id().value().toString()))
                .andExpect(jsonPath("$[0].consentText.text").value(PHOTOS.text()))
                .andExpect(jsonPath("$[0].givenAt").value("2026-09-21T10:34:00Z"));
    }

    @Test
    void givesAConsentForTheCallingCookAndLinksToIt() throws Exception {
        Consent consent = Consent.give(COOK, PHOTOS, NOW);
        given(service.give(any(), any(), any())).willReturn(consent);

        mvc.perform(post("/consent-management/consents").header("version", "1.0.0").with(cook("consent:write"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"subject":"%s","consentTextId":"%s"}""".formatted(COOK.value(), PHOTOS.id().value())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/consent-management/consents/" + consent.id().value())))
                .andExpect(jsonPath("$.consentLink").value(endsWith(consent.id().value().toString())));
    }

    @Test
    void writingNeedsTheWriteScope() throws Exception {
        mvc.perform(post("/consent-management/consents").header("version", "1.0.0").with(cook("consent:read"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"subject":"%s","consentTextId":"%s"}""".formatted(COOK.value(), PHOTOS.id().value())))
                .andExpect(status().isForbidden());
    }

    @Test
    void readingNeedsAToken() throws Exception {
        mvc.perform(get("/consent-management/consents/consent-texts").header("version", "1.0.0"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void malformedRequestsAreBadRequests() throws Exception {
        mvc.perform(post("/consent-management/consents").header("version", "1.0.0").with(cook("consent:write"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"subject\":\"%s\"}".formatted(COOK.value())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
        mvc.perform(get("/consent-management/consents").header("version", "1.0.0").with(cook("consent:read")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void revokingSomebodyElsesConsentIsForbidden() throws Exception {
        ConsentId id = ConsentId.newId();
        willThrow(new NotPermittedException("not yours")).given(service).revoke(COOK, id);

        mvc.perform(delete("/consent-management/consents/" + id.value()).header("version", "1.0.0").with(cook("consent:write")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NOT_PERMITTED"));
    }

    @Test
    void anUnknownConsentIsNotFound() throws Exception {
        ConsentId id = ConsentId.newId();
        given(service.consent(id)).willThrow(new NotFoundException("missing"));

        mvc.perform(get("/consent-management/consents/" + id.value()).header("version", "1.0.0").with(cook("consent:read")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void revokingReturnsNoContent() throws Exception {
        mvc.perform(delete("/consent-management/consents/" + ConsentId.newId().value()).header("version", "1.0.0")
                        .with(cook("consent:write")))
                .andExpect(status().isNoContent());
    }

}
