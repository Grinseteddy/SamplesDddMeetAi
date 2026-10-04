package org.larder.sharing.domain;

import static org.larder.sharing.domain.ThanksRuleViolationException.INVALID_PICTURE;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

/**
 * The picture of the finished meal: a link to an image of Media, e.g.
 * {@code https://larder.org/media/images/b009a5d1-0205-4b0c-af82-822229cf243a}.
 *
 * <p>The link must be an absolute http(s) URI whose path ends with {@code /images/{mediaId}}
 * (the shape of Media's {@code imageLink}); the media id is its last path segment. The host is not
 * checked: Media answers with links of the host it was called on, which differs per deployment.
 * Whether the image exists is checked against Media by the application, not here.
 */
public record Picture(URI link, MediaId mediaId) {

    public Picture {
        Objects.requireNonNull(link, "link must not be null");
        Objects.requireNonNull(mediaId, "mediaId must not be null");
    }

    public static Picture of(URI link) {
        if (link == null) {
            throw new ThanksRuleViolationException(INVALID_PICTURE, "Thanks need a picture");
        }
        String scheme = link.getScheme();
        String path = link.getPath();
        if (!link.isAbsolute() || !("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme))
                || link.getHost() == null || path == null || link.getQuery() != null || link.getFragment() != null) {
            throw notAnImageOfMedia(link);
        }
        String[] segments = path.split("/");
        if (segments.length < 2 || !"images".equals(segments[segments.length - 2])) {
            throw notAnImageOfMedia(link);
        }
        try {
            String id = segments[segments.length - 1];
            UUID mediaId = UUID.fromString(id);
            if (!mediaId.toString().equalsIgnoreCase(id)) {
                throw notAnImageOfMedia(link);
            }
            return new Picture(link, new MediaId(mediaId));
        } catch (IllegalArgumentException e) {
            throw notAnImageOfMedia(link);
        }
    }

    private static ThanksRuleViolationException notAnImageOfMedia(URI link) {
        return new ThanksRuleViolationException(INVALID_PICTURE,
                "The picture " + link + " is not a link to an image of Media (.../images/{mediaId})");
    }
}
