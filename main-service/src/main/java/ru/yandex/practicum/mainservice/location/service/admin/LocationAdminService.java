package ru.yandex.practicum.mainservice.location.service.admin;

import ru.yandex.practicum.mainservice.location.dto.LocationDtoForAdmin;
import ru.yandex.practicum.mainservice.location.model.GetLocationsForAdminRequest;
import ru.yandex.practicum.mainservice.location.model.Location;

import java.util.List;

public interface LocationAdminService {


    LocationDtoForAdmin add(LocationDtoForAdmin locationDtoForAdmin);

    LocationDtoForAdmin update(LocationDtoForAdmin locationDtoForAdmin);

    List<LocationDtoForAdmin> getLocations(GetLocationsForAdminRequest getLocationsForAdminRequest);

    void delete(Long compilationId);

    Location findAddedLocation(Location location);
}
