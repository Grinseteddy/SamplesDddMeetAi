package org.larder.sharing.application;

import org.larder.sharing.domain.CookId;

/**
 * Narrows the list of thanks; {@code null} means "any".
 *
 * @param giver the cook who gave the thanks
 * @param mentionedCook a cook mentioned by a recipient of the thanks
 */
public record ThanksFilter(CookId giver, CookId mentionedCook) {

    public static ThanksFilter all() {
        return new ThanksFilter(null, null);
    }
}
