package ru.yandex.practicum.mainservice.event.service.privates.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.yandex.practicum.mainservice.EventTestData;
import ru.yandex.practicum.mainservice.category.model.Category;
import ru.yandex.practicum.mainservice.category.repository.CategoryRepository;
import ru.yandex.practicum.mainservice.event.dto.*;
import ru.yandex.practicum.mainservice.event.enums.EventState;
import ru.yandex.practicum.mainservice.event.model.Event;
import ru.yandex.practicum.mainservice.event.model.Location;
import ru.yandex.practicum.mainservice.event.repository.EventRepository;
import ru.yandex.practicum.mainservice.event.stateclient.ClientHandler;
import ru.yandex.practicum.mainservice.exceptions.EntitiesConflictException;
import ru.yandex.practicum.mainservice.exceptions.EntityNotFoundException;
import ru.yandex.practicum.mainservice.requests.dto.EventRequestStatusUpdateResult;
import ru.yandex.practicum.mainservice.requests.dto.ParticipationRequestDto;
import ru.yandex.practicum.mainservice.requests.enums.RequestStatus;
import ru.yandex.practicum.mainservice.requests.model.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.mainservice.requests.model.Request;
import ru.yandex.practicum.mainservice.requests.repository.RequestRepository;
import ru.yandex.practicum.mainservice.user.model.User;
import ru.yandex.practicum.mainservice.user.repository.UserRepository;
import ru.yandex.practicum.statdto.StatsDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.yandex.practicum.mainservice.EventTestData.*;
import static ru.yandex.practicum.mainservice.requests.enums.RequestStatus.CONFIRMED;
import static ru.yandex.practicum.mainservice.requests.enums.RequestStatus.PENDING;


@ExtendWith(MockitoExtension.class)
class EventPrivateServiceImplTest {


    @InjectMocks
    private EventPrivateServiceImpl eventPrivateService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private ClientHandler clientHandler;

    @Test
    void shouldAddNewEvent() {

        Category category = EventTestData.makeCategory(1L);
        Location location = makeLocation(1L);
        User user = makeUser(1L);

        NewEventDto newEventDto = makeEventDto(category, location)
                .build();

        Event event = EventMapper.toEvent(newEventDto, user, category);

        Mockito.when(categoryRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(category));

        Mockito.when(eventRepository.save(Mockito.any(Event.class)))
                .thenAnswer(invocation -> {
                            Event resultEvent = invocation.getArgument(0);
                            resultEvent.setId(1L);
                            return resultEvent;
                        }
                );

        Mockito.when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));

        EventFullDto resultEvent = eventPrivateService.add(user.getId(), newEventDto);

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        Mockito.verify(eventRepository).save(captor.capture());
        Event capturedEvent = captor.getValue();

        assertThat(capturedEvent, allOf(

                hasProperty("title", equalTo(newEventDto.getTitle())),
                hasProperty("initiator", equalTo(user)),
                hasProperty("annotation", equalTo(newEventDto.getAnnotation())),
                hasProperty("category", equalTo(category)),
                hasProperty("description", equalTo(newEventDto.getDescription())),
                hasProperty("location", equalTo(location)),
                hasProperty("paid", equalTo(newEventDto.getPaid())),
                hasProperty("participantLimit", equalTo(newEventDto.getParticipantLimit())),
                hasProperty("requestModeration", equalTo(newEventDto.getRequestModeration())),
                hasProperty("state", equalTo(EventState.PENDING))
        ));

        assertThat(resultEvent, allOf(
                hasProperty("id", equalTo(1L)),
                hasProperty("title", equalTo(event.getTitle()))
        ));
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenUserNotFound() {


        Mockito.when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        EntityNotFoundException entityNotFoundException = assertThrows(EntityNotFoundException.class,
                () -> eventPrivateService.add(1L, new NewEventDto()));

        assertThat(entityNotFoundException.getMessage(), containsString("User not found"));
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenCategoryNotFound() {

        User user = makeUser(1L);

        NewEventDto newEventDto = makeEventDto(makeCategory(1L), makeLocation(1L))
                .build();

        Mockito.when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));

        Mockito.when(categoryRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        EntityNotFoundException entityNotFoundException = assertThrows(EntityNotFoundException.class,
                () -> eventPrivateService.add(1L, newEventDto));

        assertThat(entityNotFoundException.getMessage(), containsString(String
                .format("Category with id=%s was not found", newEventDto.getCategory())));

    }


    @Test
    void shouldUpdateUserEvent() {
        User user = makeUser(1L);
        Category oldCategory = makeCategory(1L);
        Category newCategory = makeCategory(2L);
        Location location = makeLocation(1L);

        Event event = makeEvent(user, oldCategory, location)
                .createOn(FIXED_TIME)
                .build();

        UpdateEventUserRequest updateRequest = UpdateEventUserRequest.builder()
                .title("New title")
                .annotation("New valid annotation text")
                .category(newCategory.getId())
                .build();

        Mockito.when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        Mockito.when(eventRepository.findById(event.getId()))
                .thenReturn(Optional.of(event));
        Mockito.when(categoryRepository.findById(newCategory.getId()))
                .thenReturn(Optional.of(newCategory));
        Mockito.when(eventRepository.save(Mockito.any(Event.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Mockito.when(requestRepository.countAllByStatusAndEventId(CONFIRMED, event.getId())).thenReturn(3L);

        EventFullDto result = eventPrivateService.update(user.getId(), event.getId(), updateRequest);

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        Mockito.verify(eventRepository).save(eventCaptor.capture());

        Event savedEvent = eventCaptor.getValue();

        assertThat(savedEvent.getTitle(), equalTo("New title"));
        assertThat(savedEvent.getAnnotation(), equalTo("New valid annotation text"));
        assertThat(savedEvent.getCategory(), equalTo(newCategory));
        assertThat(savedEvent.getState(), equalTo(EventState.PENDING));

        assertThat(result.getTitle(), equalTo("New title"));
        assertThat(result.getConfirmedRequests(), equalTo(3L));
    }


    @Test
    void shouldThrowWhenEventNotFound() {
        User user = makeUser(1L);

        Mockito.when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        Mockito.when(eventRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        EntityNotFoundException entityNotFoundException = assertThrows(EntityNotFoundException.class,
                () -> eventPrivateService.update(user.getId(), 1L, new UpdateEventUserRequest()));
        assertThat(entityNotFoundException.getMessage(), containsString("Event with id=1 was not found"));
    }

    @Test
    void shouldThrowWhenUserIsNotInitiator() {
        User initiator = makeUser(1L);
        User user = makeUser(2L);
        Category category = makeCategory(1L);
        Location location = makeLocation(1L);
        Event event = makeEvent(initiator, category, location)
                .createOn(FIXED_TIME)
                .build();
        Mockito.when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        Mockito.when(eventRepository.findById(event.getId()))
                .thenReturn(Optional.of(event));

        EntitiesConflictException entitiesConflictException = assertThrows(EntitiesConflictException.class, () ->
                eventPrivateService.update(user.getId(), event.getId(), new UpdateEventUserRequest()));
        assertThat(entitiesConflictException.getMessage(),
                containsString("The user can only change his own events"));
    }

    @Test
    void shouldThrowWhenEventIsPublished() {
        User initiator = makeUser(1L);
        Category category = makeCategory(1L);
        Location location = makeLocation(1L);
        Event event = makeEvent(initiator, category, location)
                .state(EventState.PUBLISHED)
                .build();
        Mockito.when(userRepository.findById(initiator.getId()))
                .thenReturn(Optional.of(initiator));
        Mockito.when(eventRepository.findById(event.getId()))
                .thenReturn(Optional.of(event));

        EntitiesConflictException entitiesConflictException = assertThrows(EntitiesConflictException.class, () ->
                eventPrivateService.update(initiator.getId(), event.getId(), new UpdateEventUserRequest()));
        assertThat(entitiesConflictException.getMessage(),
                containsString("Event must not be published"));
    }


    @Test
    void shouldGetAllUserEvents() {
        User initiator = makeUser(1L);
        Category category = makeCategory(1L);
        Location location = makeLocation(1L);
        Event event = makeEvent(initiator, category, location)
                .build();
        Event event1 = makeEvent(initiator, category, location)
                .id(2L)
                .build();

        StatsDto statsDto1 = StatsDto.builder()
                .uri("/events/1")
                .hits(4L)
                .app("ewm")
                .build();

        StatsDto statsDto2 = StatsDto.builder()
                .uri("/events/2")
                .hits(2L)
                .app("ewm")
                .build();

        List<Event> events = List.of(event, event1);

        Mockito.when(userRepository.findById(initiator.getId())).thenReturn(Optional.of(initiator));
        Mockito.when(eventRepository.findAll(Mockito.any(BooleanExpression.class), Mockito.any(PageRequest.class)))
                .thenReturn(new PageImpl<>(events));
        Mockito.when(requestRepository.findAllByStatusAndEventIn(CONFIRMED, events))
                .thenReturn(List.of());

        Mockito.when(clientHandler
                        .getStatsForEvents(Mockito.eq(events), Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class)))
                .thenReturn(List.of(statsDto1, statsDto2));

        List<EventShortDto> result = eventPrivateService.getAllUserEvents(initiator.getId(), 0, 10);

        ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);

        Mockito.verify(eventRepository).findAll(Mockito.any(BooleanExpression.class), pageRequestCaptor.capture());

        assertThat(pageRequestCaptor.getValue().getPageNumber(), equalTo(0));
        assertThat(pageRequestCaptor.getValue().getPageSize(), equalTo(10));

        assertThat(result, hasSize(2));
        assertThat(result.get(0).getId(), equalTo(1L));
        assertThat(result.get(0).getViews(), equalTo(4L));
        assertThat(result.get(1).getId(), equalTo(2L));
        assertThat(result.get(1).getViews(), equalTo(2L));

    }

    @Test
    void shouldUseCorrectPageRequest() {
        User user = makeUser(1L);
        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        Mockito.when(eventRepository.findAll(Mockito.any(BooleanExpression.class), Mockito.any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));
        Mockito.when(requestRepository.findAllByStatusAndEventIn(CONFIRMED, List.of()))
                .thenReturn(List.of());

        Mockito.when(clientHandler.getStatsForEvents(Mockito.any(List.class),
                        Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class)))
                .thenReturn(List.of());

        ArgumentCaptor<PageRequest> pageCaptor = ArgumentCaptor.forClass(PageRequest.class);

        eventPrivateService.getAllUserEvents(user.getId(), 20, 10);

        Mockito.verify(eventRepository).findAll(Mockito.any(BooleanExpression.class), pageCaptor.capture());

        assertThat(pageCaptor.getValue().getPageNumber(), equalTo(2));
        assertThat(pageCaptor.getValue().getPageSize(), equalTo(10));

    }

    @Test
    void getUserEvent() {
        User initiator = makeUser(1L);
        Category category = makeCategory(1L);
        Location location = makeLocation(1L);
        Event event = makeEvent(initiator, category, location)
                .createOn(FIXED_TIME)
                .build();

        StatsDto statsDto1 = StatsDto.builder()
                .uri("/events/1")
                .hits(4L)
                .app("ewm")
                .build();

        Mockito.when(userRepository.findById(initiator.getId()))
                .thenReturn(Optional.of(initiator));
        Mockito.when(eventRepository.findById(event.getId()))
                .thenReturn(Optional.of(event));
        Mockito.when(requestRepository.countAllByStatusAndEventId(CONFIRMED, event.getId()))
                .thenReturn(3L);

        Mockito.when(clientHandler.getStatsForEvent(Mockito.eq(event),
                        Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class)))
                .thenReturn(List.of(statsDto1));

        EventFullDto result = eventPrivateService.getUserEvent(initiator.getId(), event.getId());

        assertThat(result, allOf(
                hasProperty("id", equalTo(event.getId())),
                hasProperty("title", equalTo(event.getTitle())),
                hasProperty("annotation", equalTo(event.getAnnotation())),
                hasProperty("description", equalTo(event.getDescription())),
                hasProperty("createdOn", equalTo("2026-01-10 12:00:00")),
                hasProperty("publishedOn", equalTo("2026-01-10 17:00:00")),
                hasProperty("eventDate", equalTo("2026-01-11 12:00:00")),
                hasProperty("paid", equalTo(event.isPaid())),
                hasProperty("participantLimit", equalTo(event.getParticipantLimit())),
                hasProperty("requestModeration", equalTo(event.isRequestModeration())),
                hasProperty("state", equalTo(event.getState())),
                hasProperty("confirmedRequests", equalTo(3L)),
                hasProperty("views", equalTo(4L))
        ));

        assertThat(result.getCategory(), allOf(
                hasProperty("id", equalTo(category.getId())),
                hasProperty("name", equalTo(category.getName()))
        ));
        assertThat(result.getInitiator(), allOf(
                hasProperty("id", equalTo(initiator.getId())),
                hasProperty("name", equalTo(initiator.getName())),
                hasProperty("email", equalTo(initiator.getEmail()))
        ));
        assertThat(result.getLocation(), allOf(
                hasProperty("lat", equalTo(location.getLat())),
                hasProperty("lon", equalTo(location.getLon()))
        ));

        Mockito.verify(requestRepository).countAllByStatusAndEventId(CONFIRMED, event.getId());
        Mockito.verify(clientHandler).getStatsForEvent(Mockito.eq(event),
                Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class));
    }

    @Test
    void shouldThrowExceptionWhenEventNotFound() {
        User initiator = makeUser(1L);

        Mockito.when(userRepository.findById(initiator.getId()))
                .thenReturn(Optional.of(initiator));
        Mockito.when(eventRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> eventPrivateService.getUserEvent(initiator.getId(), 1L));
    }

    @Test
    void getUserEventRequests() {
        User initiator = makeUser(1L);
        Category category = makeCategory(1L);
        Location location = makeLocation(1L);

        Event event = makeEvent(initiator, category, location)
                .id(1L)
                .state(EventState.PENDING)
                .requestModeration(true)
                .participantLimit(5)
                .build();

        Mockito.when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(initiator));

        Mockito.when(requestRepository.findAllByEventId(Mockito.anyLong()))
                .thenReturn(List.of(makeRequest(1L, event, initiator).build()));

        Mockito.when(eventRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(event));

        List<ParticipationRequestDto> userRequests = eventPrivateService
                .getUserEventRequests(initiator.getId(), event.getId());

        assertThat(userRequests.size(), equalTo(1));

        assertThat(userRequests.get(0), allOf(
                hasProperty("event", equalTo(event.getId())),
                hasProperty("requester", equalTo(initiator.getId())),
                hasProperty("status", equalTo(RequestStatus.PENDING)))
        );
    }

    @Test
    void confirmRequests() {

        User initiator = makeUser(1L);
        User user1 = makeUser(1L);
        User user2 = makeUser(1L);
        Category category = makeCategory(1L);
        Location location = makeLocation(1L);

        Event event = makeEvent(initiator, category, location)
                .state(EventState.PENDING)
                .requestModeration(true)
                .participantLimit(5)
                .build();

        Request request1 = makeRequest(1L, event, user1).build();
        Request request2 = makeRequest(2L, event, user2).build();

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(request1.getId(), request2.getId()));
        updateRequest.setStatus(CONFIRMED);

        Mockito.when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(initiator));

        Mockito.when(eventRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(event));

        Mockito.when(requestRepository.findAllByEventId(Mockito.anyLong()))
                .thenReturn(List.of(request1, request2));

        Mockito.when(requestRepository.saveAll(Mockito.<List<Request>>any()))
                .thenReturn(List.of(request1, request2));

        EventRequestStatusUpdateResult eventRequestStatusUpdateResult = eventPrivateService
                .confirmRequests(initiator.getId(), event.getId(), updateRequest);

        assertThat(eventRequestStatusUpdateResult.getConfirmedRequests().size(), equalTo(2));
        assertThat(eventRequestStatusUpdateResult.getConfirmedRequests().get(0), allOf(
                hasProperty("event", equalTo(event.getId())),
                hasProperty("requester", equalTo(user1.getId())),
                hasProperty("status", equalTo(CONFIRMED)))
        );
        assertThat(eventRequestStatusUpdateResult.getConfirmedRequests().get(1), allOf(
                hasProperty("event", equalTo(event.getId())),
                hasProperty("requester", equalTo(user2.getId())),
                hasProperty("status", equalTo(CONFIRMED)))
        );

        assertThat(eventRequestStatusUpdateResult.getRejectedRequests().size(), equalTo(0));
    }


    @Test
    void shouldThrowWhenRequestIsNotPending() {
        User user = makeUser(1L);
        Event event = makeEvent(user, makeCategory(1L), makeLocation(1L)).build();

        Request confirmedRequest = makeRequest(1L, event, makeUser(2L))
                .status(CONFIRMED)
                .build();

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(confirmedRequest.getId()));
        updateRequest.setStatus(RequestStatus.REJECTED);

        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        Mockito.when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        Mockito.when(requestRepository.findAllByEventId(event.getId()))
                .thenReturn(List.of(confirmedRequest));

        assertThrows(EntitiesConflictException.class,
                () -> eventPrivateService.confirmRequests(user.getId(), event.getId(), updateRequest));

        Mockito.verify(requestRepository, Mockito.never()).saveAll(Mockito.any());
    }

    @Test
    void shouldThrowExceptionWhenConfirmedRequestsIsOverLimit() {
        User user = makeUser(1L);
        Event event = makeEvent(user, makeCategory(1L), makeLocation(1L))
                .participantLimit(2).build();

        Request confirmedRequest = makeRequest(1L, event, makeUser(2L))
                .status(CONFIRMED)
                .build();
        Request confirmedRequest2 = makeRequest(2L, event, makeUser(3L))
                .status(CONFIRMED)
                .build();
        Request pendingRequest3 = makeRequest(3L, event, makeUser(4L))
                .status(PENDING)
                .build();

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(pendingRequest3.getId()));
        updateRequest.setStatus(CONFIRMED);

        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        Mockito.when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        Mockito.when(requestRepository.findAllByEventId(event.getId()))
                .thenReturn(List.of(confirmedRequest, confirmedRequest2, pendingRequest3));

        assertThrows(EntitiesConflictException.class,
                () -> eventPrivateService.confirmRequests(user.getId(), event.getId(), updateRequest));

        Mockito.verify(requestRepository, Mockito.never()).saveAll(Mockito.any());
    }

}
