package ru.yandex.practicum.mainservice.event.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.mainservice.event.dto.EventFullDto;
import ru.yandex.practicum.mainservice.event.dto.UpdateEventAdminRequest;
import ru.yandex.practicum.mainservice.event.model.GetEventsForAdminRequest;
import ru.yandex.practicum.mainservice.event.service.admin.EventAdminService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/events")
public class EventAdminController {

    private final EventAdminService eventService;


    @PatchMapping("/{eventId}")
    public EventFullDto update(@PathVariable long eventId,
                               @Valid @RequestBody UpdateEventAdminRequest updateEventAdminRequest) {
        return eventService.update(eventId, updateEventAdminRequest);
    }

    @GetMapping
    public List<EventFullDto> events(@RequestParam(required = false) List<Long> users,
                                     @RequestParam(required = false) List<String> states,
                                     @RequestParam(required = false) List<Long> categories,
                                     @RequestParam(required = false) LocalDateTime rangeStart,
                                     @RequestParam(required = false) LocalDateTime rangeEnd,
                                     @RequestParam(defaultValue = "0") Integer from,
                                     @RequestParam(defaultValue = "10") Integer size) {

        return eventService.getEvents(GetEventsForAdminRequest.of(
                        users,
                        states,
                        categories,
                        rangeStart,
                        rangeEnd,
                        from,
                        size
                )
        );
    }

    @GetMapping("/{placeId}/locations")
    public List<EventFullDto> eventsByLocation(@PathVariable long placeId,
                                               @RequestParam(defaultValue = "0") Integer from,
                                               @RequestParam(defaultValue = "10") Integer size) {

        return eventService.getEventsByLocation(placeId, from, size);
    }
}
