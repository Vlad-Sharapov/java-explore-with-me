package ru.yandex.practicum.mainservice.event.service.admin.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mainservice.category.model.Category;
import ru.yandex.practicum.mainservice.category.repository.CategoryRepository;
import ru.yandex.practicum.mainservice.event.dto.EventFullDto;
import ru.yandex.practicum.mainservice.event.dto.UpdateEventAdminRequest;
import ru.yandex.practicum.mainservice.event.enums.EventState;
import ru.yandex.practicum.mainservice.event.enums.adminenum.AdmStateAction;
import ru.yandex.practicum.mainservice.event.model.Event;
import ru.yandex.practicum.mainservice.event.model.GetEventsForAdminRequest;
import ru.yandex.practicum.mainservice.event.model.QEvent;
import ru.yandex.practicum.mainservice.event.repository.EventRepository;
import ru.yandex.practicum.mainservice.event.service.admin.EventAdminService;
import ru.yandex.practicum.mainservice.event.stateclient.ClientHandler;
import ru.yandex.practicum.mainservice.exceptions.EntitiesConflictException;
import ru.yandex.practicum.mainservice.exceptions.EntityNotFoundException;
import ru.yandex.practicum.mainservice.requests.model.Request;
import ru.yandex.practicum.mainservice.requests.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ru.yandex.practicum.mainservice.event.dto.EventMapper.toEventFullDto;
import static ru.yandex.practicum.mainservice.event.utils.DateTimeValidator.dateTimeValidate;
import static ru.yandex.practicum.mainservice.requests.enums.RequestStatus.CONFIRMED;


@Service
@RequiredArgsConstructor
public class EventAdminServiceImpl implements EventAdminService {

    private final EventRepository eventRepository;

    private final CategoryRepository categoryRepository;

    private final ClientHandler clientHandler;

    private final RequestRepository requestRepository;


    @Transactional
    @Override
    public EventFullDto update(long eventId, UpdateEventAdminRequest updateEvent) {
        updateEvent.setId(eventId);
        Event event = eventRepository
                .findByIdEager(eventId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Event with id=%s was not found", eventId)));

        Event updatedEvent = updateEvent(event, updateEvent);
        long confirmedRequests = requestRepository.countAllByStatusAndEventId(CONFIRMED, updatedEvent.getId());
        eventRepository.save(updatedEvent);
        return toEventFullDto(updatedEvent, confirmedRequests);
    }

    @Override
    public List<EventFullDto> getEvents(GetEventsForAdminRequest request) {
        PageRequest pageRequest = PageRequest
                .of(request.getFrom() > 0 ? request.getFrom() / request.getSize() : 0, request.getSize());
        QEvent event = QEvent.event;
        List<BooleanExpression> conditions = new ArrayList<>();

        if (request.getUsers() != null) {
            conditions.add(event.initiator.id.in(request.getUsers()));
        }

        if (request.getCategories() != null) {
            conditions.add(event.category.id.in(request.getCategories()));
        }

        if (request.getStates() != null) {
            conditions.add(event.state.in(request.getStates()));
        }

        if (request.getRangeStart() != null &&
                request.getRangeEnd() != null) {
            dateTimeValidate(request.getRangeStart(), request.getRangeEnd());
            conditions.add(event.eventDate.between(request.getRangeStart(), request.getRangeEnd()));
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

        return toEventFullDto(events,
                clientHandler.getStatsForEvents(events, LocalDateTime.now().minusYears(100), LocalDateTime.now()),
                confirmedRequests);
    }

    private Event updateEvent(Event event, UpdateEventAdminRequest updateEvent) {
        Event.EventBuilder builder = event.toBuilder();
        if (updateEvent.getAnnotation() != null) {
            builder.annotation(updateEvent.getAnnotation());
        }
        if (updateEvent.getCategory() != null) {
            Category category = categoryRepository
                    .findById(updateEvent.getCategory())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found"));
            builder.category(category);
        }
        if (updateEvent.getDescription() != null) {
            builder.description(updateEvent.getDescription());
        }
        if (updateEvent.getEventDate() != null) {
            if (event.getPublishedOn() != null &&
                    event.getPublishedOn().isBefore(updateEvent.getEventDate().minusHours(1))) {
                throw new EntitiesConflictException("the start date of the event to be modified must be " +
                        "no earlier than an hour from the date of publication");
            }
            builder.eventDate(updateEvent.getEventDate());
        }
        if (updateEvent.getLocation() != null) {
            builder.location(updateEvent.getLocation());
        }
        if (updateEvent.getPaid() != null) {
            builder.paid(updateEvent.getPaid());
        }
        if (updateEvent.getParticipantLimit() != null) {
            builder.participantLimit(updateEvent.getParticipantLimit());
        }
        if (updateEvent.getRequestModeration() != null) {
            builder.requestModeration(updateEvent.getRequestModeration());
        }
        if (updateEvent.getTitle() != null) {
            builder.title(updateEvent.getTitle());
        }
        if (updateEvent.getStateAction() == AdmStateAction.PUBLISH_EVENT) {
            if (event.getState() == EventState.PUBLISHED || event.getState() == EventState.CANCELED){
                throw new EntitiesConflictException(String
                        .format("Cannot publish the event because it's not in the right state: %s",event.getState()));
            }
            builder.state(EventState.PUBLISHED);
            builder.publishedOn(LocalDateTime.now());
        }
        if (updateEvent.getStateAction() == AdmStateAction.REJECT_EVENT) {
            if (event.getState() == EventState.PUBLISHED) {
                throw new EntitiesConflictException("Cannot reject the event because it's not in the right state: PUBLISHED");
            }
            builder.state(EventState.CANCELED);
        }
        return builder.build();
    }
}
