package org.larder.cookingassistance.adapter.in.web;

import static org.larder.cookingassistance.adapter.in.web.CookingAssistanceRequests.body;
import static org.larder.cookingassistance.adapter.in.web.CookingAssistanceRequests.caller;

import java.util.List;
import java.util.UUID;

import org.larder.cookingassistance.adapter.in.web.api.HelpsApi;
import org.larder.cookingassistance.adapter.in.web.model.Help;
import org.larder.cookingassistance.adapter.in.web.model.HelpCreate;
import org.larder.cookingassistance.adapter.in.web.model.HelpLink;
import org.larder.cookingassistance.adapter.in.web.model.HelpType;
import org.larder.cookingassistance.adapter.in.web.model.HelpUpdate;
import org.larder.cookingassistance.application.HelpService;
import org.larder.cookingassistance.domain.CookId;
import org.larder.cookingassistance.domain.HelpId;
import org.larder.cookingassistance.domain.HelpRequestId;
import org.larder.platform.web.Links;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for {@code contracts/openapi/cooking-assistance.openapi.yaml}, tag Helps. The help
 * provider is always the calling cook; the Grandma Avatar answers by message, not over REST.
 */
@RestController("cookingassistanceHelpsController")
@RequestMapping("/cooking-assistance")
class HelpsController implements HelpsApi {

    private final HelpService service;

    HelpsController(HelpService service) {
        this.service = service;
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SCOPE_help:read', 'SCOPE_help:write')")
    public ResponseEntity<List<Help>> listHelps(String version, UUID helpRequestId, HelpType type, UUID helpProvider) {
        var helps = service.helps(caller(),
                helpRequestId == null ? null : new HelpRequestId(helpRequestId),
                CookingAssistanceRequests.toDomain(type),
                helpProvider == null ? null : new CookId(helpProvider));
        return ResponseEntity.ok(helps.stream().map(CookingAssistanceMapper::toApi).toList());
    }

    @Override
    @PreAuthorize("hasAuthority('SCOPE_help:write')")
    public ResponseEntity<HelpLink> createHelp(String version, HelpCreate request) {
        body(request);
        var help = service.give(caller(), new HelpRequestId(request.getHelpRequest()),
                CookingAssistanceRequests.toDomain(request.getHelpProviderType()), request.getAnswerTitle(),
                CookingAssistanceRequests.toDomain(request.getAnswer()));
        var link = Links.below(help.id().value());
        return ResponseEntity.created(link).body(new HelpLink(link));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SCOPE_help:read', 'SCOPE_help:write')")
    public ResponseEntity<Help> getHelpById(UUID helpId, String version) {
        return ResponseEntity.ok(CookingAssistanceMapper.toApi(service.help(caller(), new HelpId(helpId))));
    }

    @Override
    @PreAuthorize("hasAuthority('SCOPE_help:write')")
    public ResponseEntity<HelpLink> updateHelp(UUID helpId, String version, HelpUpdate request) {
        body(request);
        service.revise(caller(), new HelpId(helpId), request.getAnswerTitle(),
                CookingAssistanceRequests.toDomain(request.getAnswer()));
        return ResponseEntity.ok(new HelpLink(Links.current()));
    }

    @Override
    @PreAuthorize("hasAuthority('SCOPE_help:write')")
    public ResponseEntity<Void> deleteHelp(UUID helpId, String version) {
        service.withdraw(caller(), new HelpId(helpId));
        return ResponseEntity.noContent().build();
    }
}
