package ro.unibuc.hello.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ro.unibuc.hello.dto.ErrorString;

public final class ResponseUtils {

    private ResponseUtils() {}

    private static <T> ResponseEntity<T> response(T obj, HttpStatus status) {
        return new ResponseEntity<>(obj, status);
    }

    public static <T> ResponseEntity<T> ok(T obj) {
        return response(obj, HttpStatus.OK);
    }

    public static <T> ResponseEntity<T> created(T obj) {
        return response(obj, HttpStatus.CREATED);
    }

    public static ResponseEntity<Void> noContent() {
        return response(null, HttpStatus.NO_CONTENT);
    }

    public static ResponseEntity<ErrorString> badRequest(String err) {
        return response(new ErrorString(err), HttpStatus.BAD_REQUEST);
    }

    @SuppressWarnings("unused")
    public static ResponseEntity<ErrorString> badRequest(String template, Object... args) {
        return response(new ErrorString(String.format(template, args)), HttpStatus.BAD_REQUEST);
    }

    public static ResponseEntity<ErrorString> unauthorized() {
        return response(null, HttpStatus.UNAUTHORIZED);
    }

}
