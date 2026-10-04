package org.larder.notification.adapter.in.web;

import org.larder.notification.application.NotFoundException;
import org.larder.notification.application.NotPermittedException;
import org.larder.notification.domain.NotAReceiverException;
import org.larder.notification.domain.NotificationDeletedException;
import org.larder.platform.web.ApiError;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Maps the exceptions of this context to the contract's status codes. */
@RestControllerAdvice(basePackageClasses = NotificationErrorAdvice.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
class NotificationErrorAdvice {

    @ExceptionHandler({NotFoundException.class, NotificationDeletedException.class})
    ResponseEntity<ApiError> notFound(RuntimeException e) {
        return error(HttpStatus.NOT_FOUND, "NOT_FOUND", e);
    }

    @ExceptionHandler({NotPermittedException.class, NotAReceiverException.class})
    ResponseEntity<ApiError> notPermitted(RuntimeException e) {
        return error(HttpStatus.FORBIDDEN, "NOT_PERMITTED", e);
    }

    private static ResponseEntity<ApiError> error(HttpStatus status, String code, RuntimeException e) {
        return ResponseEntity.status(status).body(ApiError.of(code, e.getMessage()));
    }
}
