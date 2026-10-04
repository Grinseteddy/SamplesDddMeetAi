package org.larder.media.application;

import java.util.Optional;

import org.larder.media.domain.ImageContent;
import org.larder.media.domain.MediaId;

/**
 * Port: the object bucket holding the image bytes, one object per media id (ADR0001).
 * Every operation may fail with {@link ImageStoreException}.
 */
public interface ImageStore {

    void put(MediaId id, ImageContent content);

    Optional<ImageContent> get(MediaId id);

    /** Removes the object; removing a missing object is not an error. */
    void delete(MediaId id);
}
