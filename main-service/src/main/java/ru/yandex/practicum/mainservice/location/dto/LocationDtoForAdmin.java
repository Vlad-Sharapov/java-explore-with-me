package ru.yandex.practicum.mainservice.location.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;


@Getter
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class LocationDtoForAdmin extends NewLocationDto {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Positive(message = "Field: radius. Error: The field cannot be negative")
    private Double radius;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Size(min = 2, max = 250,
            message = "Field: name. Error: Must not be less than 2 characters and more than 250 characters.")
    private String name;

}
