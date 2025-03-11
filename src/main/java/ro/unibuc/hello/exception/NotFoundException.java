package ro.unibuc.hello.exception;

public class NotFoundException extends RuntimeException {

    @SuppressWarnings("unused")
    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String template, Object... args) {
        super(String.format(template, args));
    }
}
