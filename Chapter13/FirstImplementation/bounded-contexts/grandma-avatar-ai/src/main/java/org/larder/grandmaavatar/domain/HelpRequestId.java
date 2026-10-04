package org.larder.grandmaavatar.domain;

import java.util.Objects;
import java.util.UUID;

/** Identity of a Help Request of Cooking Assistance (the request Grandma is asked to answer). */
public record HelpRequestId(UUID value) {

    public HelpRequestId {
        Objects.requireNonNull(value, "helpRequestId");
    }
}
