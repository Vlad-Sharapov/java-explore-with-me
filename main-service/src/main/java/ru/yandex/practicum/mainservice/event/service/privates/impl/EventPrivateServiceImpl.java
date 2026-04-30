package ru.yandex.practicum.mainservice.event.service.privates.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mainservice.category.model.Category;
import ru.yandex.practicum.mainservice.category.repository.CategoryRepository;
import ru.yandex.practicum.mainservice.event.dto.*;
import ru.yandex.practicum.mainservice.event.enums.EventState;
import ru.yandex.practicum.mainservice.event.enums.userenum.UsrStateAction;
import ru.yandex.practicum.mainservice.event.model.Event;
import ru.yandex.practicum.mainservice.event.model.QEvent;
import ru.yandex.practicum.mainservice.event.repository.EventRepository;
import ru.yandex.practicum.mainservice.event.service.privates.EventPrivateService;
import ru.yandex.practicum.mainservice.event.stateclient.ClientHandler;
import ru.yandex.practicum.mainservice.exceptions.BadRequestException;
import ru.yandex.practicum.mainservice.exceptions.EntitiesConflictException;
import ru.yandex.practicum.mainservice.exceptions.EntityNotFoundException;
import ru.yandex.practicum.mainservice.requests.dto.EventRequestStatusUpdateResult;
import ru.yandex.practicum.mainservice.requests.dto.ParticipationRequestDto;
import ru.yandex.practicum.mainservice.requests.model.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.mainservice.requests.model.Request;
import ru.yandex.practicum.mainservice.requests.repository.RequestRepository;
import ru.yandex.practicum.mainservice.user.model.User;
import ru.yandex.practicum.mainservice.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.yandex.practicum.mainservice.event.dto.EventMapper.toEventFullDto;
import static ru.yandex.practicum.mainservice.event.dto.EventMapper.toEventShortDto;
import static ru.yandex.practicum.mainservice.requests.dto.RequestMapper.toParticipationRequestDto;
import static ru.yandex.practicum.mainservice.requests.enums.RequestStatus.CONFIRMED;

@Service
@RequiredArgsConstructor
public class EventPrivateServiceImpl implements EventPrivateService {

    private final UserRepository userRepository;

    private final EventRepository eventRepository;

    private final CategoryRepository categoryRepository;

    private final ClientHandler clientHandler;

    private final RequestRepository requestRepository;

    @Transactional
    @Override
    public EventFullDto add(long userId, NewEventDto newEventDto) {
        User user = findUser(userId);
        Category category = categoryRepository
                .findById(newEventDto.getCategory())
                .orElseThrow(() -> new EntityNotFoundException(String
                        .format("Category with id=%s was not found", newEventDto.getCategory())));
        Event event = EventMapper.toEvent(newEventDto, user, category);
        long confirmedRequests = requestRepository.countAllByStatusAndEventId(CONFIRMED, event.getId());
        try {
            Event newEvent = eventRepository.save(event);
            return toEventFullDto(newEvent, confirmedRequests);
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("Required fields are not filled in");
        }
    }

    @Transactional
    @Override
    public EventFullDto update(long userId, long eventId, UpdateEventUserRequest updateEvent) {
        User user = findUser(userId);
        Event event = findEvent(eventId);
        if (!user.getId().equals(event.getInitiator().getId())) {
            throw new EntitiesConflictException("The user can only change his own events");
        }
        if (event.getState() == EventState.PUBLISHED) {
            throw new EntitiesConflictException("Event must not be published");
        }
        Event updatedEvent = updateEvent(event, updateEvent);
        long confirmedRequests = requestRepository.countAllByStatusAndEventId(CONFIRMED, updatedEvent.getId());
        return toEventFullDto(updatedEvent, confirmedRequests);
    }

    @Override
    public List<EventShortDto> getAllUserEvents(long userId, int from, int size) {
        User user = findUser(userId);

        PageRequest pageRequest = PageRequest.of(from > 0 ? from / size : 0, size);

        QEvent event = QEvent.event;

        BooleanExpression userCondition = event.initiator.id.in(user.getId());

        Page<Event> events = eventRepository.findAll(userCondition, pageRequest);

        List<Request> confirmedRequests = requestRepository.findAllByStatusAndEventIn(CONFIRMED, events.getContent());

        return toEventShortDto(events.getContent(),
                clientHandler.getStatsForEvents(events.getContent(), LocalDateTime.now().minusYears(100), LocalDateTime.now()),
                confirmedRequests);
    }

    @Override
    public EventFullDto getUserEvent(long userId, long eventId) {
        findUser(userId);
        Event event = eventRepository
                .findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Event with id=%s was not found", eventId)));
        long confirmedRequests = requestRepository.countAllByStatusAndEventId(CONFIRMED, event.getId());
        return toEventFullDto(event,
                clientHandler
                        .getStatsForEvent(event, LocalDateTime.now().minusYears(100), LocalDateTime.now().plusHours(1)),
                confirmedRequests);
    }

    @Override
    public List<ParticipationRequestDto> getUserEventRequests(long userId, long eventId) {
        findUser(userId);
        Event event = findEvent(eventId);
        List<Request> userEventRequests = requestRepository.findAllByEventId(event.getId());
        return toParticipationRequestDto(userEventRequests);
    }

    @Transactional
    @Override
    public EventRequestStatusUpdateResult confirmRequests(long userId, long eventId, EventRequestStatusUpdateRequest updateResult) {
        findUser(userId);
        Event event = findEvent(eventId);
        List<Request> eventRequests = requestRepository.findAllByEventId(eventId);
        List<Request> requests = updateRequestsStatus(event, eventRequests, updateResult);
        return makeResponse(requestRepository.saveAll(requests));
    }

    private Event updateEvent(Event event, UpdateEventUserRequest updateEvent) {
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
        if (updateEvent.getStateAction() == UsrStateAction.CANCEL_REVIEW) {
            builder.state(EventState.CANCELED);
        } else {
            builder.state(EventState.PENDING);
        }
        return builder.build();
    }

    private List<Request> updateRequestsStatus(Event event, List<Request> requests, EventRequestStatusUpdateRequest updateResult) {
        List<Long> maybeUpdatedRequests = updateResult.getRequestIds();
        int participantLimit = event.getParticipantLimit();
        if (participantLimit != 0 && event.isRequestModeration()) {
            long confirmedRequests = requests.stream()
                    .filter(request -> request.getStatus() == CONFIRMED)
                    .count();
            if ((confirmedRequests + maybeUpdatedRequests.size()) > participantLimit) {
                throw new EntitiesConflictException("The limit of requests for the event has been reached");
            }
            return requests.stream().filter(request -> maybeUpdatedRequests.contains(request.getId()))
                    .peek(request -> request.setStatus(updateResult.getStatus()))
                    .collect(Collectors.toList());
        } else {
            return requests.stream().filter(request -> maybeUpdatedRequests.contains(request.getId()))
                    .peek(request -> request.setStatus(updateResult.getStatus()))
                    .collect(Collectors.toList());
        }
    }

    private EventRequestStatusUpdateResult makeResponse(List<Request> requests) {
        List<ParticipationRequestDto> confirmedRequestsBuilder = new ArrayList<>();
        List<ParticipationRequestDto> canceledRequestsBuilder = new ArrayList<>();

        requests.forEach(request -> {
            if (request.getStatus() == CONFIRMED) {
                confirmedRequestsBuilder.add(toParticipationRequestDto(request));
            } else {
                canceledRequestsBuilder.add(toParticipationRequestDto(request));
            }
        });
        return EventRequestStatusUpdateResult.builder()
                .rejectedRequests(canceledRequestsBuilder)
                .confirmedRequests(confirmedRequestsBuilder)
                .build();
    }

    private User findUser(Long userId) {
        return userRepository
                .findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private Event findEvent(Long eventId) {
        return eventRepository
                .findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Event with id=%s was not found", eventId)));
    }
}
