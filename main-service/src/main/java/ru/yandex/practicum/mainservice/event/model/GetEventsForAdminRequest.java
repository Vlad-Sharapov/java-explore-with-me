package ru.yandex.practicum.mainservice.event.model;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.mainservice.event.enums.EventState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class GetEventsForAdminRequest {

    private List<Long> users;

    private List<EventState> states;

    private List<Long> categories;

    private LocalDateTime rangeStart;

    private LocalDateTime rangeEnd;

    private Integer from;

    private Integer size;


    public static GetEventsForAdminRequest of(List<Long> users,
                                              List<String> states,
                                              List<Long> categories,
                                              LocalDateTime rangeStart,
                                              LocalDateTime rangeEnd,
                                              Integer from, Integer size) {
        List<EventState> eventStates = states != null ? states.stream()
                .map(EventState::valueOf)
                .collect(Collectors.toList()) : null;
        return GetEventsForAdminRequest.builder()
                .users(users)
                .states(eventStates)
                .categories(categories)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .from(from)
                .size(size)
                .build();
    }


}
