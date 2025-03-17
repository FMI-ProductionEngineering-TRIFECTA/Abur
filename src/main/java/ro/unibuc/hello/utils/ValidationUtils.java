package ro.unibuc.hello.utils;

import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.exception.ValidationException;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public interface ValidationUtils {

    private static boolean failsRegex(String regex, String value) {
        return !Pattern.compile(regex).matcher(value).find();
    }

    private static ValidationRule<String> checkPresence(Supplier<List<GameEntity>> listSupplier, String collectionName, boolean shouldBeIn) {
        return gameId -> {
            boolean exists = listSupplier.get().stream().anyMatch(g -> g.getId().equals(gameId));
            if (exists == shouldBeIn) {
                return null;
            }
            return String.format("%s %s %s", "%s", shouldBeIn ? "is not in" : "already in", collectionName);
        };
    }

    @FunctionalInterface
    interface ValidationRule<T> {
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

    static void exists(String fieldName, String value) {
        if (value == null) {
            throw new ValidationException("%s is required", fieldName);
        }
    }

    static <T> T fallback(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    static <T> void validate(String fieldName, T field) {
        validate(fieldName, field, defaultValidator());
    }

    static <T> void validate(String fieldName, T field, ValidationRule<T> validator) {
        if (field == null) {
            return;
        }
        String errorMessage = validator.validate(field);
        if (errorMessage != null) {
            throw new ValidationException(errorMessage, fieldName);
        }
    }

    static <T> void validateAndUpdate(String fieldName, Consumer<T> setter, T fieldValue, ValidationRule<T> validator) {
        if (validator == null) {
            validator = defaultValidator();
        }

        validate(fieldName, fieldValue, validator);
        if (fieldValue != null) setter.accept(fieldValue);
    }

    static <T> void validateAndUpdate(String fieldName, Consumer<T> setter, T fieldValue) {
        validateAndUpdate(fieldName, setter, fieldValue, null);
    }

    static <T> ValidationRule<T> defaultValidator() {
        return value -> {
            if (value instanceof String s && s.trim().isEmpty()) {
                return "%s cannot be empty!";
            }
            if ((value instanceof Integer i && i < 0) || (value instanceof Double v && v < 0)) {
                return "%s cannot be negative!";
            }
            return null;
        };
    }

    static ValidationRule<String> validLength(int min) {
        return value -> value.length() < min
                ? "%s must be at least " + min + " characters long!"
                : null;
    }

    @SuppressWarnings("unused")
    static ValidationRule<String> validLength(int min, int max) {
        return value -> value.length() < min || value.length() > max
                ? "%s must be between " + min + " and " + max + " characters long!"
                : null;
    }

    static ValidationRule<String> validPassword() {
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

    static ValidationRule<String> validEmail() {
        return value -> failsRegex("^[A-Za-z0-9+_.-]+@(.+)$", value)
                ? "%s must be a valid email address!"
                : null;
    }

    static ValidationRule<String> validWebsite() {
        return value -> failsRegex("^(https?|ftp)://[^\\s/$.?#].[^\\s]*$", value)
                ? "%s must be a valid website URL!"
                : null;
    }

    static <T> ValidationRule<T> isUnique(Supplier<T> existsCheck) {
        return value -> existsCheck.get() != null
                ? "%s already exists!"
                : null;
    }

    static ValidationRule<String> isNotIn(Supplier<List<GameEntity>> listSupplier, String collectionName) {
        return checkPresence(listSupplier, collectionName, false);
    }

    static ValidationRule<String> isIn(Supplier<List<GameEntity>> listSupplier, String collectionName) {
        return checkPresence(listSupplier, collectionName, true);
    }

}
