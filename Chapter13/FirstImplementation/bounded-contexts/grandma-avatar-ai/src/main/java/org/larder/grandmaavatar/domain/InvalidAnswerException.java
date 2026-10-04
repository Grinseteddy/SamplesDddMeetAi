package org.larder.grandmaavatar.domain;

/**
 * An answer breaks an invariant of a Help - e.g. it is of another type than the request or refers to
 * another recipe. Whatever the advisor behind Grandma comes up with, such an answer never leaves her.
 */
public class InvalidAnswerException extends RuntimeException {

    public InvalidAnswerException(String message) {
        super(message);
    }
}
