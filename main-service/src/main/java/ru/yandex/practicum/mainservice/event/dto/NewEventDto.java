package ru.yandex.practicum.mainservice.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.mainservice.event.model.Location;
import ru.yandex.practicum.mainservice.event.utils.IsAfterHours;

import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewEventDto {

    protected long id;

    @Size(min = 20, max = 2000,
            message = "Field: annotation. Error: Must not be less than 20 characters and more than 2000 characters.")
    protected String annotation;

    protected Long category;

    @Size(min = 20, max = 7000,
            message = "Field: description. Error: Must not be less than 20 characters and more than 7000 characters.")
    protected String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @IsAfterHours(hours = "2", message = "Field: eventDate. Error: Wrong date.")
    protected LocalDateTime eventDate;

    protected Location location;

    protected Boolean paid;

    protected Integer participantLimit;

    protected Boolean requestModeration;

    @Size(min = 3, max = 120,
            message = "Field: description. Error: Must not be less than 20 characters and more than 7000 characters.")
    protected String title;
}
