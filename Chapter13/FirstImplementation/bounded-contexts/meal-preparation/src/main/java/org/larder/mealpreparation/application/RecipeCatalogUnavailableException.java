package org.larder.mealpreparation.application;

/** Recipe Catalog cannot be reached or answers with an error this context cannot act on. */
public class RecipeCatalogUnavailableException extends RuntimeException {

    public RecipeCatalogUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
