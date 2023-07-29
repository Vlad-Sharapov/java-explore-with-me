package ru.yandex.practicum.mainservice.event.dto;

import ru.yandex.practicum.mainservice.event.model.Location;

public class LocationMapper {

    public static LocationDto toLocationDto(Location location) {
        return LocationDto.builder()
                .id(location.getId())
                .lat(location.getLat())
                .lon(location.getLon())
                .build();
    }


}
