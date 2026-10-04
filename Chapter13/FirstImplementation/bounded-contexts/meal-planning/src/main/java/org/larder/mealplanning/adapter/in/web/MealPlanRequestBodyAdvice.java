package org.larder.mealplanning.adapter.in.web;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.larder.mealplanning.adapter.in.web.model.MealPlanCreate;
import org.larder.mealplanning.adapter.in.web.model.MealPlanUpdate;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

/**
 * Tells absent properties of the meal plan request bodies from properties given as {@code null}, which
 * the generated models (built with {@code openApiNullable=false}) cannot:
 *
 * <ul>
 *   <li>An absent {@code courses} stays absent. The generated models initialise it with an empty list,
 *       which bean validation ({@code minItems: 1}) would reject, so every plan without courses would be
 *       a bad request. An explicitly given {@code []} still is one.</li>
 *   <li>{@code howToServe: null} in a change removes the serving instructions; the property names given
 *       in the change are kept for the controller ({@link #givenProperties()}).</li>
 *   <li>{@code null} for any other property is a bad request - the contract does not allow it.</li>
 *   <li>A change needs at least one property of the meal plan ({@code minProperties: 1}).</li>
 * </ul>
 *
 * Runs before bean validation of the body.
 */
@ControllerAdvice(basePackageClasses = MealPlanRequestBodyAdvice.class)
class MealPlanRequestBodyAdvice implements RequestBodyAdvice {

    static final String GIVEN_PROPERTIES = MealPlanRequestBodyAdvice.class.getName() + ".givenProperties";

    private static final List<String> PROPERTIES = List.of("occasion", "servings", "meal", "howToServe", "courses");
    private static final Set<String> NULLABLE_IN_CHANGE = Set.of("howToServe");

    private final ObjectMapper objectMapper;

    MealPlanRequestBodyAdvice(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /** The meal plan properties given in the body of the current change request. */
    static Set<String> givenProperties() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        Object given = attributes == null ? null : attributes.getAttribute(GIVEN_PROPERTIES, RequestAttributes.SCOPE_REQUEST);
        @SuppressWarnings("unchecked")
        Set<String> properties = given instanceof Set<?> set ? (Set<String>) set : Set.of();
        return properties;
    }

    @Override
    public boolean supports(MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return targetType == MealPlanCreate.class || targetType == MealPlanUpdate.class;
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
                                           Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        byte[] body = inputMessage.getBody().readAllBytes();
        JsonNode json;
        try {
            json = body.length == 0 ? null : objectMapper.readTree(body);
        } catch (JsonProcessingException e) {
            json = null; // the converter reports the malformed body
        }
        return new ReadBody(inputMessage.getHeaders(), body, json);
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
                                Class<? extends HttpMessageConverter<?>> converterType) {
        if (!(inputMessage instanceof ReadBody read) || read.json() == null || !read.json().isObject()) {
            return body;
        }
        boolean change = body instanceof MealPlanUpdate;
        Set<String> given = new HashSet<>();
        for (String property : PROPERTIES) {
            JsonNode value = read.json().get(property);
            if (value == null) {
                continue;
            }
            if (value.isNull() && !(change && NULLABLE_IN_CHANGE.contains(property))) {
                throw new HttpMessageNotReadableException("Property '" + property + "' must not be null", inputMessage);
            }
            given.add(property);
        }
        if (!given.contains("courses")) {
            if (body instanceof MealPlanCreate create) {
                create.setCourses(null);
            } else if (body instanceof MealPlanUpdate update) {
                update.setCourses(null);
            }
        }
        if (change) {
            if (given.isEmpty()) {
                throw new HttpMessageNotReadableException(
                        "A change needs at least one of the properties " + PROPERTIES, inputMessage);
            }
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                attributes.setAttribute(GIVEN_PROPERTIES, Set.copyOf(given), RequestAttributes.SCOPE_REQUEST);
            }
        }
        return body;
    }

    @Override
    public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
                                  Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    /** The body read once, kept for the converter, plus its JSON tree. */
    private record ReadBody(HttpHeaders headers, byte[] bytes, JsonNode json) implements HttpInputMessage {

        @Override
        public InputStream getBody() {
            return new ByteArrayInputStream(bytes);
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }
    }
}
