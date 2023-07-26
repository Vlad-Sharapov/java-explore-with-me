package ru.yandex.practicum.mainservice.event.service.privates;

import ru.yandex.practicum.mainservice.event.dto.EventFullDto;
import ru.yandex.practicum.mainservice.event.dto.EventShortDto;
import ru.yandex.practicum.mainservice.event.dto.NewEventDto;
import ru.yandex.practicum.mainservice.event.dto.UpdateEventUserRequest;
import ru.yandex.practicum.mainservice.requests.dto.EventRequestStatusUpdateResult;
import ru.yandex.practicum.mainservice.requests.dto.ParticipationRequestDto;
import ru.yandex.practicum.mainservice.requests.model.EventRequestStatusUpdateRequest;

import java.util.List;

public interface EventPrivateService {

    EventFullDto add(long userId, NewEventDto newEventDto);

    EventFullDto update(long userId, long eventId, UpdateEventUserRequest updateEventUserRequest);

    List<EventShortDto> getAllUserEvents(long userId, int from, int size);

    EventFullDto getUserEvent(long userId, long eventId);

    List<ParticipationRequestDto> getUserEventRequests(long userId, long eventId);

    EventRequestStatusUpdateResult confirmRequests(long userId, long eventId, EventRequestStatusUpdateRequest updateResult);

}
