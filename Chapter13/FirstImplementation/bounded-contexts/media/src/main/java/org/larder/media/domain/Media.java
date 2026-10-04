package org.larder.media.domain;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

/**
 * An uploaded image together with the business objects it is used in (aggregate root).
 * The aggregate holds the metadata; the image bytes live in the bucket under the media id.
 *
 * <ul>
 *   <li>A media may have no links (not yet used anywhere) and at most {@value #MAX_LINKS} links.</li>
 *   <li>The same link (type and url) is kept only once.</li>
 *   <li>Links cannot be changed after the upload; delete and upload again instead.</li>
 *   <li>Only the cook who uploaded a media may delete it.</li>
 * </ul>
 */
public final class Media {

    public static final int MAX_LINKS = 20;

    private final MediaId id;
    private final UploaderId uploader;
    private final ImageFormat format;
    private final long size;
    private final List<Link> links;
    private final Instant uploadedAt;

    private Media(MediaId id, UploaderId uploader, ImageFormat format, long size, List<Link> links, Instant uploadedAt) {
        this.id = Objects.requireNonNull(id);
        this.uploader = Objects.requireNonNull(uploader);
        this.format = Objects.requireNonNull(format);
        this.size = size;
        this.links = List.copyOf(new LinkedHashSet<>(Objects.requireNonNull(links)));
        this.uploadedAt = Objects.requireNonNull(uploadedAt);
        if (this.links.size() > MAX_LINKS) {
            throw new InvalidLinkException("A media can be used in at most " + MAX_LINKS + " business objects");
        }
    }

    public static Media upload(UploaderId uploader, ImageContent content, List<Link> links, Instant now) {
        return new Media(MediaId.newId(), uploader, content.format(), content.size(), links, now);
    }

    /** Recreates a stored media. */
    public static Media restore(MediaId id, UploaderId uploader, ImageFormat format, long size, List<Link> links, Instant uploadedAt) {
        return new Media(id, uploader, format, size, links, uploadedAt);
    }

    public boolean isUploadedBy(UploaderId candidate) {
        return uploader.equals(candidate);
    }

    public boolean isUsedIn(LinkType type, BusinessObjectId businessObject) {
        return links.stream().anyMatch(link -> link.pointsTo(type, businessObject));
    }

    public MediaId id() {
        return id;
    }

    public UploaderId uploader() {
        return uploader;
    }

    public ImageFormat format() {
        return format;
    }

    public long size() {
        return size;
    }

    public List<Link> links() {
        return links;
    }

    public Instant uploadedAt() {
        return uploadedAt;
    }
}
