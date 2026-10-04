package org.larder.platform.web;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/** The {@code Error} body shared by all contracts: {@code code}, {@code message}, optional {@code details}. */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiError(String code, String message, Map<String, Object> details) {

    public static ApiError of(String code, String message) {
        return new ApiError(code, message, Map.of());
    }
}
