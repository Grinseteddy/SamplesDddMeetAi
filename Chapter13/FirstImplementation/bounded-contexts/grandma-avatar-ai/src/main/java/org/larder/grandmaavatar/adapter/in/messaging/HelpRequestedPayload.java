package org.larder.grandmaavatar.adapter.in.messaging;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Payload of {@code HelpRequested} as the published language of Cooking Assistance defines it
 * ({@code HelpRequestedPayload}). Tolerant reader: enums are read as strings and checked when mapped,
 * unknown properties are ignored.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record HelpRequestedPayload(
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
}
