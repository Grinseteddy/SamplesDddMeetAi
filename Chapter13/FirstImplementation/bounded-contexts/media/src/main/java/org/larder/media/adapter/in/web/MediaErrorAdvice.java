package org.larder.media.adapter.in.web;

import org.larder.media.application.ImageStoreException;
import org.larder.media.application.NotFoundException;
import org.larder.media.application.NotPermittedException;
import org.larder.media.domain.InvalidImageException;
import org.larder.media.domain.InvalidLinkException;
import org.larder.platform.web.ApiError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Maps the exceptions of this context to the contract's status codes (no 409 in the contract). */
@RestControllerAdvice(basePackageClasses = MediaErrorAdvice.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
class MediaErrorAdvice {

    private static final Logger LOG = LoggerFactory.getLogger(MediaErrorAdvice.class);

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiError> notFound(NotFoundException e) {
        return error(HttpStatus.NOT_FOUND, "NOT_FOUND", e);
    }

    @ExceptionHandler(NotPermittedException.class)
    ResponseEntity<ApiError> notPermitted(NotPermittedException e) {
        return error(HttpStatus.FORBIDDEN, "NOT_PERMITTED", e);
    }

    @ExceptionHandler(InvalidImageException.class)
    ResponseEntity<ApiError> invalidImage(InvalidImageException e) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_IMAGE", e);
    }

    @ExceptionHandler(InvalidLinkException.class)
    ResponseEntity<ApiError> invalidLink(InvalidLinkException e) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_LINK", e);
    }

    /** The contract's 500 "Service unavailable": the bucket cannot be reached. */
    @ExceptionHandler(ImageStoreException.class)
    ResponseEntity<ApiError> storageUnavailable(ImageStoreException e) {
        LOG.error("Image storage failed", e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE_UNAVAILABLE", e);
    }

    private static ResponseEntity<ApiError> error(HttpStatus status, String code, RuntimeException e) {
        return ResponseEntity.status(status).body(ApiError.of(code, e.getMessage()));
    }
}
