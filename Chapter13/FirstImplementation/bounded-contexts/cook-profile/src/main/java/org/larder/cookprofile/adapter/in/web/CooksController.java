package org.larder.cookprofile.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.larder.cookprofile.adapter.in.web.api.CooksApi;
import org.larder.cookprofile.adapter.in.web.model.Cook;
import org.larder.cookprofile.adapter.in.web.model.CookLink;
import org.larder.cookprofile.adapter.in.web.model.CookUpdate;
import org.larder.cookprofile.adapter.in.web.model.User;
import org.larder.cookprofile.application.CookService;
import org.larder.cookprofile.domain.CookChange;
import org.larder.cookprofile.domain.CookId;
import org.larder.cookprofile.domain.EmailAddress;
import org.larder.cookprofile.domain.PersonName;
import org.larder.platform.security.CurrentCook;
import org.larder.platform.web.Links;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST adapter for {@code contracts/openapi/cook-profile.openapi.yaml}, tag Cooks. */
@RestController("cookprofileCooksController")
@RequestMapping("/cook-profile")
class CooksController implements CooksApi {

    private final CookService service;

    CooksController(CookService service) {
        this.service = service;
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SCOPE_cook:read', 'SCOPE_cook:write')")
    public ResponseEntity<List<Cook>> listCooks(String version, String name, String email) {
        var cooks = service.cooks(
                name == null ? null : new PersonName(name),
                email == null ? null : new EmailAddress(email));
        return ResponseEntity.ok(cooks.stream().map(CookMapper::toApi).toList());
    }

    @Override
    @PreAuthorize("hasAuthority('SCOPE_cook:write')")
    public ResponseEntity<CookLink> createCook(String version, User user) {
        var cook = service.register(caller(),
                new EmailAddress(user.getEmail()), new PersonName(user.getName()), new PersonName(user.getGivenName()));
        var link = Links.below(cook.id().value());
        return ResponseEntity.created(link).body(new CookLink(link));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SCOPE_cook:read', 'SCOPE_cook:write')")
    public ResponseEntity<Cook> getCookById(UUID cookId, String version) {
        return ResponseEntity.ok(CookMapper.toApi(service.cook(new CookId(cookId))));
    }

    @Override
    @PreAuthorize("hasAuthority('SCOPE_cook:write')")
    public ResponseEntity<CookLink> updateCook(UUID cookId, String version, CookUpdate update) {
        var change = new CookChange(
                update.getEmail() == null ? null : new EmailAddress(update.getEmail()),
                update.getName() == null ? null : new PersonName(update.getName()),
                update.getGivenName() == null ? null : new PersonName(update.getGivenName()),
                update.getStatus() == null ? null : CookMapper.toDomain(update.getStatus()));
        service.change(caller(), new CookId(cookId), change);
        return ResponseEntity.ok(new CookLink(Links.current()));
    }

    @Override
    @PreAuthorize("hasAuthority('SCOPE_cook:write')")
    public ResponseEntity<Void> deleteCook(UUID cookId, String version) {
        service.deregister(caller(), new CookId(cookId));
        return ResponseEntity.noContent().build();
    }

    private static CookId caller() {
        return new CookId(CurrentCook.require().value());
    }
}
