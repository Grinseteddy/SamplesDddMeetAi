package org.larder.grandmaavatar.domain;

/** Length rules of the published language for free texts. */
final class Texts {

    private Texts() {
    }

    /** Grandma's own texts: never blank, never longer than the contract allows. */
    static String answerText(String text, String what, int maxLength) {
        if (text == null || text.isBlank()) {
            throw new InvalidAnswerException(what + " must not be blank");
        }
        if (text.length() > maxLength) {
            throw new InvalidAnswerException(what + " must not be longer than " + maxLength + " characters");
        }
        return text;
    }

    /** Texts of a cook: exactly the contract's rule (1..maxLength characters), nothing stricter. */
    static String requestText(String text, String what, int maxLength) {
        if (text == null || text.isEmpty()) {
            throw new InvalidHelpRequestException(what + " must not be empty");
        }
        if (text.length() > maxLength) {
            throw new InvalidHelpRequestException(what + " must not be longer than " + maxLength + " characters");
        }
        return text;
    }
}
