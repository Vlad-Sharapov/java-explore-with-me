package ru.yandex.practicum.mainservice.place.service.admin;

import ru.yandex.practicum.mainservice.place.dto.PlaceDto;

import java.util.List;

public interface LocationAdminService {


    PlaceDto add(PlaceDto locationDtoForAdmin);

    PlaceDto update(PlaceDto locationDtoForAdmin);

    List<PlaceDto> getPlaces(String text, int from, int size);

    void delete(Long compilationId);
}
