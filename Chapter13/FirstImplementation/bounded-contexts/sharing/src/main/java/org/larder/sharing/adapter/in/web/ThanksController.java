package org.larder.sharing.adapter.in.web;

import static org.larder.sharing.adapter.in.web.ThanksRequests.caller;

import java.util.UUID;

import org.larder.platform.web.Links;
import org.larder.sharing.adapter.in.web.api.ThanksApi;
import org.larder.sharing.adapter.in.web.model.Thanks;
import org.larder.sharing.adapter.in.web.model.ThanksCreate;
import org.larder.sharing.adapter.in.web.model.ThanksLink;
import org.larder.sharing.adapter.in.web.model.ThanksList;
import org.larder.sharing.adapter.in.web.model.ThanksUpdate;
import org.larder.sharing.application.ThanksFilter;
import org.larder.sharing.application.ThanksService;
import org.larder.sharing.domain.CookId;
import org.larder.sharing.domain.HelpId;
import org.larder.sharing.domain.Picture;
import org.larder.sharing.domain.ThanksId;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for {@code contracts/openapi/sharing.openapi.yaml}, tag Thanks.
 *
 * <p>Reading needs any of the API's scopes. Writing needs {@code sharing:write} - the only scope the
 * write operations list; {@code sharing:admin} ("administrative access to all thanks") therefore grants
 * reading only: the contract lets only the giver change or withdraw thanks.
 * Giving or changing thanks also calls Cooking Assistance, Media and Consent Management with the
 * caller's token, so the caller needs {@code help:read}, {@code media:read} and {@code consent:read}, too.
 */
@RestController("sharingThanksController")
@RequestMapping("/sharing")
class ThanksController implements ThanksApi {

    private final ThanksService service;

    ThanksController(ThanksService service) {
        this.service = service;
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SCOPE_sharing:read', 'SCOPE_sharing:write', 'SCOPE_sharing:admin')")
    public ResponseEntity<ThanksList> listThanks(String version, UUID giver, UUID recipient) {
        var filter = new ThanksFilter(giver == null ? null : new CookId(giver),
                recipient == null ? null : new CookId(recipient));
        return ResponseEntity.ok(new ThanksList(service.thanks(filter).stream().map(ThanksMapper::toApi).toList()));
    }

    @Override
    @PreAuthorize("hasAuthority('SCOPE_sharing:write')")
    public ResponseEntity<ThanksLink> createThanks(String version, ThanksCreate request) {
        var thanks = service.give(caller(), new HelpId(request.getHelpId()),
                ThanksRequests.toRecipients(request.getRecipients()),
                request.getThanksText(),
                Picture.of(request.getPictures()));
        var link = Links.below(thanks.id().value());
        return ResponseEntity.created(link).body(new ThanksLink(link));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SCOPE_sharing:read', 'SCOPE_sharing:write', 'SCOPE_sharing:admin')")
    public ResponseEntity<Thanks> getThanksById(UUID thanksId, String version) {
        return ResponseEntity.ok(ThanksMapper.toApi(service.thanks(new ThanksId(thanksId))));
    }

    @Override
    @PreAuthorize("hasAuthority('SCOPE_sharing:write')")
    public ResponseEntity<ThanksLink> updateThanks(UUID thanksId, String version, ThanksUpdate request) {
        service.revise(caller(), new ThanksId(thanksId), ThanksRequests.toRevision(request));
        return ResponseEntity.ok(new ThanksLink(Links.current()));
    }

    @Override
    @PreAuthorize("hasAuthority('SCOPE_sharing:write')")
    public ResponseEntity<Void> deleteThanks(UUID thanksId, String version) {
        service.withdraw(caller(), new ThanksId(thanksId));
        return ResponseEntity.noContent().build();
    }
}
