package org.larder.sharing.application;

import org.larder.sharing.domain.MediaId;

/**
 * Port to Media: whether an image exists. {@link NotPermittedException} when Media refuses the caller;
 * {@link UpstreamUnavailableException} when it cannot answer.
 */
public interface Pictures {

    boolean exists(MediaId image);
}
