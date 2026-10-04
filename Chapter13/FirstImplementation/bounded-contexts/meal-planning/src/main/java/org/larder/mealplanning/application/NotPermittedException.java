package org.larder.mealplanning.application;

/** The caller acts on a meal plan that is not their own, or an upstream refuses the caller. */
public class NotPermittedException extends RuntimeException {

    public NotPermittedException(String message) {
        super(message);
    }
}
