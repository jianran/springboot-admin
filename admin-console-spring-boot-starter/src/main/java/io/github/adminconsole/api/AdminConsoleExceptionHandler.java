package io.github.adminconsole.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(basePackageClasses = AdminConsoleController.class)
public class AdminConsoleExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<?> badRequest(IllegalArgumentException exception) { return error(HttpStatus.BAD_REQUEST, exception); }
    @ExceptionHandler(SecurityException.class)
    ResponseEntity<?> forbidden(SecurityException exception) { return error(HttpStatus.FORBIDDEN, exception); }
    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<?> unavailable(IllegalStateException exception) { return error(HttpStatus.SERVICE_UNAVAILABLE, exception); }
    private ResponseEntity<?> error(HttpStatus status, Exception exception) {
        return ResponseEntity.status(status).body(Map.of("error", exception.getMessage()));
    }
}
