package ru.yandex.practicum.mainservice.location.service.privates.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.mainservice.location.model.Location;
import ru.yandex.practicum.mainservice.location.repository.LocationRepository;
import ru.yandex.practicum.mainservice.location.service.privates.LocationPrivateService;


@Service
@RequiredArgsConstructor
public class LocationPrivateServiceImpl implements LocationPrivateService {

    private final LocationRepository locationRepository;

    @Override
    public Location findAddedLocation(Location location) {
        return locationRepository.findLocationByCoordinate(location.getLat(), location.getLon())
                .orElse(location);
    }

}
