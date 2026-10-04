package org.larder.cookprofile.adapter.in.web;

import org.larder.cookprofile.adapter.in.web.model.Cook;
import org.larder.cookprofile.adapter.in.web.model.Status;
import org.larder.cookprofile.domain.CookStatus;

/** Translates between the domain model and the contract's model. */
final class CookMapper {

    private CookMapper() {
    }

    static Cook toApi(org.larder.cookprofile.domain.Cook cook) {
        return new Cook(
                cook.id().value(),
                cook.email().value(),
                cook.name().value(),
                cook.givenName().value(),
                cook.memberSince(),
                toApi(cook.status()));
    }

    static Status toApi(CookStatus status) {
        return switch (status) {
            case ACTIVE -> Status.ACTIVE;
            case INACTIVE -> Status.IN_ACTIVE;
            case PREMIUM -> Status.PREMIUM;
        };
    }

    static CookStatus toDomain(Status status) {
        return switch (status) {
            case ACTIVE -> CookStatus.ACTIVE;
            case IN_ACTIVE -> CookStatus.INACTIVE;
            case PREMIUM -> CookStatus.PREMIUM;
        };
    }
}
