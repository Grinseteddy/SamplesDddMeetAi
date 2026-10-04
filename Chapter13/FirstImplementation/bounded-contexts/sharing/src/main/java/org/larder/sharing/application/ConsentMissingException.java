package org.larder.sharing.application;

import org.larder.sharing.domain.CookId;

/** A cook has no consent in force that the thanks need. */
public class ConsentMissingException extends RuntimeException {

    public static final String MENTION_WITHOUT_CONSENT = "MENTION_WITHOUT_CONSENT";
    public static final String PICTURE_WITHOUT_CONSENT = "PICTURE_WITHOUT_CONSENT";

    private final String code;

    private ConsentMissingException(String code, String message) {
        super(message);
        this.code = code;
    }

    static ConsentMissingException toBeMentioned(CookId cook) {
        return new ConsentMissingException(MENTION_WITHOUT_CONSENT,
                "Cook " + cook.value() + " has not consented to being mentioned as helper in thanks");
    }

    static ConsentMissingException toSharePhotos(CookId giver) {
        return new ConsentMissingException(PICTURE_WITHOUT_CONSENT,
                "Cook " + giver.value() + " has not consented to the use of their photos in public thanks");
    }

    public String code() {
        return code;
    }
}
