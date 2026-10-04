package org.larder.cookprofile.adapter.in.web;

import org.larder.cookprofile.application.AlreadyRegisteredException;
import org.larder.cookprofile.application.NotFoundException;
import org.larder.cookprofile.application.NotPermittedException;
import org.larder.cookprofile.domain.InvalidCookException;
import org.larder.platform.web.ApiError;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Maps the exceptions of this context to the contract's status codes (no 409 in the contract). */
@RestControllerAdvice(basePackageClasses = CookProfileErrorAdvice.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
class CookProfileErrorAdvice {

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiError> notFound(NotFoundException e) {
        return error(HttpStatus.NOT_FOUND, "NOT_FOUND", e);
    }

    @ExceptionHandler(NotPermittedException.class)
    ResponseEntity<ApiError> notPermitted(NotPermittedException e) {
        return error(HttpStatus.FORBIDDEN, "NOT_PERMITTED", e);
    }

    @ExceptionHandler(AlreadyRegisteredException.class)
    ResponseEntity<ApiError> alreadyRegistered(AlreadyRegisteredException e) {
        return error(HttpStatus.BAD_REQUEST, "ALREADY_REGISTERED", e);
    }

    @ExceptionHandler(InvalidCookException.class)
    ResponseEntity<ApiError> invalidCook(InvalidCookException e) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_COOK", e);
    }

    private static ResponseEntity<ApiError> error(HttpStatus status, String code, RuntimeException e) {
        return ResponseEntity.status(status).body(ApiError.of(code, e.getMessage()));
    }
}
