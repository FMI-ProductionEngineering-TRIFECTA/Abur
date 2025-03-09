package ro.unibuc.hello.utils;

import ro.unibuc.hello.exception.ValidationException;

import java.util.function.Consumer;
import java.util.regex.Pattern;

public class ValidationUtils {
    private static final String missingFieldTemplate = "%s is required";
    private static final String emptyFieldTemplate = "%s cannot be empty!";
    private static final String negativeFieldTemplate = "%s cannot be negative!";

    @FunctionalInterface
    public interface ValidationRule<T> {
        String validate(T value);

        default ValidationRule<T> and(ValidationRule<T> other) {
            return value -> {
                String error = this.validate(value);
                if (error != null) {
                    return error;
                }
                return other.validate(value);
            };
        }
    }

    public static void exists(String fieldName, String value) {
        if (value == null) {
            throw new ValidationException(missingFieldTemplate, fieldName);
        }
    }

    public static <T> T fallback(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    public static <T> void validate(String fieldName, T field) {
        if (field instanceof String s && s.trim().isEmpty()) {
            throw new ValidationException(emptyFieldTemplate, fieldName);
        }
        if (field instanceof Integer i && i < 0) {
            throw new ValidationException(negativeFieldTemplate, fieldName);
        }
        if (field instanceof Double v && v < 0) {
            throw new ValidationException(negativeFieldTemplate, fieldName);
        }
    }

    public static <T> void validate(String fieldName, T field, ValidationRule<T> validator) {
        if (field == null) {
            return;
        }
        String errorMessage = validator.validate(field);
        if (errorMessage != null) {
            throw new ValidationException(errorMessage, fieldName);
        }
    }

    public static <R> void validateAndUpdate(String fieldName, Consumer<R> setter, R fieldValue, ValidationRule<R> validator) {
        if (validator == null) {
            validate(fieldName, fieldValue);
        }
        else {
            validate(fieldName, fieldValue, validator);
        }

        if (fieldValue != null) setter.accept(fieldValue);
    }

    public static <R> void validateAndUpdate(String fieldName, Consumer<R> setter, R fieldValue) {
        validateAndUpdate(fieldName, setter, fieldValue, null);
    }

    private static boolean failsRegex(String regex, String value) {
        return !Pattern.compile(regex).matcher(value).find();
    }

    public static ValidationRule<String> validLength(int min) {
        return value -> value.length() < min
                ? "%s must be at least " + min + " characters long!"
                : null;
    }

    public static ValidationRule<String> validLength(int min, int max) {
        return value -> value.length() < min || value.length() > max
                ? "%s must be between " + min + " and " + max + " characters long!"
                : null;
    }

    public static ValidationRule<String> validPassword() {
        return value -> {
            if (failsRegex("[A-Z]", value)) {
                return "%s must contain at least one uppercase letter!";
            }
            if (failsRegex("[0-9]", value)) {
                return "%s must contain at least one digit!";
            }
            return null;
        };
    }

    public static ValidationRule<String> validEmail() {
        return value -> failsRegex("^[A-Za-z0-9+_.-]+@(.+)$", value)
                ? "%s must be a valid email address!"
                : null;
    }

    public static ValidationRule<String> validWebsite() {
        return value -> failsRegex("^(https?|ftp)://[^\\s/$.?#].[^\\s]*$", value)
                ? "%s must be a valid website URL!"
                : null;
    }
}
