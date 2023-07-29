package ru.yandex.practicum.mainservice.location.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class NewLocationDto {

    private Long id;

    private Double lat;

    private Double lon;

}
