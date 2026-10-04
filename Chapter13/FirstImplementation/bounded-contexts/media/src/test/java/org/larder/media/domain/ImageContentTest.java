package org.larder.media.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class ImageContentTest {

    @Test
    void recognisesTheAcceptedImageFormats() {
        assertThat(ImageContent.of(bytes(0xFF, 0xD8, 0xFF, 0xE0, 0x00)).format()).isEqualTo(ImageFormat.JPEG);
        assertThat(ImageContent.of(bytes(0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0x00)).format()).isEqualTo(ImageFormat.PNG);
        assertThat(ImageContent.of("GIF89a....".getBytes(StandardCharsets.US_ASCII)).format()).isEqualTo(ImageFormat.GIF);
        assertThat(ImageContent.of("RIFF\0\0\0\0WEBPVP8 ".getBytes(StandardCharsets.US_ASCII)).format()).isEqualTo(ImageFormat.WEBP);
        assertThat(ImageFormat.WEBP.contentType()).isEqualTo("image/webp");
    }

    @Test
    void anImageMustNotBeEmpty() {
        assertThatThrownBy(() -> ImageContent.of(new byte[0])).isInstanceOf(InvalidImageException.class);
    }

    @Test
    void otherContentIsNoImage() {
        assertThatThrownBy(() -> ImageContent.of("%PDF-1.7".getBytes(StandardCharsets.US_ASCII)))
                .isInstanceOf(InvalidImageException.class);
    }

    @Test
    void anImageIsAtMostFiveMebibytes() {
        byte[] largest = new byte[ImageContent.MAX_BYTES];
        largest[0] = (byte) 0xFF;
        largest[1] = (byte) 0xD8;
        largest[2] = (byte) 0xFF;
        assertThat(ImageContent.of(largest).size()).isEqualTo(ImageContent.MAX_BYTES);

        byte[] tooLarge = new byte[ImageContent.MAX_BYTES + 1];
        System.arraycopy(largest, 0, tooLarge, 0, 3);
        assertThatThrownBy(() -> ImageContent.of(tooLarge)).isInstanceOf(InvalidImageException.class);
    }

    private static byte[] bytes(int... values) {
        byte[] bytes = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            bytes[i] = (byte) values[i];
        }
        return bytes;
    }
}
