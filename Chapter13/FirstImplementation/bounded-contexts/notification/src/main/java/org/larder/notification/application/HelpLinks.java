package org.larder.notification.application;

import java.net.URI;
import java.util.UUID;

/** Port: where a Cook finds a Help in Cooking Assistance. */
public interface HelpLinks {

    URI helpLink(UUID helpId);
}
