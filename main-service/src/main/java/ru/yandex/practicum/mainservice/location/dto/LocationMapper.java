package ru.yandex.practicum.mainservice.location.dto;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.mainservice.location.model.Location;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class LocationMapper {

    public static NewLocationDto toUserLocationDto(Location location) {
        return NewLocationDto.builder()
                .id(location.getId())
                .lat(location.getLat())
                .lon(location.getLon())
                .build();
    }

    public static LocationDtoForAdmin toLocationDtoForAdmin(Location location) {
        return LocationDtoForAdmin.builder()
                .id(location.getId())
                .lat(location.getLat())
                .lon(location.getLon())
                .radius(location.getRadius())
                .name(location.getName())
                .build();
    }

    public static List<LocationDtoForAdmin> toLocationDtoForAdmin(Collection<Location> locations) {
        return locations.stream()
                .map(LocationMapper::toLocationDtoForAdmin)
                .collect(Collectors.toList());
    }

    public static OutputLocationDtoForUser toLocationDtoForUser(Location location) {
        return OutputLocationDtoForUser.builder()
                .id(location.getId())
                .lat(location.getLat())
                .lon(location.getLon())
                .name(location.getName())
                .build();
    }

    public static Location toLocation(LocationDtoForAdmin locationDtoForAdmin) {
        return Location.builder()
                .id(locationDtoForAdmin.getId())
                .lat(locationDtoForAdmin.getLat())
                .lon(locationDtoForAdmin.getLon())
                .radius(locationDtoForAdmin.getRadius())
                .name(locationDtoForAdmin.getName())
                .build();
    }

}
