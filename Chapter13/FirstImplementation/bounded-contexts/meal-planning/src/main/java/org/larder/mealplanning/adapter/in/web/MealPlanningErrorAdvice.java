package org.larder.mealplanning.adapter.in.web;

import org.larder.mealplanning.application.NotFoundException;
import org.larder.mealplanning.application.NotPermittedException;
import org.larder.mealplanning.application.RecipeCatalogUnavailableException;
import org.larder.mealplanning.application.UnknownRecipeException;
import org.larder.mealplanning.domain.MealPlanRuleViolationException;
import org.larder.platform.web.ApiError;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Maps the exceptions of this context to the contract's status codes (no 409 in the contract). */
@RestControllerAdvice(basePackageClasses = MealPlanningErrorAdvice.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
class MealPlanningErrorAdvice {

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiError> notFound(NotFoundException e) {
        return error(HttpStatus.NOT_FOUND, "NOT_FOUND", e);
    }

    @ExceptionHandler(NotPermittedException.class)
    ResponseEntity<ApiError> notPermitted(NotPermittedException e) {
        return error(HttpStatus.FORBIDDEN, "NOT_PERMITTED", e);
    }

    /** A course refers to a recipe the Recipe Catalog does not know. */
    @ExceptionHandler(UnknownRecipeException.class)
    ResponseEntity<ApiError> unknownRecipe(UnknownRecipeException e) {
        return error(HttpStatus.BAD_REQUEST, "UNKNOWN_RECIPE", e);
    }

    /** A meal plan rule would be broken, or an unknown diet is searched for. */
    @ExceptionHandler(MealPlanRuleViolationException.class)
    ResponseEntity<ApiError> ruleViolated(MealPlanRuleViolationException e) {
        return error(HttpStatus.BAD_REQUEST, e.code(), e);
    }

    /** The contract's "500 Service not available". */
    @ExceptionHandler(RecipeCatalogUnavailableException.class)
    ResponseEntity<ApiError> upstreamUnavailable(RecipeCatalogUnavailableException e) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "UPSTREAM_UNAVAILABLE", e);
    }

    private static ResponseEntity<ApiError> error(HttpStatus status, String code, RuntimeException e) {
        return ResponseEntity.status(status).body(ApiError.of(code, e.getMessage()));
    }
}
