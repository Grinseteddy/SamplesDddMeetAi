package org.larder.recipecatalog.application;

/** The caller changes a recipe that is not their own. */
public class NotPermittedException extends RuntimeException {

    public NotPermittedException(String message) {
        super(message);
    }
}
