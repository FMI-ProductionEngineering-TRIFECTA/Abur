package ro.unibuc.hello.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String template, Object... args) {
        super(String.format(template, args));
    }
}
