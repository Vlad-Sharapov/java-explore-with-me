package ru.yandex.practicum.mainservice.event.service.admin;

import ru.yandex.practicum.mainservice.event.dto.EventFullDto;
import ru.yandex.practicum.mainservice.event.dto.UpdateEventAdminRequest;
import ru.yandex.practicum.mainservice.event.model.GetEventsForAdminRequest;

import java.util.List;

public interface EventAdminService {

    EventFullDto update(long eventId, UpdateEventAdminRequest updateEventUserRequest);

    List<EventFullDto> getEvents(GetEventsForAdminRequest request);

    List<EventFullDto> getEventsByLocation(long placeId, int from, int size);

}
