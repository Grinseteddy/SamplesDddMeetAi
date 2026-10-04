package org.larder.media.adapter.in.web;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.List;

import org.larder.media.adapter.in.web.model.Link;
import org.larder.media.adapter.in.web.model.LinkType;
import org.larder.media.adapter.in.web.model.Media;
import org.larder.media.application.Image;
import org.larder.media.domain.ImageContent;
import org.larder.media.domain.InvalidImageException;
import org.larder.media.domain.InvalidLinkException;

/**
 * Translates between the contract's model and the domain. Requests are translated into
 * domain values (which check the rules); responses only from the domain into the contract.
 */
final class MediaMapper {

    /** Longest base64 text that can decode to at most {@link ImageContent#MAX_BYTES}. */
    private static final long MAX_BASE64_LENGTH = (ImageContent.MAX_BYTES + 2L) / 3 * 4;

    private MediaMapper() {
    }

    static Media toApi(Image image) {
        return new Media(
                image.media().id().value(),
                Base64.getEncoder().encodeToString(image.content().bytes()),
                image.media().links().stream().map(MediaMapper::toApi).toList());
    }

    static Link toApi(org.larder.media.domain.Link link) {
        return new Link(toApi(link.type()), link.url().toString());
    }

    static LinkType toApi(org.larder.media.domain.LinkType type) {
        return switch (type) {
            case RECIPE -> LinkType.RECIPE;
            case THANKS -> LinkType.THANKS;
            case HELP_REQUEST -> LinkType.HELP_REQUEST;
            case HELP -> LinkType.HELP;
        };
    }

    static org.larder.media.domain.LinkType toDomain(LinkType type) {
        return switch (type) {
            case RECIPE -> org.larder.media.domain.LinkType.RECIPE;
            case THANKS -> org.larder.media.domain.LinkType.THANKS;
            case HELP_REQUEST -> org.larder.media.domain.LinkType.HELP_REQUEST;
            case HELP -> org.larder.media.domain.LinkType.HELP;
        };
    }

    static List<org.larder.media.domain.Link> toDomain(List<Link> links) {
        if (links == null) {
            return List.of();
        }
        return links.stream()
                .map(link -> new org.larder.media.domain.Link(
                        link.getType() == null ? null : toDomain(link.getType()), toUri(link.getUrl())))
                .toList();
    }

    private static URI toUri(String url) {
        if (url == null) {
            return null;
        }
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            throw new InvalidLinkException("A link url must be a valid uri: " + e.getMessage());
        }
    }

    /** Decodes standard base64 (RFC 4648, no line breaks) into image content. */
    static ImageContent toDomain(String base64) {
        if (base64 == null || base64.isEmpty()) {
            throw new InvalidImageException("An image must not be empty");
        }
        if (base64.length() > MAX_BASE64_LENGTH) {
            throw new InvalidImageException("An image must not be larger than " + ImageContent.MAX_BYTES + " bytes");
        }
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(base64);
        } catch (IllegalArgumentException e) {
            throw new InvalidImageException("The media is not valid base64: " + e.getMessage());
        }
        return ImageContent.of(bytes);
    }
}
