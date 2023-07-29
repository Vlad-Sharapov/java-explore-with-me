package ru.yandex.practicum.mainservice.location.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Size;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class OutputLocationDtoForUser extends NewLocationDto {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Size(min = 2, max = 250,
            message = "Field: name. Error: Must not be less than 2 characters and more than 250 characters.")
    private String name;
}
