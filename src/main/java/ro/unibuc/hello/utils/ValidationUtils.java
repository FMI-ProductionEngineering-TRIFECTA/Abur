package ro.unibuc.hello.utils;

import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ro.unibuc.hello.dto.ErrorString;

import java.util.List;
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

    private static ResponseEntity<ErrorString> errorResponse(String template, String fieldName) {
        return new ResponseEntity<>(new ErrorString(String.format(template, fieldName)), HttpStatus.BAD_REQUEST);
    }

    private static boolean failsRegex(String regex, String value) {
        return !Pattern.compile(regex).matcher(value).matches();
    }

    public static <T> ResponseEntity<ErrorString> validate(T field, String fieldName) {
        ResponseEntity<ErrorString> error;

        switch (field) {
            case String s when s.isBlank() -> error = errorResponse(emptyFieldTemplate, fieldName);
            case Integer i when i < 0 -> error = errorResponse(negativeFieldTemplate, fieldName);
            case Double v when v < 0 -> error = errorResponse(negativeFieldTemplate, fieldName);
            default -> error = null;
        };

        return error;
    }

    public static <T> ResponseEntity<ErrorString> validate(T field, String fieldName, ValidationRule<T> validator) {
        if (field == null) {
            return null;
        }
        String errorMessage = validator.validate(field);
        if (errorMessage != null) {
            return errorResponse(errorMessage, fieldName);
        }
        return null;
    }

    public static ResponseEntity<ErrorString> exists(String fieldName, String value) {
        return value == null
            ? errorResponse(missingFieldTemplate, fieldName)
            : null;
    }

    public static <R> ResponseEntity<ErrorString> validateAndUpdate(String fieldName, Consumer<R> setter, R fieldValue, ValidationRule<R> validator) {
        ResponseEntity<ErrorString> err = validator == null
                ? validate(fieldValue, fieldName)
                : validate(fieldValue, fieldName, validator);

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
