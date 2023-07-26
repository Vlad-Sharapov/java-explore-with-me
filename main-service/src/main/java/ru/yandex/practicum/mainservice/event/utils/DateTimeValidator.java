package ru.yandex.practicum.mainservice.event.utils;

import ru.yandex.practicum.mainservice.exceptions.BadRequestException;

import java.time.LocalDateTime;

public class DateTimeValidator {

    public static void dateTimeValidate(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new BadRequestException("Fields: start, end. Error: The start time should not be later than the end time");
        }
    }

}
