package org.larder.grandmaavatar.application;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.larder.grandmaavatar.domain.Help;

/** Remembers every published Help. */
public class RecordingHelpPublisher implements HelpPublisher {

    public final List<Published> published = new ArrayList<>();

    @Override
    public void helpProvided(Help help, UUID correlationId) {
        published.add(new Published(help, correlationId));
    }

    public record Published(Help help, UUID correlationId) {
    }
}
