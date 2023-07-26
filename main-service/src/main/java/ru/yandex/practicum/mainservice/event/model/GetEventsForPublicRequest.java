package ru.yandex.practicum.mainservice.event.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder(toBuilder = true)
public class GetEventsForPublicRequest {

    private String text;

    private List<Long> categories;

    private Boolean paid;

    private LocalDateTime rangeStart;

    private LocalDateTime rangeEnd;

    private Boolean onlyAvailable;

    private Sort sort;

    private Integer from;

    private Integer size;


    public static GetEventsForPublicRequest of(String text,
                                              List<Long> categories,
                                              Boolean paid,
                                              LocalDateTime rangeStart,
                                              LocalDateTime rangeEnd,
                                              Boolean onlyAvailable,
                                              String sort,
                                              Integer from, Integer size) {
        return GetEventsForPublicRequest.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(onlyAvailable)
                .sort(Sort.valueOf(sort.toUpperCase()))
                .from(from)
                .size(size)
                .build();
    }

    public enum Sort {
        EVENT_DATE,VIEWS
    }

}
