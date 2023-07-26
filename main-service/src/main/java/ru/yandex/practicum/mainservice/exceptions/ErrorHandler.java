package ru.yandex.practicum.mainservice.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    @ExceptionHandler({DataIntegrityViolationException.class, ConstraintViolationException.class, EntitiesConflictException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleEntitiesConflictException(final Exception e) {
        return new ErrorResponse(HttpStatus.CONFLICT.toString(),
                "For the requested operation the conditions are not met.",
                e.getMessage(),
                LocalDateTime.now().format(formatter));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleObjectNotFoundException(final EntityNotFoundException e) {

        return new ErrorResponse(HttpStatus.NOT_FOUND.toString(),
                "The required object was not found.",
                e.getMessage(),
                LocalDateTime.now().format(formatter));
    }

    @ExceptionHandler({BadRequestException.class,})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleObjectExistenceException(final Exception e) {
        return new ErrorResponse(HttpStatus.BAD_REQUEST.toString(),
                "Incorrectly made request.",
                e.getMessage(),
                LocalDateTime.now().format(formatter));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        return new ErrorResponse(HttpStatus.BAD_REQUEST.toString(),
                "Incorrectly made request.asd",
                Objects.requireNonNull(e.getFieldError()).getDefaultMessage(),
                LocalDateTime.now().format(formatter));
    }
}
