package org.larder.media.domain;

import java.util.Arrays;

/**
 * The bytes of an image (value object).
 *
 * <ul>
 *   <li>An image is not empty and at most {@value #MAX_BYTES} bytes (5 MiB) large.</li>
 *   <li>It is a JPEG, PNG, GIF or WebP image, recognised by its leading bytes.</li>
 * </ul>
 */
public final class ImageContent {

    public static final int MAX_BYTES = 5 * 1024 * 1024;

    private final byte[] bytes;
    private final ImageFormat format;

    private ImageContent(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw new InvalidImageException("An image must not be empty");
        }
        if (bytes.length > MAX_BYTES) {
            throw new InvalidImageException("An image must not be larger than " + MAX_BYTES + " bytes");
        }
        this.bytes = bytes.clone();
        this.format = ImageFormat.detect(bytes).orElseThrow(
                () -> new InvalidImageException("Only JPEG, PNG, GIF and WebP images are accepted"));
    }

    public static ImageContent of(byte[] bytes) {
        return new ImageContent(bytes);
    }

    public byte[] bytes() {
        return bytes.clone();
    }

    public ImageFormat format() {
        return format;
    }

    public long size() {
        return bytes.length;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ImageContent other && Arrays.equals(bytes, other.bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    public String toString() {
        return "ImageContent[" + format + ", " + bytes.length + " bytes]";
    }
}
