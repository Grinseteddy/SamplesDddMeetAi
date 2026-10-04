package org.larder.cookingassistance.adapter.in.web;

import org.larder.cookingassistance.application.NotFoundException;
import org.larder.cookingassistance.application.NotPermittedException;
import org.larder.cookingassistance.application.UnknownHelpRequestException;
import org.larder.cookingassistance.domain.HelpRuleViolationException;
import org.larder.platform.web.ApiError;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Maps the exceptions of this context to the contract's status codes (no 409 in the contract). */
@RestControllerAdvice(basePackageClasses = CookingAssistanceErrorAdvice.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
class CookingAssistanceErrorAdvice {

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiError> notFound(NotFoundException e) {
        return error(HttpStatus.NOT_FOUND, "NOT_FOUND", e);
    }

    @ExceptionHandler(NotPermittedException.class)
    ResponseEntity<ApiError> notPermitted(NotPermittedException e) {
        return error(HttpStatus.FORBIDDEN, "NOT_PERMITTED", e);
    }

    /** createHelp has no 404 in the contract: an unknown help request in the body is a bad request. */
    @ExceptionHandler(UnknownHelpRequestException.class)
    ResponseEntity<ApiError> unknownHelpRequest(UnknownHelpRequestException e) {
        return error(HttpStatus.BAD_REQUEST, "UNKNOWN_HELP_REQUEST", e);
    }

    /** An invariant of help requests or helps would be broken, e.g. Chef for a catastrophe. */
    @ExceptionHandler(HelpRuleViolationException.class)
    ResponseEntity<ApiError> ruleViolated(HelpRuleViolationException e) {
        return error(HttpStatus.BAD_REQUEST, e.code(), e);
    }

    private static ResponseEntity<ApiError> error(HttpStatus status, String code, RuntimeException e) {
        return ResponseEntity.status(status).body(ApiError.of(code, e.getMessage()));
    }
}
