package ru.yandex.practicum.mainservice.place.dto;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.mainservice.place.model.Place;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class PlaceMapper {

    public static PlaceDto toPlaceDto(Place place) {
        return PlaceDto.builder()
                .id(place.getId())
                .lat(place.getLat())
                .lon(place.getLon())
                .radius(place.getRadius())
                .name(place.getName())
                .build();
    }

    public static List<PlaceDto> toPlaceDto(Collection<Place> places) {
        return places.stream()
                .map(PlaceMapper::toPlaceDto)
                .collect(Collectors.toList());
    }

    public static Place toPlace(PlaceDto place) {
        return Place.builder()
                .id(place.getId())
                .lat(place.getLat())
                .lon(place.getLon())
                .radius(place.getRadius())
                .name(place.getName())
                .build();
    }

}
