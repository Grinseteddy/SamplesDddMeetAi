package org.larder.media.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.larder.media.adapter.in.web.model.Link;
import org.larder.media.adapter.in.web.model.LinkType;
import org.larder.media.domain.InvalidImageException;
import org.larder.media.domain.InvalidLinkException;
import org.larder.media.domain.ImageContent;

/** Translation between the contract's model and Media's domain, in both directions. */
class MediaMapperTest {

    private static final String PNG_BASE64 =
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==";

    @Test
    void everyLinkTypeTranslatesBothWays() {
        for (LinkType type : LinkType.values()) {
            assertThat(MediaMapper.toApi(MediaMapper.toDomain(type))).isEqualTo(type);
        }
    }

    @Test
    void linksTranslateToTheDomainAndMissingLinksMeanNone() {
        var links = MediaMapper.toDomain(List.of(
                new Link(LinkType.HELP, "https://larder.org/cooking-assistance/helps/a9caf90d-00b4-4a66-8184-7d02152e8d6a")));

        assertThat(links).hasSize(1);
        assertThat(MediaMapper.toApi(links.get(0)).getType()).isEqualTo(LinkType.HELP);
        assertThat(MediaMapper.toDomain((List<Link>) null)).isEmpty();
    }

    @Test
    void incompleteOrMalformedLinksAreRejected() {
        assertThatThrownBy(() -> MediaMapper.toDomain(List.of(new Link(null, "https://larder.org/recipes/x"))))
                .isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> MediaMapper.toDomain(List.of(new Link(LinkType.RECIPE, null))))
                .isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> MediaMapper.toDomain(List.of(new Link(LinkType.RECIPE, "http://bad uri"))))
                .isInstanceOf(InvalidLinkException.class);
    }

    @Test
    void base64BecomesImageContent() {
        assertThat(MediaMapper.toDomain(PNG_BASE64).size()).isEqualTo(Base64.getDecoder().decode(PNG_BASE64).length);
    }

    @Test
    void emptyTooLargeOrInvalidBase64IsRejected() {
        assertThatThrownBy(() -> MediaMapper.toDomain((String) null)).isInstanceOf(InvalidImageException.class);
        assertThatThrownBy(() -> MediaMapper.toDomain("")).isInstanceOf(InvalidImageException.class);
        assertThatThrownBy(() -> MediaMapper.toDomain("A".repeat((ImageContent.MAX_BYTES / 3 + 2) * 4)))
                .isInstanceOf(InvalidImageException.class).hasMessageContaining("larger");
        assertThatThrownBy(() -> MediaMapper.toDomain("not base64!")).isInstanceOf(InvalidImageException.class);
    }
}
