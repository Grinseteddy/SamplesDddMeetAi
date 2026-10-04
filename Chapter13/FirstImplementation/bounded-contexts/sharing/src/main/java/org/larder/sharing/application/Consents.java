package org.larder.sharing.application;

import org.larder.sharing.domain.CookId;

/**
 * Port to Consent Management: whether a cook has a consent in force (given and not revoked) for a purpose.
 * {@link NotPermittedException} when Consent Management refuses the caller;
 * {@link UpstreamUnavailableException} when it cannot answer.
 */
public interface Consents {

    boolean isInForce(CookId cook, ConsentPurpose purpose);
}
