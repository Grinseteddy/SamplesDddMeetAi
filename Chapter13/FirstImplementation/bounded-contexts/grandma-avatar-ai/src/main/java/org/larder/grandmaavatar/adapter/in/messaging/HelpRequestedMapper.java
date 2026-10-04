package org.larder.grandmaavatar.adapter.in.messaging;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.larder.grandmaavatar.domain.CookId;
import org.larder.grandmaavatar.domain.HelpProviderType;
import org.larder.grandmaavatar.domain.HelpRequest;
import org.larder.grandmaavatar.domain.HelpRequestId;
import org.larder.grandmaavatar.domain.HelpType;
import org.larder.grandmaavatar.domain.HowToStepId;
import org.larder.grandmaavatar.domain.IngredientId;
import org.larder.grandmaavatar.domain.InvalidHelpRequestException;
import org.larder.grandmaavatar.domain.RecipeId;

/** Translates the published language of Cooking Assistance into Grandma's {@link HelpRequest}. */
public final class HelpRequestedMapper {

    private HelpRequestedMapper() {
    }

    /** @throws InvalidHelpRequestException if the payload breaks the contract */
    public static HelpRequest toDomain(HelpRequestedPayload payload) {
        require(payload != null, "payload is missing");
        require(payload.helpRequestId() != null, "helpRequestId is missing");
        require(payload.requester() != null, "requester is missing");
        require(payload.status() != null, "status is missing");
        require(payload.ingredients() == null || payload.ingredients().stream().allMatch(id -> id != null),
                "ingredients must be ids");
        return new HelpRequest(
                new HelpRequestId(payload.helpRequestId()),
                new CookId(payload.requester()),
                payload.title(),
                enumValue(HelpType.class, payload.type(), "type"),
                payload.description(),
                Optional.ofNullable(payload.recipe()).map(RecipeId::new),
                Optional.ofNullable(payload.howToStep()).map(HowToStepId::new),
                payload.ingredients() == null ? List.of()
                        : payload.ingredients().stream().map(IngredientId::new).toList(),
                providers(payload.preferredProvider()),
                // The contract allows only OPEN; anything else is read as "no longer open" and not answered.
                "OPEN".equals(payload.status()));
    }

    private static Set<HelpProviderType> providers(List<String> preferredProvider) {
        require(preferredProvider != null, "preferredProvider is missing");
        Set<HelpProviderType> providers = EnumSet.noneOf(HelpProviderType.class);
        for (String provider : preferredProvider) {
            require(providers.add(enumValue(HelpProviderType.class, provider, "preferredProvider")),
                    "preferredProvider must not repeat " + provider);
        }
        return providers;
    }

    private static <E extends Enum<E>> E enumValue(Class<E> type, String value, String what) {
        require(value != null, what + " is missing");
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException e) {
            throw new InvalidHelpRequestException(what + " " + value + " is unknown");
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new InvalidHelpRequestException(message);
        }
    }
}
