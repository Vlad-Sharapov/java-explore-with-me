package ru.yandex.practicum.mainservice.event.service.publics;

import ru.yandex.practicum.mainservice.event.dto.EventFullDto;
import ru.yandex.practicum.mainservice.event.dto.EventShortDto;
import ru.yandex.practicum.mainservice.event.model.GetEventsForPublicRequest;

import java.util.List;

public interface EventPublicService {

    List<EventShortDto> getEvents(GetEventsForPublicRequest request);

    EventFullDto getEvent(long eventId);

}
