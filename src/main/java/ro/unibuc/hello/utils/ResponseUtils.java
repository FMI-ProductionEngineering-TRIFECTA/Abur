package ro.unibuc.hello.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ro.unibuc.hello.dto.ErrorString;

public interface ResponseUtils {

    private static <T> ResponseEntity<T> response(T obj, HttpStatus status) {
        return new ResponseEntity<>(obj, status);
    }

    static <T> ResponseEntity<T> ok(T obj) {
        return response(obj, HttpStatus.OK);
    }

    static <T> ResponseEntity<T> created(T obj) {
        return response(obj, HttpStatus.CREATED);
    }

    static ResponseEntity<Void> noContent() {
        return response(null, HttpStatus.NO_CONTENT);
    }

    static ResponseEntity<ErrorString> badRequest(String err) {
        return response(new ErrorString(err), HttpStatus.BAD_REQUEST);
    }

    @SuppressWarnings("unused")
    static ResponseEntity<ErrorString> badRequest(String template, Object... args) {
        return response(new ErrorString(String.format(template, args)), HttpStatus.BAD_REQUEST);
    }

    static ResponseEntity<ErrorString> unauthorized() {
        return response(null, HttpStatus.UNAUTHORIZED);
    }

}
