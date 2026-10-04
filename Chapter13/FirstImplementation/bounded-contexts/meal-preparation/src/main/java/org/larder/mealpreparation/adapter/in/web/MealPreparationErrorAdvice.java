package org.larder.mealpreparation.adapter.in.web;

import org.larder.mealpreparation.application.NotFoundException;
import org.larder.mealpreparation.application.NotPermittedException;
import org.larder.mealpreparation.application.RecipeCatalogUnavailableException;
import org.larder.mealpreparation.application.UnknownRecipeException;
import org.larder.mealpreparation.domain.MealPreparationRuleViolationException;
import org.larder.platform.web.ApiError;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Maps the exceptions of this context to the contract's status codes (no 409 in the contract). */
@RestControllerAdvice(basePackageClasses = MealPreparationErrorAdvice.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
class MealPreparationErrorAdvice {

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiError> notFound(NotFoundException e) {
        return error(HttpStatus.NOT_FOUND, "NOT_FOUND", e);
    }

    @ExceptionHandler(NotPermittedException.class)
    ResponseEntity<ApiError> notPermitted(NotPermittedException e) {
        return error(HttpStatus.FORBIDDEN, "NOT_PERMITTED", e);
    }

    @ExceptionHandler(UnknownRecipeException.class)
    ResponseEntity<ApiError> unknownRecipe(UnknownRecipeException e) {
        return error(HttpStatus.BAD_REQUEST, "UNKNOWN_RECIPE", e);
    }

    /** A recipe without steps, a stale step id, next on the last or previous on the first step. */
    @ExceptionHandler(MealPreparationRuleViolationException.class)
    ResponseEntity<ApiError> ruleViolated(MealPreparationRuleViolationException e) {
        return error(HttpStatus.BAD_REQUEST, e.code(), e);
    }

    /** The contract's 500 "Service unavailable". */
    @ExceptionHandler(RecipeCatalogUnavailableException.class)
    ResponseEntity<ApiError> upstreamUnavailable(RecipeCatalogUnavailableException e) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "UPSTREAM_UNAVAILABLE", e);
    }

    private static ResponseEntity<ApiError> error(HttpStatus status, String code, RuntimeException e) {
        return ResponseEntity.status(status).body(ApiError.of(code, e.getMessage()));
    }
}
