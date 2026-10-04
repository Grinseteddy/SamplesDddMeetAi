package org.larder.grandmaavatar.domain;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

/** Identity of a Help given by Grandma. */
public record HelpId(UUID value) {

    public HelpId {
        Objects.requireNonNull(value, "helpId");
    }

    /**
     * Grandma answers a request at most once, so her Help's identity is derived from the request:
     * the same request always yields the same helpId, even if it is delivered (and answered) twice.
     * Consumers can therefore deduplicate by helpId as well.
     */
    public static HelpId forRequest(HelpRequestId request) {
        return new HelpId(UUID.nameUUIDFromBytes(
                ("grandma-avatar/help/" + request.value()).getBytes(StandardCharsets.UTF_8)));
    }
}
