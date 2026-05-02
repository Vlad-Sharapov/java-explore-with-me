package ru.yandex.practicum.mainservice.event.utils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class BeforeTwoHoursValidator implements ConstraintValidator<IsAfterHours, LocalDateTime> {

    private String hours;

    @Override
    public void initialize(IsAfterHours constraintAnnotation) {
        hours = constraintAnnotation.hours();
    }

    @Override
    public boolean isValid(LocalDateTime dateTime, ConstraintValidatorContext constraintValidatorContext) {
        if (dateTime != null) {
            return dateTime.isAfter(LocalDateTime.now().plusHours(Long.parseLong(hours)));
        }
        return true;
    }
}
