package org.larder.cookprofile.application;

/**
 * A registration or change collides with an existing cook: the user is already a cook,
 * or the email address belongs to another cook. A broken request (the contract has no 409).
 */
public class AlreadyRegisteredException extends RuntimeException {

    public AlreadyRegisteredException(String message) {
        super(message);
    }
}
