package org.larder.sharing.domain;

/** Giving or changing thanks would break one of their rules; nothing is changed. */
public class ThanksRuleViolationException extends RuntimeException {

    public static final String INVALID_RECIPIENT = "INVALID_RECIPIENT";
    public static final String DUPLICATE_RECIPIENT = "DUPLICATE_RECIPIENT";
    public static final String RECIPIENT_NOT_HELPER = "RECIPIENT_NOT_HELPER";
    public static final String INVALID_THANKS_TEXT = "INVALID_THANKS_TEXT";
    public static final String INVALID_PICTURE = "INVALID_PICTURE";
    public static final String THANKS_ALREADY_GIVEN = "THANKS_ALREADY_GIVEN";

    private final String code;

    public ThanksRuleViolationException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
