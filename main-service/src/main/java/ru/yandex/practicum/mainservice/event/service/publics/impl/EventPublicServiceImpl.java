package ru.yandex.practicum.mainservice.event.service.publics.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.mainservice.event.dto.EventFullDto;
import ru.yandex.practicum.mainservice.event.dto.EventShortDto;
import ru.yandex.practicum.mainservice.event.enums.EventState;
import ru.yandex.practicum.mainservice.event.model.Event;
import ru.yandex.practicum.mainservice.event.model.GetEventsForPublicRequest;
import ru.yandex.practicum.mainservice.event.model.QEvent;
import ru.yandex.practicum.mainservice.event.repository.EventRepository;
import ru.yandex.practicum.mainservice.event.service.publics.EventPublicService;
import ru.yandex.practicum.mainservice.event.stateclient.ClientHandler;
import ru.yandex.practicum.mainservice.exceptions.EntityNotFoundException;
import ru.yandex.practicum.mainservice.place.model.Place;
import ru.yandex.practicum.mainservice.place.repository.PlaceRepository;
import ru.yandex.practicum.mainservice.requests.model.Request;
import ru.yandex.practicum.mainservice.requests.repository.RequestRepository;
import ru.yandex.practicum.statdto.StatsDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.yandex.practicum.mainservice.event.dto.EventMapper.toEventFullDto;
import static ru.yandex.practicum.mainservice.event.dto.EventMapper.toEventShortDto;
import static ru.yandex.practicum.mainservice.event.enums.EventState.PUBLISHED;
import static ru.yandex.practicum.mainservice.event.utils.DateTimeValidator.dateTimeValidate;
import static ru.yandex.practicum.mainservice.requests.enums.RequestStatus.CONFIRMED;

@Service
@RequiredArgsConstructor
public class EventPublicServiceImpl implements EventPublicService {

    private final EventRepository eventRepository;

    private final ClientHandler clientHandler;

    private final RequestRepository requestRepository;

    private final PlaceRepository placeRepository;

    @Override
    public List<EventShortDto> getEvents(GetEventsForPublicRequest request) {
        PageRequest pageRequest = PageRequest
                .of(request.getFrom() > 0 ? request.getFrom() / request.getSize() : 0, request.getSize(),
                        makeOrderByClause(request.getSort()));
        QEvent event = QEvent.event;
        List<BooleanExpression> conditions = new ArrayList<>();
        conditions.add(event.state.eq(EventState.PUBLISHED));

        if (request.getText() != null) {
            conditions.add(event.annotation.likeIgnoreCase("%" + request.getText() + "%")
                    .or(event.description.likeIgnoreCase("%" + request.getText() + "%"))
                    .or(event.title.likeIgnoreCase("%" + request.getText() + "%")));
        }

        if (request.getCategories() != null) {
            conditions.add(event.category.id.in(request.getCategories()));
        }

        if (request.getPaid() != null) {
            conditions.add(event.paid.eq(request.getPaid()));
        }

        if (request.getRangeStart() != null &&
                request.getRangeEnd() != null) {
            dateTimeValidate(request.getRangeStart(), request.getRangeEnd());
            conditions.add(event.eventDate.between(request.getRangeStart(), request.getRangeEnd()));
        } else {
            conditions.add(event.eventDate.after(LocalDateTime.now()));
        }

        Optional<BooleanExpression> maybeFinallyCondition = conditions.stream()
                .reduce(BooleanExpression::and);
        List<Event> events;
        if (maybeFinallyCondition.isPresent()) {
            events = eventRepository.findAll(maybeFinallyCondition.get(), pageRequest).getContent();
        } else {
            events = eventRepository.findAll(pageRequest).getContent();
        }
        List<Request> confirmedRequests = requestRepository.findAllByStatusAndEventIn(CONFIRMED, events);

        if (request.getOnlyAvailable()) {
            events = events.stream()
                    .filter(e -> e.getParticipantLimit() > getEventConfirmedRequests(e.getId(), confirmedRequests).size())
                    .collect(Collectors.toList());
        }
        return toEventShortDto(events,
                clientHandler.getStatsForEvents(events, LocalDateTime.now().minusYears(100), LocalDateTime.now()),
                confirmedRequests);
    }

    @Override
    public List<EventShortDto> getEventsByLocation(long placeId, int from, int size) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Place with id=%s was not found", placeId)));
        List<Event> eventsByLocation = eventRepository
                .findPublishedEventsByLocation(place.getLat(), place.getLon(), place.getRadius());
        List<Request> confirmedRequests = requestRepository.findAllByStatusAndEventIn(CONFIRMED, eventsByLocation);
        List<StatsDto> statsForEvents = clientHandler
                .getStatsForEvents(eventsByLocation, LocalDateTime.now().minusYears(100), LocalDateTime.now());
        return toEventShortDto(eventsByLocation, statsForEvents, confirmedRequests);
    }

    @Override
    public EventFullDto getEvent(long eventId) {
        Event event = eventRepository
                .findByIdAndState(eventId, PUBLISHED)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Event with id=%s was not found", eventId)));
        return toEventFullDto(event,
                clientHandler
                        .getStatsForEvent(event, LocalDateTime.now().minusYears(100), LocalDateTime.now().plusHours(1)),
                requestRepository.countAllByStatusAndEventId(CONFIRMED, event.getId()));
    }

    private Sort makeOrderByClause(GetEventsForPublicRequest.Sort sort) {
        switch (sort) {
            case EVENT_DATE:
                return Sort.by("eventDate").ascending();
            case VIEWS:
                return Sort.by("views").ascending();
            default:
                return Sort.by("eventDate").descending();
        }
    }

    private List<Request> getEventConfirmedRequests(long eventId, List<Request> confirmedRequests) {
        return confirmedRequests.stream()
                .filter(confirmedRequest -> confirmedRequest.getEvent().getId().equals(eventId))
                .collect(Collectors.toList());
    }
}
