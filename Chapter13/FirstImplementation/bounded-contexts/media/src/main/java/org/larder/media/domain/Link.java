package org.larder.media.domain;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

/**
 * A reference from a media to one business object that uses it.
 *
 * <ul>
 *   <li>The url is an absolute {@code http(s)} address of at most {@value #MAX_URL_LENGTH} characters.</li>
 *   <li>Its last path segment is the id of the business object
 *       (e.g. {@code .../recipe-catalog/recipes/{recipeId}}), so media can be found by
 *       business object id and type.</li>
 * </ul>
 */
public record Link(LinkType type, URI url) {

    public static final int MAX_URL_LENGTH = 2048;

    public Link {
        if (type == null) {
            throw new InvalidLinkException("A link needs a type");
        }
        if (url == null) {
            throw new InvalidLinkException("A link needs a url");
        }
        if (!url.isAbsolute() || !("http".equalsIgnoreCase(url.getScheme()) || "https".equalsIgnoreCase(url.getScheme()))
                || url.getHost() == null) {
            throw new InvalidLinkException("A link url must be an absolute http(s) address: " + url);
        }
        if (url.toString().length() > MAX_URL_LENGTH) {
            throw new InvalidLinkException("A link url must not be longer than " + MAX_URL_LENGTH + " characters");
        }
        lastSegmentAsId(url);
    }

    /** The id of the linked business object, taken from the last path segment of the url. */
    public BusinessObjectId businessObjectId() {
        return lastSegmentAsId(url);
    }

    public boolean pointsTo(LinkType candidateType, BusinessObjectId candidateId) {
        return type == candidateType && businessObjectId().equals(candidateId);
    }

    private static BusinessObjectId lastSegmentAsId(URI url) {
        String path = Objects.requireNonNullElse(url.getPath(), "");
        while (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        String last = path.substring(path.lastIndexOf('/') + 1);
        try {
            return new BusinessObjectId(UUID.fromString(last));
        } catch (IllegalArgumentException e) {
            throw new InvalidLinkException("A link url must end with the id (uuid) of the business object: " + url);
        }
    }
}
