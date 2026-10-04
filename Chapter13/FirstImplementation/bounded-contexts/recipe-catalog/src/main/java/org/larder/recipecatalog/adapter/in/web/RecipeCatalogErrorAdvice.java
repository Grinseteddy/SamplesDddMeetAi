package org.larder.recipecatalog.adapter.in.web;

import org.larder.platform.web.ApiError;
import org.larder.recipecatalog.application.NotFoundException;
import org.larder.recipecatalog.application.NotPermittedException;
import org.larder.recipecatalog.domain.RecipeRuleViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Maps the exceptions of this context to the contract's status codes (no 409 in the contract). */
@RestControllerAdvice(basePackageClasses = RecipeCatalogErrorAdvice.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
class RecipeCatalogErrorAdvice {

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiError> notFound(NotFoundException e) {
        return error(HttpStatus.NOT_FOUND, "NOT_FOUND", e);
    }

    @ExceptionHandler(NotPermittedException.class)
    ResponseEntity<ApiError> notPermitted(NotPermittedException e) {
        return error(HttpStatus.FORBIDDEN, "NOT_PERMITTED", e);
    }

    /** A recipe rule would be broken, e.g. removing the last ingredient or a duplicate sequence number. */
    @ExceptionHandler(RecipeRuleViolationException.class)
    ResponseEntity<ApiError> ruleViolated(RecipeRuleViolationException e) {
        return error(HttpStatus.BAD_REQUEST, e.code(), e);
    }

    private static ResponseEntity<ApiError> error(HttpStatus status, String code, RuntimeException e) {
        return ResponseEntity.status(status).body(ApiError.of(code, e.getMessage()));
    }
}
