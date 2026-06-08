package ru.yandex.practicum.mainservice.requests.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.mainservice.category.model.Category;
import ru.yandex.practicum.mainservice.event.enums.EventState;
import ru.yandex.practicum.mainservice.event.model.Event;
import ru.yandex.practicum.mainservice.event.model.Location;
import ru.yandex.practicum.mainservice.event.repository.EventRepository;
import ru.yandex.practicum.mainservice.exceptions.EntitiesConflictException;
import ru.yandex.practicum.mainservice.exceptions.EntityNotFoundException;
import ru.yandex.practicum.mainservice.requests.dto.ParticipationRequestDto;
import ru.yandex.practicum.mainservice.requests.model.Request;
import ru.yandex.practicum.mainservice.requests.repository.RequestRepository;
import ru.yandex.practicum.mainservice.user.model.User;
import ru.yandex.practicum.mainservice.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.yandex.practicum.mainservice.event.enums.EventState.PUBLISHED;
import static ru.yandex.practicum.mainservice.requests.enums.RequestStatus.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {

    private final LocalDateTime timeStart = LocalDateTime.now();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    @InjectMocks
    private RequestServiceImpl requestService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private RequestRepository requestRepository;

    @Test
    void shouldAddRequestWithPendingWhenRequestModerationEnabledAndParticipantLimitExists() {
        User initiator = makeUser(1L);
        User user = makeUser(2L);
        Event event = publishedEventBuilder(initiator)
                .requestModeration(true)
                .participantLimit(5)
                .build();
        Request newRequest = makeRequest(1L, event, user)
                .status(PENDING)
                .build();
        Mockito.when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));

        Mockito.when(eventRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(event));

        Mockito.when(requestRepository.save(Mockito.any()))
                .thenReturn(newRequest);

        ParticipationRequestDto participationRequestDto = requestService.addRequest(user.getId(), event.getId());

        ArgumentCaptor<Request> captor = ArgumentCaptor.forClass(Request.class);
        Mockito.verify(requestRepository).save(captor.capture());
        Request saveRequest = captor.getValue();

        assertThat(PENDING, equalTo(saveRequest.getStatus()));
        assertThat(saveRequest.getRequester(), equalTo(user));
        assertThat(saveRequest.getEvent(), equalTo(event));

        assertThat(participationRequestDto, allOf(
                hasProperty("created", equalTo(newRequest.getCreated()
                        .format(formatter))),
                hasProperty("event", equalTo(event.getId())),
                hasProperty("requester", equalTo(user.getId())),
                hasProperty("status", equalTo(PENDING))));
    }

    @Test
    void shouldAddRequestWithCompletedWhenRequestModerationFalseAndParticipantLimitBlank() {
        User initiator = makeUser(1L);
        User user = makeUser(2L);

        Event event = publishedEventBuilder(initiator)
                .participantLimit(0)
                .requestModeration(false)
                .build();

        Request newRequest = makeRequest(1L, event, user)
                .status(CONFIRMED)
                .build();

        Mockito.when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));

        Mockito.when(eventRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(event));

        Mockito.when(requestRepository.save(Mockito.any()))
                .thenReturn(newRequest);

        ParticipationRequestDto participationRequestDto = requestService.addRequest(initiator.getId(), event.getId());

        ArgumentCaptor<Request> captor = ArgumentCaptor.forClass(Request.class);

        Mockito.verify(requestRepository).save(captor.capture());
        Request saveRequest = captor.getValue();

        assertThat(CONFIRMED, equalTo(saveRequest.getStatus()));

        assertThat(saveRequest.getRequester(), equalTo(user));
        assertThat(saveRequest.getEvent(), equalTo(event));

        assertThat(participationRequestDto, allOf(
                hasProperty("created", equalTo(newRequest.getCreated()
                        .format(formatter))),
                hasProperty("event", equalTo(event.getId())),
                hasProperty("requester", equalTo(user.getId())),
                hasProperty("status", equalTo(CONFIRMED))
        ));
    }

    @Test
    void shouldThrowEntitiesConflictExceptionWhenRequestAlreadyExist() {
        User initiator = makeUser(1L);
        User user = makeUser(2L);
        Event event = publishedEventBuilder(initiator)
                .requestModeration(true)
                .participantLimit(5)
                .build();

        Request newRequest = makeRequest(1L, event, user)
                .status(PENDING)
                .build();

        Mockito.when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));

        Mockito.when(eventRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(event));

        Mockito.when(requestRepository.findAllByEventId(Mockito.anyLong()))
                .thenReturn(List.of(newRequest));

        EntitiesConflictException entitiesConflictException = assertThrows(EntitiesConflictException.class,
                () -> requestService.addRequest(user.getId(), event.getId()));

        assertThat("This request already exist", equalTo(entitiesConflictException.getMessage()));
    }


    @Test
    void shouldThrowEntitiesConflictExceptionWhenRequestsEventReached() {

        User initiator = makeUser(1L);
        User user = makeUser(2L);
        User user2 = makeUser(3L);
        Event event = publishedEventBuilder(initiator)
                .requestModeration(true)
                .participantLimit(1)
                .build();

        Request newRequest = makeRequest(1L, event, user)
                .status(CONFIRMED)
                .build();

        Mockito.when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));

        Mockito.when(eventRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(event));

        Mockito.when(requestRepository.findAllByEventId(Mockito.anyLong()))
                .thenReturn(List.of(newRequest));

        EntitiesConflictException entitiesConflictException = assertThrows(EntitiesConflictException.class,
                () -> requestService.addRequest(user2.getId(), event.getId()));

        assertThat("The limit of requests for the event has been reached", equalTo(entitiesConflictException.getMessage()));

    }

    @Test
    void shouldThrowEntitiesConflictExceptionWhenAddRequestToHisEvent() {
        User initiator = makeUser(1L);
        Event event = publishedEventBuilder(initiator)
                .requestModeration(true)
                .participantLimit(5)
                .build();

        Mockito.when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(initiator));

        Mockito.when(eventRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(event));

        Mockito.when(requestRepository.findAllByEventId(Mockito.anyLong()))
                .thenReturn(List.of());

        EntitiesConflictException entitiesConflictException = assertThrows(EntitiesConflictException.class,
                () -> requestService.addRequest(initiator.getId(), event.getId()));

        assertThat("The initiator of the event cannot add a request to participate in his event",
                equalTo(entitiesConflictException.getMessage()));
    }

    @Test
    void shouldThrowEntitiesConflictExceptionWhenAddRequestToNotPublishedEvent() {
        User initiator = makeUser(1L);
        User user = makeUser(2L);
        Event event = publishedEventBuilder(initiator)
                .state(EventState.PENDING)
                .requestModeration(true)
                .participantLimit(5)
                .build();

        Mockito.when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));

        Mockito.when(eventRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(event));

        Mockito.when(requestRepository.findAllByEventId(Mockito.anyLong()))
                .thenReturn(List.of());

        EntitiesConflictException entitiesConflictException = assertThrows(EntitiesConflictException.class,
                () -> requestService.addRequest(user.getId(), event.getId()));

        assertThat("You cannot participate in an unpublished event",
                equalTo(entitiesConflictException.getMessage()));
    }


    @Test
    void getUserRequests() {
        User initiator = makeUser(1L);
        Event event = publishedEventBuilder(initiator)
                .state(EventState.PENDING)
                .requestModeration(true)
                .participantLimit(5)
                .build();

        Mockito.when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(initiator));

        Mockito.when(requestRepository.findAllByRequesterId(Mockito.anyLong()))
                .thenReturn(List.of(makeRequest(1L, event, initiator).build()));

        List<ParticipationRequestDto> userRequests = requestService.getUserRequests(initiator.getId());

        assertThat(userRequests.size(), equalTo(1));

        assertThat(userRequests.get(0), allOf(
                hasProperty("event", equalTo(event.getId())),
                hasProperty("requester", equalTo(initiator.getId()))
        ));
    }


    @Test
    void shouldThrowEntityNotFoundExceptionWhenUserIdNotFound() {
        User initiator = makeUser(1L);
        Event event = publishedEventBuilder(initiator)
                .state(EventState.PENDING)
                .requestModeration(true)
                .participantLimit(5)
                .build();

        Mockito.when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> requestService.getUserRequests(999L));
    }


    @Test
    void canceledRequest() {
        User initiator = makeUser(1L);
        User user = makeUser(2L);

        Event event = publishedEventBuilder(initiator)
                .participantLimit(0)
                .requestModeration(false)
                .build();

        Request newRequest = makeRequest(1L, event, user)
                .status(PENDING)
                .build();

        Request canceledRequest = makeRequest(1L, event, user)
                .status(CANCELED)
                .build();

        Mockito.when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));

        Mockito.when(requestRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(newRequest));

        Mockito.when(requestRepository.save(Mockito.any()))
                .thenReturn(canceledRequest);

        ParticipationRequestDto participationRequestDto = requestService
                .canceledRequest(user.getId(), newRequest.getId());

        ArgumentCaptor<Request> captor = ArgumentCaptor.forClass(Request.class);


        Mockito.verify(requestRepository,
                        Mockito.times(1))
                .save(captor.capture());

        Request saveRequest = captor.getValue();

        assertThat(CANCELED, equalTo(saveRequest.getStatus()));
        assertThat(event, equalTo(saveRequest.getEvent()));
        assertThat(user, equalTo(saveRequest.getRequester()));

        assertThat(participationRequestDto, allOf(
                hasProperty("event", equalTo(event.getId())),
                hasProperty("requester", equalTo(user.getId())),
                hasProperty("status", equalTo(CANCELED))
        ));
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenUserNotFound() {
        User initiator = makeUser(1L);
        User user = makeUser(2L);

        Event event = publishedEventBuilder(initiator)
                .participantLimit(0)
                .requestModeration(false)
                .build();

        Request newRequest = makeRequest(1L, event, user)
                .status(CONFIRMED)
                .build();

        Mockito.when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> requestService.canceledRequest(initiator.getId(), newRequest.getId()));
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenRequestNotFound() {
        User initiator = makeUser(1L);
        User user = makeUser(2L);

        Event event = publishedEventBuilder(initiator)
                .participantLimit(0)
                .requestModeration(false)
                .build();

        Request newRequest = makeRequest(1L, event, user)
                .status(CONFIRMED)
                .build();

        Mockito.when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));

        Mockito.when(requestRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());


        assertThrows(EntityNotFoundException.class,
                () -> requestService.canceledRequest(initiator.getId(), newRequest.getId()));

    }

    private User makeUser(Long id) {
        return User.builder()
                .id(id)
                .name("Ben")
                .email("user" + id + "@a.ru")
                .build();
    }

    private Event.EventBuilder publishedEventBuilder(User initiator) {
        return Event.builder()
                .id(1L)
                .initiator(initiator)
                .annotation("annotation")
                .title("event1")
                .category(Category.builder()
                        .id(1L)
                        .name("drama")
                        .build())
                .publishedOn(timeStart.plusHours(5))
                .description("qwer tyui op[a sdfg hjkl;x")
                .eventDate(timeStart.plusHours(24))
                .location(Location.builder()
                        .id(1L)
                        .lon(50.00)
                        .lat(50.00)
                        .build())
                .paid(false)
                .state(PUBLISHED);
    }

    private Request.RequestBuilder makeRequest(Long id, Event event, User user) {
        return Request.builder()
                .id(id)
                .event(event)
                .requester(user)
                .created(LocalDateTime.now())
                .status(PENDING);

    }
}
