package ro.unibuc.hello.utils;

import org.springframework.http.ResponseEntity;
import ro.unibuc.hello.dto.ErrorString;

import java.util.function.Consumer;
import java.util.regex.Pattern;

import static ro.unibuc.hello.utils.ResponseUtils.badRequest;

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

    public static ResponseEntity<ErrorString> exists(String fieldName, String value) {
        return value == null
                ? badRequest(missingFieldTemplate, fieldName)
                : null;
    }

    public static <T> T fallback(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    public static <T> ResponseEntity<ErrorString> validate(String fieldName, T field) {
        ResponseEntity<ErrorString> error;

        switch (field) {
            case null -> error = null;
            case String s when s.isBlank() -> error = badRequest(emptyFieldTemplate, fieldName);
            case Integer i when i < 0 -> error = badRequest(negativeFieldTemplate, fieldName);
            case Double v when v < 0 -> error = badRequest(negativeFieldTemplate, fieldName);
            default -> error = null;
        };

        return error;
    }

    public static <T> ResponseEntity<ErrorString> validate(String fieldName, T field, ValidationRule<T> validator) {
        if (field == null) {
            return null;
        }
        String errorMessage = validator.validate(field);
        if (errorMessage != null) {
            return badRequest(errorMessage, fieldName);
        }
        return null;
    }

    public static <R> ResponseEntity<ErrorString> validateAndUpdate(String fieldName, Consumer<R> setter, R fieldValue, ValidationRule<R> validator) {
        ResponseEntity<ErrorString> err = validator == null
                ? validate(fieldName, fieldValue)
                : validate(fieldName, fieldValue, validator);

        if (err != null) return err;
        if (fieldValue != null) setter.accept(fieldValue);
        return null;
    }

    public static <R> ResponseEntity<ErrorString> validateAndUpdate(String fieldName, Consumer<R> setter, R fieldValue) {
        return validateAndUpdate(fieldName, setter, fieldValue, null);
    }

    @SuppressWarnings("unchecked")
    public static ResponseEntity<ErrorString> chain(ResponseEntity<ErrorString>... responses) {
        for (ResponseEntity<ErrorString> response : responses) {
            if (response != null) {
                return response;
            }
        }
        return null;
    }

    private static boolean failsRegex(String regex, String value) {
        return !Pattern.compile(regex).matcher(value).matches();
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
