package org.larder.platform.web;

import java.lang.reflect.Method;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

/**
 * Converts query and path parameters to the enums generated from the OpenAPI contracts.
 * The contracts use their own spelling (e.g. {@code helpRequest}), which the generator
 * maps in a static {@code fromValue(String)} method; Spring's default only knows the
 * Java constant name ({@code HELP_REQUEST}). Enums without {@code fromValue} fall back
 * to the constant name.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
final class ContractEnumConverterFactory implements ConverterFactory<String, Enum> {

    @Override
    public <T extends Enum> Converter<String, T> getConverter(Class<T> targetType) {
        Method fromValue = findFromValue(targetType);
        return source -> {
            if (source.isEmpty()) {
                return null;
            }
            if (fromValue != null) {
                try {
                    return (T) fromValue.invoke(null, source);
                } catch (ReflectiveOperationException e) {
                    throw new IllegalArgumentException("Unexpected value '" + source + "' for " + targetType.getSimpleName(), e);
                }
            }
            return (T) Enum.valueOf(targetType, source.trim());
        };
    }

    private static Method findFromValue(Class<?> enumType) {
        try {
            return enumType.getMethod("fromValue", String.class);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
