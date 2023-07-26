package ru.yandex.practicum.mainservice.event.utils;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = BeforeTwoHoursValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IsAfterHours {
    String message() default "Invalid user";

    String hours();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
