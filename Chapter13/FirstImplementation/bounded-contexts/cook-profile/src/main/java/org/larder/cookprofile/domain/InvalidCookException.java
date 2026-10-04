package org.larder.cookprofile.domain;

/** A cook or a change of a cook breaks a rule of the domain (a broken request). */
public class InvalidCookException extends RuntimeException {

    public InvalidCookException(String message) {
        super(message);
    }
}
