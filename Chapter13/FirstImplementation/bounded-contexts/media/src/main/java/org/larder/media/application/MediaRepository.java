package org.larder.media.application;

import java.util.List;
import java.util.Optional;

import org.larder.media.domain.BusinessObjectId;
import org.larder.media.domain.LinkType;
import org.larder.media.domain.Media;
import org.larder.media.domain.MediaId;

/** Port: the metadata of the uploaded media (who uploaded them, format, size, links). */
public interface MediaRepository {

    /** Stores a new media; media are immutable after the upload. */
    void add(Media media);

    Optional<Media> findById(MediaId id);

    /** All media linked to one business object, oldest upload first. */
    List<Media> findByBusinessObject(LinkType type, BusinessObjectId businessObject);

    void remove(MediaId id);
}
