package org.larder.consentmanagement.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.larder.consentmanagement.adapter.in.web.api.ConsentTextsApi;
import org.larder.consentmanagement.adapter.in.web.model.ConsentText;
import org.larder.consentmanagement.application.ConsentService;
import org.larder.consentmanagement.domain.ConsentTextId;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST adapter for {@code contracts/openapi/consent-management.openapi.yaml}, tag Consent Texts. */
@RestController("consentmanagementConsentTextsController")
@RequestMapping("/consent-management")
class ConsentTextsController implements ConsentTextsApi {

    private final ConsentService service;

    ConsentTextsController(ConsentService service) {
        this.service = service;
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SCOPE_consent:read', 'SCOPE_consent:write')")
    public ResponseEntity<List<ConsentText>> getConsentTexts(String version) {
        return ResponseEntity.ok(service.consentTexts().stream().map(ConsentMapper::toApi).toList());
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SCOPE_consent:read', 'SCOPE_consent:write')")
    public ResponseEntity<ConsentText> getConsentTextById(UUID consentTextId, String version) {
        return ResponseEntity.ok(ConsentMapper.toApi(service.consentText(new ConsentTextId(consentTextId))));
    }
}
