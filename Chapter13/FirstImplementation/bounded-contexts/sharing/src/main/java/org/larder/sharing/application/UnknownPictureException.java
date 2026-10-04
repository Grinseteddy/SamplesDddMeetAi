package org.larder.sharing.application;

import org.larder.sharing.domain.Picture;

/** Thanks refer to an image Media does not know. */
public class UnknownPictureException extends RuntimeException {

    public UnknownPictureException(Picture picture) {
        super("Media has no image " + picture.mediaId().value() + " (" + picture.link() + ")");
    }
}
