package org.larder.sharing.adapter.in.web;

import org.larder.platform.web.ApiError;
import org.larder.sharing.application.ConcurrentChangeException;
import org.larder.sharing.application.ConsentMissingException;
import org.larder.sharing.application.NotFoundException;
import org.larder.sharing.application.NotPermittedException;
import org.larder.sharing.application.UnknownHelpException;
import org.larder.sharing.application.UnknownPictureException;
import org.larder.sharing.application.UpstreamUnavailableException;
import org.larder.sharing.domain.ThanksRuleViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Maps the exceptions of this context to the contract's status codes (no 409 in the contract). */
@RestControllerAdvice(basePackageClasses = SharingErrorAdvice.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
class SharingErrorAdvice {

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiError> notFound(NotFoundException e) {
        return error(HttpStatus.NOT_FOUND, "NOT_FOUND", e);
    }

    @ExceptionHandler(NotPermittedException.class)
    ResponseEntity<ApiError> notPermitted(NotPermittedException e) {
        return error(HttpStatus.FORBIDDEN, "NOT_PERMITTED", e);
    }

    @ExceptionHandler(UnknownHelpException.class)
    ResponseEntity<ApiError> unknownHelp(UnknownHelpException e) {
        return error(HttpStatus.BAD_REQUEST, "UNKNOWN_HELP", e);
    }

    @ExceptionHandler(UnknownPictureException.class)
    ResponseEntity<ApiError> unknownPicture(UnknownPictureException e) {
        return error(HttpStatus.BAD_REQUEST, "UNKNOWN_PICTURE", e);
    }

    /** A mentioned cook or the giver of a picture has not consented. */
    @ExceptionHandler(ConsentMissingException.class)
    ResponseEntity<ApiError> consentMissing(ConsentMissingException e) {
        return error(HttpStatus.BAD_REQUEST, e.code(), e);
    }

    /** Recipient rules, text, picture link, thanks already given for the help. */
    @ExceptionHandler(ThanksRuleViolationException.class)
    ResponseEntity<ApiError> ruleViolated(ThanksRuleViolationException e) {
        return error(HttpStatus.BAD_REQUEST, e.code(), e);
    }

    /** The contract has no 409; a lost race is a request to repeat. */
    @ExceptionHandler(ConcurrentChangeException.class)
    ResponseEntity<ApiError> concurrentChange(ConcurrentChangeException e) {
        return error(HttpStatus.BAD_REQUEST, "THANKS_CHANGED_CONCURRENTLY", e);
    }

    /** The contract's 500 "Service unavailable". */
    @ExceptionHandler(UpstreamUnavailableException.class)
    ResponseEntity<ApiError> upstreamUnavailable(UpstreamUnavailableException e) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "UPSTREAM_UNAVAILABLE", e);
    }

    private static ResponseEntity<ApiError> error(HttpStatus status, String code, RuntimeException e) {
        return ResponseEntity.status(status).body(ApiError.of(code, e.getMessage()));
    }
}
