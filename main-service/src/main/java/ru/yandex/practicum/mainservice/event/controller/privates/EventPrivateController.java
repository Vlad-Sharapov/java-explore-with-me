package ru.yandex.practicum.mainservice.event.controller.privates;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.mainservice.event.dto.EventFullDto;
import ru.yandex.practicum.mainservice.event.dto.EventShortDto;
import ru.yandex.practicum.mainservice.event.dto.NewEventDto;
import ru.yandex.practicum.mainservice.event.dto.UpdateEventUserRequest;
import ru.yandex.practicum.mainservice.event.service.privates.EventPrivateService;
import ru.yandex.practicum.mainservice.requests.dto.EventRequestStatusUpdateResult;
import ru.yandex.practicum.mainservice.requests.dto.ParticipationRequestDto;
import ru.yandex.practicum.mainservice.requests.model.EventRequestStatusUpdateRequest;

import javax.validation.Valid;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events")
public class EventPrivateController {

    private final EventPrivateService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto save(@PathVariable long userId,
                             @Valid @RequestBody NewEventDto newEventDto) {
        return eventService.add(userId, newEventDto);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto update(@PathVariable long userId,
                               @PathVariable long eventId,
                               @Valid @RequestBody UpdateEventUserRequest newEventDto) {
        return eventService.update(userId, eventId, newEventDto);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult changeStatus(@PathVariable long userId,
                                                       @PathVariable long eventId,
                                                       @RequestBody EventRequestStatusUpdateRequest updateRequest) {
        return eventService.confirmRequests(userId, eventId, updateRequest);

    }

    @GetMapping
    public List<EventShortDto> events(@PathVariable long userId,
                                      @RequestParam(defaultValue = "0") int from,
                                      @RequestParam(defaultValue = "10") int size) {
        return eventService.getAllUserEvents(userId, from, size);

    }

    @GetMapping("/{eventId}")
    public EventFullDto event(@PathVariable long userId,
                              @PathVariable long eventId) {
        return eventService.getUserEvent(userId, eventId);

    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> eventRequests(@PathVariable long userId,
                                                       @PathVariable long eventId) {
        return eventService.getUserEventRequests(userId, eventId);

    }
}
