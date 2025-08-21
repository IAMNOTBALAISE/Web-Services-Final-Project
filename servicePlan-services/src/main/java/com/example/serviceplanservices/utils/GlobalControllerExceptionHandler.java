package com.example.serviceplanservices.utils;





import com.example.serviceplanservices.utils.exceptions.DuplicateCoverageDetailsException;
import com.example.serviceplanservices.utils.exceptions.InvalidInputException;
import com.example.serviceplanservices.utils.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@Slf4j
public class GlobalControllerExceptionHandler {

    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public HttpErrorInfo handleNotFoundException(WebRequest request, NotFoundException ex) {
        return createHttpErrorInfo(NOT_FOUND, request, ex.getMessage());
    }

    @ResponseStatus(UNPROCESSABLE_ENTITY)
    @ExceptionHandler(InvalidInputException.class)
    public HttpErrorInfo handleInvalidInputException(WebRequest request, InvalidInputException ex) {
        return createHttpErrorInfo(UNPROCESSABLE_ENTITY, request, ex.getMessage());
    }

    @ResponseStatus(UNPROCESSABLE_ENTITY)
    @ExceptionHandler(DuplicateCoverageDetailsException.class)
    public HttpErrorInfo handleDuplicateCoverageDetailsException(WebRequest request,
                                                                 DuplicateCoverageDetailsException ex) {
        return createHttpErrorInfo(UNPROCESSABLE_ENTITY, request, ex.getMessage());
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public HttpErrorInfo handleIllegalArgumentException(WebRequest request, IllegalArgumentException ex) {
        return createHttpErrorInfo(BAD_REQUEST, request, ex.getMessage());
    }

    private HttpErrorInfo createHttpErrorInfo(HttpStatus status,
                                              WebRequest request,
                                              String message) {

        String path = request.getDescription(false);
        log.debug("Returning HTTP {} for {}: {}", status, path, message);
        return new HttpErrorInfo(status, path, message);
    }
}
