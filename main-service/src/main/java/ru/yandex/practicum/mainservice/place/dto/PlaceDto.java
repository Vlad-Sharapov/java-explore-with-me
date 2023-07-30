package ru.yandex.practicum.mainservice.place.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;


@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class PlaceDto {

    private Long id;

    private Double lat;

    private Double lon;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Positive(message = "Field: radius. Error: The field cannot be negative")
    private Double radius;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Size(min = 2, max = 250,
            message = "Field: name. Error: Must not be less than 2 characters and more than 250 characters.")
    private String name;

}
