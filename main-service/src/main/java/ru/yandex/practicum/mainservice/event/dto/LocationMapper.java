package ru.yandex.practicum.mainservice.event.dto;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.mainservice.event.model.Location;

@UtilityClass
public class LocationMapper {

    public static LocationDto toUserLocationDto(Location location) {
        return LocationDto.builder()
                .id(location.getId())
                .lat(location.getLat())
                .lon(location.getLon())
                .build();
    }
}
