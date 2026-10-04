package org.larder.cookingassistance.adapter.out.messaging;

import java.util.List;
import java.util.UUID;

import org.larder.cookingassistance.domain.HelpRequest;
import org.larder.cookingassistance.domain.HowToStepId;
import org.larder.cookingassistance.domain.IngredientId;
import org.larder.cookingassistance.domain.RecipeId;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Payload of HelpRequested ({@code HelpRequestedPayload} in cooking-assistance.asyncapi.yaml).
 * Optional references are left out, not sent as {@code null} or empty.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
record HelpRequestedPayload(
        UUID helpRequestId,
        UUID requester,
        String title,
        String type,
        String description,
        UUID recipe,
        UUID howToStep,
        List<UUID> ingredients,
        List<String> preferredProvider,
        String status) {

    static HelpRequestedPayload of(HelpRequest request) {
        return new HelpRequestedPayload(
                request.id().value(),
                request.requester().value(),
                request.title(),
                request.type().name(),
                request.description(),
                request.recipe().map(RecipeId::value).orElse(null),
                request.howToStep().map(HowToStepId::value).orElse(null),
                request.ingredients().stream().map(IngredientId::value).toList(),
                request.preferredProviders().stream().map(Enum::name).toList(),
                request.status().name());
    }
}
