package org.larder.mealplanning.application;

/** The Recipe Catalog cannot answer (unreachable, failing or answering outside its contract). */
public class RecipeCatalogUnavailableException extends RuntimeException {

    public RecipeCatalogUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
