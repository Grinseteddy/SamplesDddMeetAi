package org.larder.cookingassistance.adapter.in.web;

import static org.larder.cookingassistance.adapter.in.web.CookingAssistanceRequests.body;
import static org.larder.cookingassistance.adapter.in.web.CookingAssistanceRequests.caller;

import java.util.List;
import java.util.UUID;

import org.larder.cookingassistance.adapter.in.web.api.HelpRequestsApi;
import org.larder.cookingassistance.adapter.in.web.model.HelpRequest;
import org.larder.cookingassistance.adapter.in.web.model.HelpRequestCreate;
import org.larder.cookingassistance.adapter.in.web.model.HelpRequestLink;
import org.larder.cookingassistance.adapter.in.web.model.HelpRequestStatus;
import org.larder.cookingassistance.adapter.in.web.model.HelpRequestUpdate;
import org.larder.cookingassistance.application.HelpRequestService;
import org.larder.cookingassistance.domain.CookId;
import org.larder.cookingassistance.domain.HelpRequestId;
import org.larder.platform.web.Links;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST adapter for {@code contracts/openapi/cooking-assistance.openapi.yaml}, tag Help Requests. */
@RestController("cookingassistanceHelpRequestsController")
@RequestMapping("/cooking-assistance")
class HelpRequestsController implements HelpRequestsApi {

    private final HelpRequestService service;

    HelpRequestsController(HelpRequestService service) {
        this.service = service;
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SCOPE_help:read', 'SCOPE_help:write')")
    public ResponseEntity<List<HelpRequest>> listHelpRequests(String version, UUID requester, HelpRequestStatus status) {
        var requests = service.helpRequests(
                requester == null ? null : new CookId(requester),
                status == null ? null : org.larder.cookingassistance.domain.HelpRequestStatus.valueOf(status.name()));
        return ResponseEntity.ok(requests.stream().map(CookingAssistanceMapper::toApi).toList());
    }

    @Override
    @PreAuthorize("hasAuthority('SCOPE_help:write')")
    public ResponseEntity<HelpRequestLink> createHelpRequest(String version, HelpRequestCreate request) {
        var helpRequest = service.raise(caller(), CookingAssistanceRequests.toDraft(body(request)));
        var link = Links.below(helpRequest.id().value());
        return ResponseEntity.created(link).body(new HelpRequestLink(link));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SCOPE_help:read', 'SCOPE_help:write')")
    public ResponseEntity<HelpRequest> getHelpRequestById(UUID helpRequestId, String version) {
        return ResponseEntity.ok(CookingAssistanceMapper.toApi(service.helpRequest(new HelpRequestId(helpRequestId))));
    }

    @Override
    @PreAuthorize("hasAuthority('SCOPE_help:write')")
    public ResponseEntity<HelpRequestLink> updateHelpRequest(UUID helpRequestId, String version, HelpRequestUpdate request) {
        service.revise(caller(), new HelpRequestId(helpRequestId), CookingAssistanceRequests.toRevision(body(request)));
        return ResponseEntity.ok(new HelpRequestLink(Links.current()));
    }

    @Override
    @PreAuthorize("hasAuthority('SCOPE_help:write')")
    public ResponseEntity<Void> deleteHelpRequest(UUID helpRequestId, String version) {
        service.withdraw(caller(), new HelpRequestId(helpRequestId));
        return ResponseEntity.noContent().build();
    }
}
