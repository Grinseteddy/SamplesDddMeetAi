package org.larder.media.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.media.TestData.COOK;
import static org.larder.media.TestData.HELP_REQUEST;
import static org.larder.media.TestData.NOW;
import static org.larder.media.TestData.OTHER_COOK;
import static org.larder.media.TestData.PNG;
import static org.larder.media.TestData.RECIPE;
import static org.larder.media.TestData.RECIPE_ID;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

class MediaTest {

    @Test
    void anUploadedMediaKnowsItsUploaderFormatSizeAndLinks() {
        Media media = Media.upload(COOK, PNG, List.of(RECIPE, HELP_REQUEST), NOW);

        assertThat(media.isUploadedBy(COOK)).isTrue();
        assertThat(media.isUploadedBy(OTHER_COOK)).isFalse();
        assertThat(media.format()).isEqualTo(ImageFormat.PNG);
        assertThat(media.size()).isEqualTo(PNG.size());
        assertThat(media.links()).containsExactly(RECIPE, HELP_REQUEST);
        assertThat(media.uploadedAt()).isEqualTo(NOW);
        assertThat(media.isUsedIn(LinkType.RECIPE, RECIPE_ID)).isTrue();
        assertThat(media.isUsedIn(LinkType.THANKS, RECIPE_ID)).isFalse();
    }

    @Test
    void aMediaMayBeUsedNowhereYet() {
        assertThat(Media.upload(COOK, PNG, List.of(), NOW).links()).isEmpty();
    }

    @Test
    void theSameLinkIsKeptOnlyOnce() {
        assertThat(Media.upload(COOK, PNG, List.of(RECIPE, RECIPE), NOW).links()).containsExactly(RECIPE);
    }

    @Test
    void aMediaHasAtMostTwentyLinks() {
        List<Link> links = IntStream.range(0, Media.MAX_LINKS + 1)
                .mapToObj(i -> new Link(LinkType.HELP, URI.create("https://larder.org/cooking-assistance/helps/" + UUID.randomUUID())))
                .toList();

        assertThatThrownBy(() -> Media.upload(COOK, PNG, links, NOW)).isInstanceOf(InvalidLinkException.class);
        assertThat(Media.upload(COOK, PNG, links.subList(0, Media.MAX_LINKS), NOW).links()).hasSize(Media.MAX_LINKS);
    }

    @Test
    void aLinkNamesTheBusinessObjectByTheLastSegmentOfItsUrl() {
        assertThat(RECIPE.businessObjectId()).isEqualTo(RECIPE_ID);
        assertThat(new Link(LinkType.RECIPE, URI.create(RECIPE.url() + "/")).businessObjectId()).isEqualTo(RECIPE_ID);
    }

    @Test
    void aLinkNeedsAnAbsoluteHttpUrlEndingWithAnId() {
        assertThatThrownBy(() -> new Link(LinkType.RECIPE, URI.create("/recipe-catalog/recipes/" + RECIPE_ID.value())))
                .isInstanceOf(InvalidLinkException.class);
        assertThatThrownBy(() -> new Link(LinkType.RECIPE, URI.create("ftp://larder.org/recipes/" + RECIPE_ID.value())))
                .isInstanceOf(InvalidLinkException.class);
        assertThatThrownBy(() -> new Link(LinkType.RECIPE, URI.create("https://larder.org/recipe-catalog/recipes")))
                .isInstanceOf(InvalidLinkException.class);
        assertThatThrownBy(() -> new Link(null, RECIPE.url())).isInstanceOf(InvalidLinkException.class);
        assertThatThrownBy(() -> new Link(LinkType.RECIPE, URI.create("https://larder.org/" + "x".repeat(2048) + "/" + RECIPE_ID.value())))
                .isInstanceOf(InvalidLinkException.class);
    }
}
