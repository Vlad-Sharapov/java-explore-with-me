package ru.yandex.practicum.mainservice.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.mainservice.event.enums.userenum.UsrStateAction;
import ru.yandex.practicum.mainservice.event.model.Location;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventUserRequest {

    @Size(min = 20, max = 2000,
            message = "Field: annotation. Error: Must not be less than 20 characters and more than 2000 characters.")
    private String annotation;

    private Long category;

    @Size(min = 20, max = 7000,
            message = "Field: description. Error: Must not be less than 20 characters and more than 7000 characters.")
    private String description;

    @Future
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private Location location;

    private Boolean paid;

    @PositiveOrZero(message = "participantLimit не должен быть отрицательным")
    private Integer participantLimit;

    private Boolean requestModeration;

    @Size(min = 3, max = 120,
            message = "Field: title. Error: Must not be less than 3 characters and more than 120 characters.")
    private String title;

    private UsrStateAction stateAction;

}
