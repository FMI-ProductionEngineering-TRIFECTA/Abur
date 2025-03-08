package ro.unibuc.hello.utils;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ValidationUtils {
    public static <T> boolean isValid(T field) {
        return switch (field) {
            case null -> false;
            case String s -> !s.isBlank();
            case Integer i -> i > 0;
            case List<?> list -> !list.isEmpty();
            case Double v -> v > 0;
            default -> true;
        };

    }

    public static <T> boolean isValid(T field, Predicate<T> validator) {
        return field != null && validator.test(field);
    }

    private static boolean matchesRegex(String regex, String value) {
        return Pattern.compile(regex).matcher(value).matches();
    }

    public static Predicate<String> validLength(int min) {
        return value -> value != null && value.length() >= min;
    }

    public static Predicate<String> validLength(int min, int max) {
        return value -> value != null && value.length() >= min && value.length() <= max;
    }

    public static Predicate<String> validPassword() {
        return value ->
                value != null &&
                matchesRegex("[A-Z]", value) &&
                matchesRegex("[0-9]", value);
    }

    public static Predicate<String> validEmail() {
        return value -> matchesRegex("^[A-Za-z0-9+_.-]+@(.+)$", value);
    }

    public static Predicate<String> validWebsite() {
        return value -> matchesRegex("^(https?|ftp)://[^\\s/$.?#].[^\\s]*$", value);
    }
}
