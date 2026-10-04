package org.larder.media.domain;

import java.util.Objects;
import java.util.UUID;

/** The cook who uploaded a media; the only one who may delete it. */
public record UploaderId(UUID value) {

    public UploaderId {
        Objects.requireNonNull(value, "uploader must not be null");
    }
}
