package ro.unibuc.hello.exception;

public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String template, Object... args) {
        super(String.format(template, args));
    }
}
