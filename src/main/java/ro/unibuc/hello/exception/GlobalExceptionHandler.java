package ro.unibuc.hello.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ro.unibuc.hello.dto.ErrorString;

import static ro.unibuc.hello.utils.ResponseUtils.badRequest;
import static ro.unibuc.hello.utils.ResponseUtils.unauthorized;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorString> handleValidationException(ValidationException ex) {
        return badRequest(ex.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorString> handleNotFound(NotFoundException ex) {
        return badRequest(ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorString> handleUnauthorized() {
        return unauthorized();
    }

}
