package org.larder.sharing.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.sharing.TestData.IMAGE;
import static org.larder.sharing.TestData.IMAGE_LINK;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PictureTest {

    @Test
    void theMediaIdIsTheLastSegmentOfAnImageLink() {
        Picture picture = Picture.of(IMAGE_LINK);

        assertThat(picture.mediaId()).isEqualTo(IMAGE);
        assertThat(picture.link()).isEqualTo(IMAGE_LINK);
    }

    @Test
    void theHostIsTheDeploymentsBusiness() {
        assertThat(Picture.of(URI.create("http://localhost:8080/media/images/" + IMAGE.value())).mediaId())
                .isEqualTo(IMAGE);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://larder.org/media/recipes/b009a5d1-0205-4b0c-af82-822229cf243a",
            "https://larder.org/media/images/not-a-uuid",
            "https://larder.org/media/images/",
            "https://larder.org/media/images/b009a5d1-0205-4b0c-af82-822229cf243a?size=small",
            "ftp://larder.org/media/images/b009a5d1-0205-4b0c-af82-822229cf243a",
            "/media/images/b009a5d1-0205-4b0c-af82-822229cf243a",
            "urn:uuid:b009a5d1-0205-4b0c-af82-822229cf243a"})
    void aLinkThatIsNoImageOfMediaIsRejected(String link) {
        assertThatThrownBy(() -> Picture.of(URI.create(link)))
                .isInstanceOf(ThanksRuleViolationException.class)
                .extracting(e -> ((ThanksRuleViolationException) e).code())
                .isEqualTo(ThanksRuleViolationException.INVALID_PICTURE);
    }

    @Test
    void thanksNeedAPicture() {
        assertThatThrownBy(() -> Picture.of(null)).isInstanceOf(ThanksRuleViolationException.class);
    }
}
