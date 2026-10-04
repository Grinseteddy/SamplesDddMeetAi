package org.larder.cookingassistance.application;

import java.util.Objects;

import org.larder.cookingassistance.domain.Answer;
import org.larder.cookingassistance.domain.CookId;
import org.larder.cookingassistance.domain.HelpId;
import org.larder.cookingassistance.domain.HelpRequestId;

/**
 * The Grandma Avatar's answer to a help request, as received from its HelpProvided message.
 * {@code helpId} is the avatar's: receiving the same help twice must not store it twice.
 */
public record GrandmaHelp(HelpId helpId, HelpRequestId helpRequest, CookId helpRequester, String answerTitle,
                          Answer answer) {

    public GrandmaHelp {
        Objects.requireNonNull(helpId, "helpId must not be null");
        Objects.requireNonNull(helpRequest, "helpRequest must not be null");
        Objects.requireNonNull(helpRequester, "helpRequester must not be null");
    }
}
