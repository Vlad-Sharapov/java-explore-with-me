package ru.yandex.practicum.mainservice.location.service.admin.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.mainservice.exceptions.EntityNotFoundException;
import ru.yandex.practicum.mainservice.location.dto.LocationDtoForAdmin;
import ru.yandex.practicum.mainservice.location.model.GetLocationsForAdminRequest;
import ru.yandex.practicum.mainservice.location.model.Location;
import ru.yandex.practicum.mainservice.location.model.QLocation;
import ru.yandex.practicum.mainservice.location.repository.LocationRepository;
import ru.yandex.practicum.mainservice.location.service.admin.LocationAdminService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ru.yandex.practicum.mainservice.location.dto.LocationMapper.toLocation;
import static ru.yandex.practicum.mainservice.location.dto.LocationMapper.toLocationDtoForAdmin;

@Service
@RequiredArgsConstructor
public class LocationAdminServiceImpl implements LocationAdminService {

    private final LocationRepository locationRepository;

    @Override
    public LocationDtoForAdmin add(LocationDtoForAdmin locationDtoForAdmin) {
        Location location = toLocation(locationDtoForAdmin);
        Location savedLocation = locationRepository.save(location);
        Double aDouble = locationRepository.coordinateLength(56.342905, 37.517609, 56.173671, 37.502729);
        System.out.println(aDouble);
        return toLocationDtoForAdmin(savedLocation);
    }

    @Override
    public LocationDtoForAdmin update(LocationDtoForAdmin locationDtoForAdmin) {
        Location location = locationRepository.findById(locationDtoForAdmin.getId())
                .orElseThrow(() -> new EntityNotFoundException(String.format("location with id=%s was not found", locationDtoForAdmin.getId())));
        Location updatedLocation = locationRepository.save(updateLocation(location, locationDtoForAdmin));
        return toLocationDtoForAdmin(updatedLocation);
    }

    @Override
    public List<LocationDtoForAdmin> getLocations(GetLocationsForAdminRequest request) {
        PageRequest pageRequest = PageRequest
                .of(request.getFrom() > 0 ? request.getFrom() / request.getSize() : 0, request.getSize());
        List<BooleanExpression> conditions = new ArrayList<>();
        QLocation location = QLocation.location;

        if (request.getText() != null) {
            location.name.likeIgnoreCase("%" + request.getText() + "%");
        }
        if (request.getUntitled() != null) {
            conditions.add(request.getUntitled() ? location.name.isNull() : location.name.isNotNull());
        }

        Optional<BooleanExpression> maybeFinallyCondition = conditions.stream()
                .reduce(BooleanExpression::and);
        List<Location> locations;
        locations = maybeFinallyCondition
                .map(booleanExpression -> locationRepository.findAll(booleanExpression, pageRequest).getContent())
                .orElseGet(() -> locationRepository.findAll(pageRequest).getContent());

        return toLocationDtoForAdmin(locations);
    }

    @Override
    public void delete(Long locationId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("location with id=%s was not found", locationId)));
        locationRepository.delete(location);
    }

    @Override
    public Location findAddedLocation(Location location) {
        Optional<Location> locationByCoordinate = locationRepository.findLocationByCoordinate(location.getLat(), location.getLon());
        if (locationByCoordinate.isPresent()) {
            Location foundLocation = locationByCoordinate.get();
            return location.toBuilder()
                    .name(foundLocation.getName())
                    .radius(foundLocation.getRadius())
                    .build();
        } else {
            return location;
        }
    }

//    @Override
//    public Location findAddedLocation(Location location) {
//        return locationRepository.findLocationByCoordinate(location.getLat(), location.getLon())
//                .orElse(location);
//    }

    private Location updateLocation(Location location, LocationDtoForAdmin locationDtoForAdmin) {

        Location.LocationBuilder builder = location.toBuilder()
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
