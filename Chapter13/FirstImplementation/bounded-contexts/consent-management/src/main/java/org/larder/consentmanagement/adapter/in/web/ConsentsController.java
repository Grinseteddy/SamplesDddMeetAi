package org.larder.consentmanagement.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.larder.consentmanagement.adapter.in.web.api.ConsentsApi;
import org.larder.consentmanagement.adapter.in.web.model.Consent;
import org.larder.consentmanagement.adapter.in.web.model.ConsentCreate;
import org.larder.consentmanagement.adapter.in.web.model.ConsentLink;
import org.larder.consentmanagement.application.ConsentService;
import org.larder.consentmanagement.domain.ConsentId;
import org.larder.consentmanagement.domain.ConsentTextId;
import org.larder.consentmanagement.domain.SubjectId;
import org.larder.platform.security.CurrentCook;
import org.larder.platform.web.Links;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST adapter for {@code contracts/openapi/consent-management.openapi.yaml}, tag Consents. */
@RestController("consentmanagementConsentsController")
@RequestMapping("/consent-management")
class ConsentsController implements ConsentsApi {

    private final ConsentService service;

    ConsentsController(ConsentService service) {
        this.service = service;
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SCOPE_consent:read', 'SCOPE_consent:write')")
    public ResponseEntity<List<Consent>> getConsents(UUID subject, String version) {
        return ResponseEntity.ok(service.consentsOf(new SubjectId(subject)).stream().map(ConsentMapper::toApi).toList());
    }

    @Override
    @PreAuthorize("hasAuthority('SCOPE_consent:write')")
    public ResponseEntity<ConsentLink> createConsent(String version, ConsentCreate request) {
        var consent = service.give(caller(), new SubjectId(request.getSubject()), new ConsentTextId(request.getConsentTextId()));
        var link = Links.below(consent.id().value());
        return ResponseEntity.created(link).body(new ConsentLink(link));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SCOPE_consent:read', 'SCOPE_consent:write')")
    public ResponseEntity<Consent> getConsentById(UUID consentId, String version) {
        return ResponseEntity.ok(ConsentMapper.toApi(service.consent(new ConsentId(consentId))));
    }

    @Override
    @PreAuthorize("hasAuthority('SCOPE_consent:write')")
    public ResponseEntity<Void> revokeConsent(UUID consentId, String version) {
        service.revoke(caller(), new ConsentId(consentId));
        return ResponseEntity.noContent().build();
    }

    private static SubjectId caller() {
        return new SubjectId(CurrentCook.require().value());
    }
}
