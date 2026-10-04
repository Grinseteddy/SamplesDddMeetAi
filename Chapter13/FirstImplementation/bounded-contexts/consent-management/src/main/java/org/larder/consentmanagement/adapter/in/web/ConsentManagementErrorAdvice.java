package org.larder.consentmanagement.adapter.in.web;

import org.larder.consentmanagement.application.NotFoundException;
import org.larder.consentmanagement.application.NotPermittedException;
import org.larder.consentmanagement.application.UnknownConsentTextException;
import org.larder.consentmanagement.domain.ConsentAlreadyRevokedException;
import org.larder.platform.web.ApiError;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Maps the exceptions of this context to the contract's status codes (no 409 in the contract). */
@RestControllerAdvice(basePackageClasses = ConsentManagementErrorAdvice.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
class ConsentManagementErrorAdvice {

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiError> notFound(NotFoundException e) {
        return error(HttpStatus.NOT_FOUND, "NOT_FOUND", e);
    }

    @ExceptionHandler(NotPermittedException.class)
    ResponseEntity<ApiError> notPermitted(NotPermittedException e) {
        return error(HttpStatus.FORBIDDEN, "NOT_PERMITTED", e);
    }

    @ExceptionHandler(UnknownConsentTextException.class)
    ResponseEntity<ApiError> unknownText(UnknownConsentTextException e) {
        return error(HttpStatus.BAD_REQUEST, "UNKNOWN_CONSENT_TEXT", e);
    }

    @ExceptionHandler(ConsentAlreadyRevokedException.class)
    ResponseEntity<ApiError> alreadyRevoked(ConsentAlreadyRevokedException e) {
        return error(HttpStatus.BAD_REQUEST, "CONSENT_ALREADY_REVOKED", e);
    }

    private static ResponseEntity<ApiError> error(HttpStatus status, String code, RuntimeException e) {
        return ResponseEntity.status(status).body(ApiError.of(code, e.getMessage()));
    }
}
