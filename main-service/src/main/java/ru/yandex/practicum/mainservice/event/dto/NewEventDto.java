package ru.yandex.practicum.mainservice.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.mainservice.event.model.Location;
import ru.yandex.practicum.mainservice.event.utils.IsAfterHours;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class NewEventDto {

    protected long id;

    @NotBlank(message = "annotation should not be empty.")
    @Size(min = 20, max = 2000,
            message = "Field: annotation. Error: Must not be less than 20 characters and more than 2000 characters.")
    protected String annotation;

    @NotNull(message = "category should not be empty.")
    protected Long category;

    @NotBlank(message = "description should not be empty.")
    @Size(min = 20, max = 7000,
            message = "Field: description. Error: Must not be less than 20 characters and more than 7000 characters.")
    protected String description;

    @NotNull(message = "eventDate should not be empty.")
    @Future
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @IsAfterHours(hours = "2", message = "Field: eventDate. Error: Wrong date.")
    protected LocalDateTime eventDate;

    @NotNull(message = "location should not be empty.")
    protected Location location;

    protected Boolean paid;

    @PositiveOrZero(message = "participantLimit should not be negative.")
    protected Integer participantLimit;

    protected Boolean requestModeration;

    @NotBlank(message = "title should not be empty.")
    @Size(min = 3, max = 120,
            message = "Field: description. Error: Must not be less than 20 characters and more than 7000 characters.")
    protected String title;
}
