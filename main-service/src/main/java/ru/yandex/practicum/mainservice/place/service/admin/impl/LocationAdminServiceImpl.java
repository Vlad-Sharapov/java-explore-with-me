package ru.yandex.practicum.mainservice.place.service.admin.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mainservice.exceptions.EntityNotFoundException;
import ru.yandex.practicum.mainservice.place.dto.PlaceDto;
import ru.yandex.practicum.mainservice.place.model.Place;
import ru.yandex.practicum.mainservice.place.repository.PlaceRepository;
import ru.yandex.practicum.mainservice.place.service.admin.LocationAdminService;

import java.util.List;

import static ru.yandex.practicum.mainservice.place.dto.PlaceMapper.toPlace;
import static ru.yandex.practicum.mainservice.place.dto.PlaceMapper.toPlaceDto;

@Service
@RequiredArgsConstructor
public class LocationAdminServiceImpl implements LocationAdminService {

    private final PlaceRepository placeRepository;

    @Transactional
    @Override
    public PlaceDto add(PlaceDto placeDto) {
        Place place = toPlace(placeDto);
        Place savedPlace = placeRepository.save(place);
        return toPlaceDto(savedPlace);
    }

    @Transactional
    @Override
    public PlaceDto update(PlaceDto locationDtoForAdmin) {
        Place place = placeRepository.findById(locationDtoForAdmin.getId())
                .orElseThrow(() -> new EntityNotFoundException(String.format("location with id=%s was not found", locationDtoForAdmin.getId())));
        Place updatedPlace = placeRepository.save(updatePlace(place, locationDtoForAdmin));
        return toPlaceDto(updatedPlace);
    }

    public List<PlaceDto> getPlaces(String text, int from, int size) {
        PageRequest pageRequest = PageRequest
                .of(from > 0 ? from / size : 0, size);

        List<Place> places;
        if (text != null) {
            places = placeRepository
                    .findAllByNameLikeIgnoreCase("%" + text + "%", pageRequest);
        } else {
            places = placeRepository.findAll(pageRequest).getContent();
        }
        return toPlaceDto(places);
    }

    @Transactional
    @Override
    public void delete(Long locationId) {
        Place place = placeRepository.findById(locationId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("location with id=%s was not found", locationId)));
        placeRepository.delete(place);
    }

    private Place updatePlace(Place place, PlaceDto locationDtoForAdmin) {

        Place.PlaceBuilder builder = place.toBuilder()
                .id(locationDtoForAdmin.getId());

        if (locationDtoForAdmin.getLon() != null) {
            builder.lon(locationDtoForAdmin.getLon());
        }
        if (locationDtoForAdmin.getLat() != null) {
            builder.lat(locationDtoForAdmin.getLat());
        }
        if (locationDtoForAdmin.getRadius() != null) {
            builder.radius(locationDtoForAdmin.getRadius());
        }
        if (locationDtoForAdmin.getName() != null) {
            builder.name(locationDtoForAdmin.getName());
        }
        return builder.build();
    }
}
