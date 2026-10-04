package org.larder.media.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.larder.media.TestData.COOK;
import static org.larder.media.TestData.HELP_REQUEST;
import static org.larder.media.TestData.HELP_REQUEST_ID;
import static org.larder.media.TestData.NOW;
import static org.larder.media.TestData.PNG;
import static org.larder.media.TestData.RECIPE;
import static org.larder.media.TestData.RECIPE_ID;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.larder.media.domain.ImageFormat;
import org.larder.media.domain.LinkType;
import org.larder.media.domain.Media;
import org.larder.media.domain.MediaId;
import org.larder.platform.test.TestDatabase;

/** Runs the real migrations against PostgreSQL in its own schema with its own user. */
class JdbcMediaRepositoryTest {

    private JdbcMediaRepository media;

    @BeforeEach
    void freshSchema() {
        media = new JdbcMediaRepository(TestDatabase.forSchema("media").jdbcClient());
    }

    @Test
    void storesAMediaWithItsLinksInOrder() {
        Media uploaded = Media.upload(COOK, PNG, List.of(HELP_REQUEST, RECIPE), NOW);
        media.add(uploaded);

        Media stored = media.findById(uploaded.id()).orElseThrow();
        assertThat(stored.uploader()).isEqualTo(COOK);
        assertThat(stored.format()).isEqualTo(ImageFormat.PNG);
        assertThat(stored.size()).isEqualTo(PNG.size());
        assertThat(stored.uploadedAt()).isEqualTo(NOW);
        assertThat(stored.links()).containsExactly(HELP_REQUEST, RECIPE);
    }

    @Test
    void storesAMediaWithoutLinks() {
        Media uploaded = Media.upload(COOK, PNG, List.of(), NOW);
        media.add(uploaded);

        assertThat(media.findById(uploaded.id()).orElseThrow().links()).isEmpty();
    }

    @Test
    void findsTheMediaOfOneBusinessObjectByIdAndType() {
        Media first = Media.upload(COOK, PNG, List.of(RECIPE), NOW);
        Media second = Media.upload(COOK, PNG, List.of(RECIPE, HELP_REQUEST), NOW.plusSeconds(60));
        Media other = Media.upload(COOK, PNG, List.of(HELP_REQUEST), NOW);
        media.add(second);
        media.add(first);
        media.add(other);

        assertThat(media.findByBusinessObject(LinkType.RECIPE, RECIPE_ID)).extracting(Media::id)
                .containsExactly(first.id(), second.id());
        assertThat(media.findByBusinessObject(LinkType.HELP_REQUEST, HELP_REQUEST_ID)).extracting(Media::id)
                .containsExactlyInAnyOrder(second.id(), other.id());
        assertThat(media.findByBusinessObject(LinkType.HELP, RECIPE_ID)).isEmpty();
    }

    @Test
    void removesAMediaWithItsLinks() {
        Media uploaded = Media.upload(COOK, PNG, List.of(RECIPE), NOW);
        media.add(uploaded);

        media.remove(uploaded.id());

        assertThat(media.findById(uploaded.id())).isEmpty();
        assertThat(media.findByBusinessObject(LinkType.RECIPE, RECIPE_ID)).isEmpty();
        assertThat(media.findById(MediaId.newId())).isEmpty();
    }
}
