package ru.yandex.practicum.mainservice;


import ru.yandex.practicum.mainservice.category.model.Category;
import ru.yandex.practicum.mainservice.event.dto.NewEventDto;
import ru.yandex.practicum.mainservice.event.enums.EventState;
import ru.yandex.practicum.mainservice.event.model.Event;
import ru.yandex.practicum.mainservice.event.model.Location;
import ru.yandex.practicum.mainservice.requests.enums.RequestStatus;
import ru.yandex.practicum.mainservice.requests.model.Request;
import ru.yandex.practicum.mainservice.user.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class EventTestData {


    public static final LocalDateTime FIXED_TIME =
            LocalDateTime.of(2026, 1, 10, 12, 0);

    public static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    public static User makeUser(Long id) {
        return User.builder()
                .id(id)
                .name("Ben")
                .email("user" + id + "@a.ru")
                .build();
    }

    public static NewEventDto.NewEventDtoBuilder makeEventDto(Category category, Location location) {
        return makeEventDto(category.getId(), location);
    }

    public static NewEventDto.NewEventDtoBuilder makeEventDto(Long category, Location location) {
        return NewEventDto.builder()
                .annotation("Annotation Annotation Annotation")
                .category(category)
                .description("Description Description Description")
                .eventDate(FIXED_TIME.plusDays(2))
                .location(location)
                .paid(false)
                .participantLimit(10)
                .requestModeration(true)
                .title("Concert");
    }

    public static Event.EventBuilder makeEvent(User initiator, Category category, Location location) {
        return Event.builder()
                .id(1L)
                .initiator(initiator)
                .annotation("annotation")
                .title("event1")
                .category(category)
                .publishedOn(FIXED_TIME.plusHours(5))
                .description("qwer tyui op[a sdfg hjkl;x")
                .eventDate(FIXED_TIME.plusHours(24))
                .location(location)
                .paid(false)
                .requestModeration(true)
                .participantLimit(5)
                .state(EventState.PENDING);
    }

    public static Request.RequestBuilder makeRequest(Long id, Event event, User user) {
        return Request.builder()
                .id(id)
                .event(event)
                .requester(user)
                .created(FIXED_TIME)
                .status(RequestStatus.PENDING);

    }

    public static Category makeCategory(Long id) {
        return makeCategory(id, "category1");
    }

    public static Category makeCategory(Long id, String name) {
        return Category.builder()
                .id(1L)
                .name(name)
                .build();
    }

    public static Location makeLocation(long id) {

        return makeLocation(id, 55.75, 37.61);
    }

    public static Location makeLocation(long id, Double lat, Double lon) {
        return Location.builder()
                .id(id)
                .lat(lat)
                .lon(lon)
                .build();
    }
}
