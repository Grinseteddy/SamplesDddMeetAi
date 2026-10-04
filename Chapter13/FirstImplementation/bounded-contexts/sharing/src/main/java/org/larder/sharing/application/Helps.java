package org.larder.sharing.application;

import java.util.Optional;

import org.larder.sharing.domain.Help;
import org.larder.sharing.domain.HelpId;

/**
 * Port to Cooking Assistance: the helps thanks are given for, read with the caller's rights.
 * Empty when the help does not exist; {@link NotPermittedException} when Cooking Assistance does not
 * show it to the caller; {@link UpstreamUnavailableException} when it cannot answer.
 */
public interface Helps {

    Optional<Help> find(HelpId id);
}
