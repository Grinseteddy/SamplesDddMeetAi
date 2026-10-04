package org.larder.recipecatalog.domain;

import java.net.URI;

/** What a cook supplies for a new how-to step; the recipe gives it its identity. */
public record HowToStepDraft(int sequenceNumber, String description, URI illustration) {
}
