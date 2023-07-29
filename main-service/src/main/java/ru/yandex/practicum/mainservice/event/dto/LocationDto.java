package ru.yandex.practicum.mainservice.event.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class LocationDto {

    private Long id;

    private Double lat;

    private Double lon;
}
