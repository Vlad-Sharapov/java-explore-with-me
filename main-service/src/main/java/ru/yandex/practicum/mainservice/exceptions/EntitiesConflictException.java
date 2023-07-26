package ru.yandex.practicum.mainservice.exceptions;

public class EntitiesConflictException extends RuntimeException {

    public EntitiesConflictException(String message) {
        super(message);
    }

}
