package org.larder.media.application;

import java.util.Objects;

import org.larder.media.domain.ImageContent;
import org.larder.media.domain.Media;

/** A media with its bytes, as the read use cases return it. */
public record Image(Media media, ImageContent content) {

    public Image {
        Objects.requireNonNull(media);
        Objects.requireNonNull(content);
    }
}
