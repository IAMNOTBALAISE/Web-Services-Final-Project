package com.example.apigatewayservice.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ServerWebExchange;

import static org.springframework.http.HttpStatus.*;


@RestControllerAdvice
@Slf4j
public class GlobalControllerExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<HttpErrorInfo> handleNotFound(ServerWebExchange exchange, NotFoundException ex) {
        return createResponse(NOT_FOUND, exchange, ex.getMessage());
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<HttpErrorInfo> handleInvalidInput(ServerWebExchange exchange, InvalidInputException ex) {
        return createResponse(UNPROCESSABLE_ENTITY, exchange, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<HttpErrorInfo> handleIllegalArgument(ServerWebExchange exchange, IllegalArgumentException ex) {
        return createResponse(BAD_REQUEST, exchange, ex.getMessage());
    }


    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<HttpErrorInfo> handleValidation(ServerWebExchange exchange, WebExchangeBindException ex) {
        String message = ex.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)    // uses the message from @NotBlank, @Size, etc.
                .findFirst()
                .orElse("Validation failed");

        return createResponse(BAD_REQUEST, exchange, message);
    }

    private ResponseEntity<HttpErrorInfo> createResponse(HttpStatus status,
                                                         ServerWebExchange exchange,
                                                         String message) {
        String path = exchange.getRequest()
                .getPath()
                .pathWithinApplication()
                .value();
        log.debug("HTTP {} on {}: {}", status, path, message);
        return ResponseEntity.status(status)
                .body(new HttpErrorInfo(status, path, message));
    }
}
