package ru.yandex.practicum.mainservice.event.controller.publics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.mainservice.event.dto.EventFullDto;
import ru.yandex.practicum.mainservice.event.dto.EventShortDto;
import ru.yandex.practicum.mainservice.event.model.GetEventsForPublicRequest;
import ru.yandex.practicum.mainservice.event.service.publics.EventPublicService;
import ru.yandex.practicum.mainservice.event.stateclient.ClientHandler;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventPublicController {

    private final EventPublicService eventService;

    private final ClientHandler clientHandler;

    @GetMapping
    public List<EventShortDto> events(@RequestParam(required = false) String text,
                                      @RequestParam(required = false) List<Long> categories,
                                      @RequestParam(required = false) Boolean paid,
                                      @RequestParam(required = false) LocalDateTime rangeStart,
                                      @RequestParam(required = false) LocalDateTime rangeEnd,
                                      @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                      @RequestParam(defaultValue = "event_date") String sort,
                                      @RequestParam(defaultValue = "0") Integer from,
                                      @RequestParam(defaultValue = "10") Integer size,
                                      HttpServletRequest request) {

        List<EventShortDto> events = eventService.getEvents(GetEventsForPublicRequest.of(
                        text,
                        categories,
                        paid,
                        rangeStart,
                        rangeEnd,
                        onlyAvailable,
                        sort,
                        from,
                        size
                )
        );

        clientHandler.addHit(request);

        return events;
    }

    @GetMapping("/{eventId}")
    public EventFullDto eventFullDto(@PathVariable Long eventId,
                                     HttpServletRequest request) {

        EventFullDto event = eventService.getEvent(eventId);
        clientHandler.addHit(request);

        return event;
    }


}
