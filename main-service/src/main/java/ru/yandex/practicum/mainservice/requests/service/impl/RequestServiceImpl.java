package ru.yandex.practicum.mainservice.requests.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mainservice.event.model.Event;
import ru.yandex.practicum.mainservice.event.repository.EventRepository;
import ru.yandex.practicum.mainservice.exceptions.EntitiesConflictException;
import ru.yandex.practicum.mainservice.exceptions.EntityNotFoundException;
import ru.yandex.practicum.mainservice.requests.dto.ParticipationRequestDto;
import ru.yandex.practicum.mainservice.requests.model.Request;
import ru.yandex.practicum.mainservice.requests.repository.RequestRepository;
import ru.yandex.practicum.mainservice.requests.service.RequestService;
import ru.yandex.practicum.mainservice.user.model.User;
import ru.yandex.practicum.mainservice.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static ru.yandex.practicum.mainservice.event.enums.EventState.PUBLISHED;
import static ru.yandex.practicum.mainservice.requests.dto.RequestMapper.toParticipationRequestDto;
import static ru.yandex.practicum.mainservice.requests.enums.RequestStatus.*;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final UserRepository userRepository;

    private final EventRepository eventRepository;

    private final RequestRepository requestRepository;


    @Transactional
    @Override
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("User with id=%s was not found", userId)));
        Event event = eventRepository
                .findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Event with id=%s was not found", eventId)));
        Request maybeNewRequest = makeRequest(user, event);
        validateRequest(maybeNewRequest);
        Request newRequest = requestRepository.save(maybeNewRequest);
        return toParticipationRequestDto(newRequest);
    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("User with id=%s was not found", userId)));
        List<Request> userRequests = requestRepository.findAllByRequesterId(user.getId());
        return toParticipationRequestDto(userRequests);
    }

    @Override
    public ParticipationRequestDto canceledRequest(Long userId, Long requestId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("User with id=%s was not found", userId)));
        Request request = requestRepository
                .findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Request with id=%s was not found", requestId)));
        Request canceledRequest = request.toBuilder()
                .status(CANCELED)
                .build();
        return toParticipationRequestDto(requestRepository.save(canceledRequest));
    }

    private void validateRequest(Request request) {
        List<Request> allEventRequests = requestRepository.findAllByEventId(request.getEvent().getId());
        Optional<Request> maybeSavedRequest = allEventRequests.stream()
                .filter(r -> r.getEvent().equals(request.getEvent()) && r.getRequester().equals(request.getRequester()))
                .findFirst();
        long confirmedRequests = allEventRequests.stream()
                .filter(r -> r.getStatus() == CONFIRMED)
                .count();
        if (confirmedRequests >= request.getEvent().getParticipantLimit()
                && request.getEvent().getParticipantLimit() != 0) {
            throw new EntitiesConflictException("The limit of requests for the event has been reached");
        }
        if (maybeSavedRequest.isPresent()) {
            throw new EntitiesConflictException("This request already exist");
        }
        if (request.getRequester().equals(request.getEvent().getInitiator())) {
            throw new EntitiesConflictException("The initiator of the event cannot add a request to participate in his event");
        }
        if (request.getEvent().getState() != PUBLISHED) {
            throw new EntitiesConflictException("You cannot participate in an unpublished event");
        }
    }

    public Request makeRequest(User user, Event event) {
        Request.RequestBuilder builder = Request.builder()
                .created(LocalDateTime.now())
                .requester(user)
                .event(event);
        if (!event.isRequestModeration() || event.getParticipantLimit() == 0) {
            builder.status(CONFIRMED);
        } else {
            builder.status(PENDING);
        }
        return builder.build();
    }
}
