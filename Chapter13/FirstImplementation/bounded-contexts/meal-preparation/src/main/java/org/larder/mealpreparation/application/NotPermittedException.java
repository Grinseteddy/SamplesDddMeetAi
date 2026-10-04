package org.larder.mealpreparation.application;

/**
 * The caller acts on a meal preparation another cook started, or Recipe Catalog refuses to show the
 * caller the recipe.
 */
public class NotPermittedException extends RuntimeException {

    public NotPermittedException(String message) {
        super(message);
    }
}
