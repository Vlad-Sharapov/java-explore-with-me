package ru.yandex.practicum.mainservice.event.dto;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.mainservice.category.model.Category;
import ru.yandex.practicum.mainservice.event.model.Event;
import ru.yandex.practicum.mainservice.requests.model.Request;
import ru.yandex.practicum.mainservice.user.model.User;
import ru.yandex.practicum.statdto.StatsDto;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.yandex.practicum.mainservice.category.dto.CategoryMapper.toCategoryDto;
import static ru.yandex.practicum.mainservice.event.dto.LocationMapper.toLocationDto;
import static ru.yandex.practicum.mainservice.user.dto.UserMapper.toUserDto;

@UtilityClass
public class EventMapper {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Event toEvent(NewEventDto eventDto, User initiator, Category category) {
        Event event = new Event();
        event.setId(eventDto.getId());
        event.setInitiator(initiator);
        event.setAnnotation(eventDto.getAnnotation());
        event.setCategory(category);
        event.setDescription(eventDto.getDescription());
        event.setEventDate(eventDto.getEventDate());
        event.setLocation(eventDto.getLocation());
        if (eventDto.getPaid() != null) {
            event.setPaid(eventDto.getPaid());
        }
        if (eventDto.getParticipantLimit() != null) {
            event.setParticipantLimit(eventDto.getParticipantLimit());
        }
        if (eventDto.getRequestModeration() != null) {
            event.setRequestModeration(eventDto.getRequestModeration());
        }
        event.setTitle(eventDto.getTitle());
        return event;
    }


    public static EventFullDto toEventFullDto(Event event, long confirmedRequests) {
        EventFullDto.EventFullDtoBuilder<?, ?> builder = EventFullDto.builder()
                .id(event.getId())
                .initiator(toUserDto(event.getInitiator()))
                .annotation(event.getAnnotation())
                .category(toCategoryDto(event.getCategory()))
                .confirmedRequests(confirmedRequests)
                .description(event.getDescription())
                .createdOn(event.getCreateOn().format(formatter))
                .eventDate(event.getEventDate().format(formatter))
                .location(toLocationDto(event.getLocation()))
                .paid(event.isPaid())
                .participantLimit(event.getParticipantLimit())
                .requestModeration(event.isRequestModeration())
                .title(event.getTitle())
                .state(event.getState());
        if (event.getPublishedOn() != null) {
            builder.publishedOn(event.getPublishedOn().format(formatter));
        }

        return builder.build();
    }

    public static EventFullDto toEventFullDto(Event event,
                                              Collection<StatsDto> statsDtos,
                                              long confirmedRequests) {
        EventFullDto eventFullDto = toEventFullDto(event, confirmedRequests);
        putHit(eventFullDto, statsDtos);
        return eventFullDto;
    }

    public static List<EventFullDto> toEventFullDto(Collection<Event> events,
                                                    Collection<StatsDto> statsDtos,
                                                    List<Request> confirmedRequests) {
        return events.stream()
                .map(event -> toEventFullDto(event, findNumberEventRequests(event.getId(), confirmedRequests)))
                .peek(eventFullDto -> putHit(eventFullDto, statsDtos))
                .collect(Collectors.toList());
    }

    public static EventShortDto toEventShortDto(Event event, long confirmedRequests) {
        EventShortDto.EventShortDtoBuilder<?, ?> builder = EventShortDto.builder()
                .id(event.getId())
                .initiator(toUserDto(event.getInitiator()))
                .annotation(event.getAnnotation())
                .category(toCategoryDto(event.getCategory()))
                .confirmedRequests(confirmedRequests)
                .description(event.getDescription())
                .eventDate(event.getEventDate().format(formatter))
                .paid(event.isPaid())
                .title(event.getTitle());

        return builder.build();
    }

    public static List<EventShortDto> toEventShortDto(Collection<Event> events,
                                                      Collection<StatsDto> statsDtos,
                                                      Collection<Request> confirmedRequests) {
        return events.stream()
                .map(event -> toEventShortDto(event, findNumberEventRequests(event.getId(), confirmedRequests)))
                .peek(eventShortDto -> putHit(eventShortDto, statsDtos))
                .collect(Collectors.toList());
    }

    private static void putHit(EventShortDto eventDto, Collection<StatsDto> statsDtos) {
        Optional<StatsDto> maybeStatsDto = statsDtos.stream()
                .filter(statsDto -> statsDto.getUri().equals("/events/" + eventDto.getId()))
                .findFirst();

        if (maybeStatsDto.isPresent()) {
            StatsDto statsDto = maybeStatsDto.get();
            eventDto.setViews(statsDto.getHits());
        }
    }

    private static long findNumberEventRequests(Long eventId, Collection<Request> confirmedRequests) {
        return confirmedRequests.stream()
                .filter(request -> eventId.equals(request.getEvent().getId()))
                .count();
    }

}


