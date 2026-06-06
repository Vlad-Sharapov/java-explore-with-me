package ru.yandex.practicum.mainservice.event.service.admin.impl;


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
import ru.yandex.practicum.mainservice.category.model.Category;
import ru.yandex.practicum.mainservice.category.repository.CategoryRepository;
import ru.yandex.practicum.mainservice.event.dto.EventFullDto;
import ru.yandex.practicum.mainservice.event.dto.UpdateEventAdminRequest;
import ru.yandex.practicum.mainservice.event.enums.EventState;
import ru.yandex.practicum.mainservice.event.enums.adminenum.AdmStateAction;
import ru.yandex.practicum.mainservice.event.model.Event;
import ru.yandex.practicum.mainservice.event.model.GetEventsForAdminRequest;
import ru.yandex.practicum.mainservice.event.model.Location;
import ru.yandex.practicum.mainservice.event.repository.EventRepository;
import ru.yandex.practicum.mainservice.event.stateclient.ClientHandler;
import ru.yandex.practicum.mainservice.exceptions.BadRequestException;
import ru.yandex.practicum.mainservice.exceptions.EntitiesConflictException;
import ru.yandex.practicum.mainservice.exceptions.EntityNotFoundException;
import ru.yandex.practicum.mainservice.place.repository.PlaceRepository;
import ru.yandex.practicum.mainservice.requests.model.Request;
import ru.yandex.practicum.mainservice.requests.repository.RequestRepository;
import ru.yandex.practicum.mainservice.user.model.User;
import ru.yandex.practicum.statdto.StatsDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.yandex.practicum.mainservice.EventTestData.*;
import static ru.yandex.practicum.mainservice.requests.enums.RequestStatus.CONFIRMED;

@ExtendWith(MockitoExtension.class)
class EventAdminServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ClientHandler clientHandler;

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private PlaceRepository placeRepository;

    @InjectMocks
    private EventAdminServiceImpl eventAdminService;

    @Test
    void shouldUpdateEventWithStatusCanceledWhenAdmStateActionIsRejectEvent() {
        User user = makeUser(1L);
        Category oldCategory = makeCategory(1L);
        Category newCategory = makeCategory(2L);
        Location location = makeLocation(1L);

        Event event = makeEvent(user, oldCategory, location)
                .createOn(FIXED_TIME)
                .build();

        UpdateEventAdminRequest updateEvent = UpdateEventAdminRequest.builder()
                .title("New title")
                .annotation("New valid annotation text")
                .category(newCategory.getId())
                .stateAction(AdmStateAction.REJECT_EVENT)
                .build();


        Mockito.when(eventRepository.findById(event.getId()))
                .thenReturn(Optional.of(event));
        Mockito.when(categoryRepository.findById(newCategory.getId()))
                .thenReturn(Optional.of(newCategory));
        Mockito.when(eventRepository.save(Mockito.any(Event.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Mockito.when(requestRepository.countAllByStatusAndEventId(CONFIRMED, event.getId())).thenReturn(3L);

        EventFullDto result = eventAdminService.update(event.getId(), updateEvent);

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        Mockito.verify(eventRepository).save(eventCaptor.capture());

        Event savedEvent = eventCaptor.getValue();

        assertThat(savedEvent.getTitle(), equalTo("New title"));
        assertThat(savedEvent.getAnnotation(), equalTo("New valid annotation text"));
        assertThat(savedEvent.getCategory(), equalTo(newCategory));
        assertThat(savedEvent.getState(), equalTo(EventState.CANCELED));

        assertThat(result.getTitle(), equalTo("New title"));
        assertThat(result.getConfirmedRequests(), equalTo(3L));

    }

    @Test
    void shouldEventStatusPublishedWhenAdmStateActionIsPublishEvent() {
        User user = makeUser(1L);
        Category oldCategory = makeCategory(1L);
        Category newCategory = makeCategory(2L);
        Location location = makeLocation(1L);

        Event event = makeEvent(user, oldCategory, location)
                .createOn(FIXED_TIME)
                .build();

        UpdateEventAdminRequest updateEvent = UpdateEventAdminRequest.builder()
                .title("New title")
                .annotation("New valid annotation text")
                .category(newCategory.getId())
                .stateAction(AdmStateAction.PUBLISH_EVENT)
                .build();


        Mockito.when(eventRepository.findById(event.getId()))
                .thenReturn(Optional.of(event));
        Mockito.when(categoryRepository.findById(newCategory.getId()))
                .thenReturn(Optional.of(newCategory));
        Mockito.when(eventRepository.save(Mockito.any(Event.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Mockito.when(requestRepository.countAllByStatusAndEventId(CONFIRMED, event.getId())).thenReturn(3L);

        EventFullDto result = eventAdminService.update(event.getId(), updateEvent);

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        Mockito.verify(eventRepository).save(eventCaptor.capture());

        Event savedEvent = eventCaptor.getValue();

        assertThat(savedEvent.getTitle(), equalTo("New title"));
        assertThat(savedEvent.getAnnotation(), equalTo("New valid annotation text"));
        assertThat(savedEvent.getCategory(), equalTo(newCategory));
        assertThat(savedEvent.getState(), equalTo(EventState.PUBLISHED));

        assertThat(result.getTitle(), equalTo("New title"));
        assertThat(result.getConfirmedRequests(), equalTo(3L));
    }

    @Test
    void shouldThrowWhenEventNotFound() {
        User user = makeUser(1L);

        Mockito.when(eventRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        EntityNotFoundException entityNotFoundException = assertThrows(EntityNotFoundException.class,
                () -> eventAdminService.update(1L, new UpdateEventAdminRequest()));
        assertThat(entityNotFoundException.getMessage(), containsString("Event with id=1 was not found"));
    }


    @Test
    void shouldThrowWhenCategoryNotFound() {
        User initiator = makeUser(1L);
        Category category = makeCategory(1L);
        Location location = makeLocation(1L);
        Event event = makeEvent(initiator, category, location)
                .build();
        UpdateEventAdminRequest updateEvent = UpdateEventAdminRequest.builder()
                .title("New title")
                .annotation("New valid annotation text")
                .stateAction(AdmStateAction.PUBLISH_EVENT)
                .category(99L)
                .build();
        Mockito.when(eventRepository.findById(event.getId()))
                .thenReturn(Optional.of(event));

        EntityNotFoundException entityNotFoundException = assertThrows(EntityNotFoundException.class, () ->
                eventAdminService.update(event.getId(), updateEvent));
        assertThat(entityNotFoundException.getMessage(),
                containsString("Category not found"));
    }


    @Test
    void shouldThrowWhenEventDateEarlierThanHourOfPublication() {
        User initiator = makeUser(1L);
        Category category = makeCategory(1L);
        Location location = makeLocation(1L);
        Event event = makeEvent(initiator, category, location)
                .publishedOn(FIXED_TIME.plusHours(1))
                .state(EventState.PUBLISHED)
                .build();
        UpdateEventAdminRequest updateEvent = UpdateEventAdminRequest.builder()
                .title("New title")
                .annotation("New valid annotation text")
                .eventDate(FIXED_TIME.plusHours(3))
                .build();
        Mockito.when(eventRepository.findById(event.getId()))
                .thenReturn(Optional.of(event));

        EntitiesConflictException entitiesConflictException = assertThrows(EntitiesConflictException.class, () ->
                eventAdminService.update(event.getId(), updateEvent));
        assertThat(entitiesConflictException.getMessage(),
                containsString("the start date of the event to be modified must be no earlier than an hour from the date of publication"));

    }

    @Test
    void shouldThrowWhenAdminStateActionIsRejectEventButEventPublished() {

        User initiator = makeUser(1L);
        Category category = makeCategory(1L);
        Location location = makeLocation(1L);
        Event event = makeEvent(initiator, category, location)
                .publishedOn(FIXED_TIME.plusHours(1))
                .state(EventState.PUBLISHED)
                .build();
        UpdateEventAdminRequest updateEvent = UpdateEventAdminRequest.builder()
                .title("New title")
                .annotation("New valid annotation text")
                .stateAction(AdmStateAction.REJECT_EVENT)
                .build();
        Mockito.when(eventRepository.findById(event.getId()))
                .thenReturn(Optional.of(event));

        EntitiesConflictException entitiesConflictException = assertThrows(EntitiesConflictException.class, () ->
                eventAdminService.update(event.getId(), updateEvent));
        assertThat(entitiesConflictException.getMessage(),
                containsString("Cannot reject the event because it's not in the right state: PUBLISHED"));

    }

    @Test
    void shouldThrowWhenAdminStateActionIsPublishEventButEventCanceled() {
        User initiator = makeUser(1L);
        Category category = makeCategory(1L);
        Location location = makeLocation(1L);
        Event event = makeEvent(initiator, category, location)
                .publishedOn(FIXED_TIME.plusHours(1))
                .state(EventState.CANCELED)
                .build();
        UpdateEventAdminRequest updateEvent = UpdateEventAdminRequest.builder()
                .title("New title")
                .annotation("New valid annotation text")
                .stateAction(AdmStateAction.PUBLISH_EVENT)
                .build();
        Mockito.when(eventRepository.findById(event.getId()))
                .thenReturn(Optional.of(event));

        EntitiesConflictException entitiesConflictException = assertThrows(EntitiesConflictException.class, () ->
                eventAdminService.update(event.getId(), updateEvent));
        assertThat(entitiesConflictException.getMessage(),
                containsString("Cannot publish the event because it's not in the right state: CANCELED"));
    }

    @Test
    void shouldGetEventsWithoutFilters() {
        User initiator = makeUser(1L);
        Category category = makeCategory(1L);
        Location location = makeLocation(1L);
        Event event1 = makeEvent(initiator, category, location)
                .createOn(FIXED_TIME)
                .id(1L)
                .build();
        Event event2 = makeEvent(initiator, category, location)
                .createOn(FIXED_TIME)
                .id(2L)
                .title("event2")
                .build();
        List<Event> events = List.of(event1, event2);

        Request confirmedRequest = makeRequest(1L, event1, makeUser(2L))
                .status(CONFIRMED)
                .build();
        StatsDto stats1 = StatsDto.builder()
                .app("ewm")
                .uri("/events/1")
                .hits(4L)
                .build();
        StatsDto stats2 = StatsDto.builder()
                .app("ewm")
                .uri("/events/2")
                .hits(2L)
                .build();
        GetEventsForAdminRequest request = GetEventsForAdminRequest.builder()
                .from(0)
                .size(10)
                .build();

        Mockito.when(eventRepository.findAll(Mockito.any(PageRequest.class)))
                .thenReturn(new PageImpl<>(events));
        Mockito.when(requestRepository.findAllByStatusAndEventIn(CONFIRMED, events))
                .thenReturn(List.of(confirmedRequest));
        Mockito.when(clientHandler.getStatsForEvents(Mockito.eq(events),
                        Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class)))
                .thenReturn(List.of(stats1, stats2));

        List<EventFullDto> result = eventAdminService.getEvents(request);

        ArgumentCaptor<PageRequest> pageCaptor = ArgumentCaptor.forClass(PageRequest.class);
        Mockito.verify(eventRepository).findAll(pageCaptor.capture());
        Mockito.verify(eventRepository, Mockito.never())
                .findAll(Mockito.any(BooleanExpression.class), Mockito.any(PageRequest.class));

        assertThat(pageCaptor.getValue().getPageNumber(), equalTo(0));
        assertThat(pageCaptor.getValue().getPageSize(), equalTo(10));
        assertThat(result, hasSize(2));
        assertThat(result.get(0).getId(), equalTo(event1.getId()));
        assertThat(result.get(0).getConfirmedRequests(), equalTo(1L));
        assertThat(result.get(0).getViews(), equalTo(4L));
        assertThat(result.get(1).getId(), equalTo(event2.getId()));
        assertThat(result.get(1).getConfirmedRequests(), equalTo(0L));
        assertThat(result.get(1).getViews(), equalTo(2L));
    }

    @Test
    void shouldGetEventsWithFilters() {
        User initiator = makeUser(1L);
        Category category = makeCategory(1L);
        Location location = makeLocation(1L);
        Event event = makeEvent(initiator, category, location)
                .createOn(FIXED_TIME)
                .state(EventState.PUBLISHED)
                .build();
        List<Event> events = List.of(event);
        GetEventsForAdminRequest request = GetEventsForAdminRequest.builder()
                .users(List.of(initiator.getId()))
                .categories(List.of(category.getId()))
                .states(List.of(EventState.PUBLISHED))
                .rangeStart(FIXED_TIME)
                .rangeEnd(FIXED_TIME.plusDays(2))
                .from(0)
                .size(10)
                .build();

        Mockito.when(eventRepository.findAll(Mockito.any(BooleanExpression.class), Mockito.any(PageRequest.class)))
                .thenReturn(new PageImpl<>(events));
        Mockito.when(requestRepository.findAllByStatusAndEventIn(CONFIRMED, events))
                .thenReturn(List.of());
        Mockito.when(clientHandler.getStatsForEvents(Mockito.eq(events),
                        Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class)))
                .thenReturn(List.of());

        List<EventFullDto> result = eventAdminService.getEvents(request);

        Mockito.verify(eventRepository).findAll(Mockito.any(BooleanExpression.class), Mockito.any(PageRequest.class));
        Mockito.verify(eventRepository, Mockito.never()).findAll(Mockito.any(PageRequest.class));
        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), equalTo(event.getId()));
    }

    @Test
    void shouldUseCorrectPageRequestForGetEvents() {
        GetEventsForAdminRequest request = GetEventsForAdminRequest.builder()
                .from(20)
                .size(10)
                .build();

        Mockito.when(eventRepository.findAll(Mockito.any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));
        Mockito.when(requestRepository.findAllByStatusAndEventIn(CONFIRMED, List.of()))
                .thenReturn(List.of());
        Mockito.when(clientHandler.getStatsForEvents(Mockito.eq(List.of()),
                        Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class)))
                .thenReturn(List.of());

        eventAdminService.getEvents(request);

        ArgumentCaptor<PageRequest> pageCaptor = ArgumentCaptor.forClass(PageRequest.class);
        Mockito.verify(eventRepository).findAll(pageCaptor.capture());
        assertThat(pageCaptor.getValue().getPageNumber(), equalTo(2));
        assertThat(pageCaptor.getValue().getPageSize(), equalTo(10));
    }

    @Test
    void shouldThrowWhenRangeStartAfterRangeEnd() {
        GetEventsForAdminRequest request = GetEventsForAdminRequest.builder()
                .rangeStart(FIXED_TIME.plusDays(2))
                .rangeEnd(FIXED_TIME)
                .from(0)
                .size(10)
                .build();

        assertThrows(BadRequestException.class, () -> eventAdminService.getEvents(request));

        Mockito.verify(eventRepository, Mockito.never()).findAll(Mockito.any(PageRequest.class));
        Mockito.verify(eventRepository, Mockito.never())
                .findAll(Mockito.any(BooleanExpression.class), Mockito.any(PageRequest.class));
        Mockito.verify(requestRepository, Mockito.never()).findAllByStatusAndEventIn(Mockito.any(), Mockito.any());
        Mockito.verify(clientHandler, Mockito.never()).getStatsForEvents(Mockito.any(), Mockito.any(), Mockito.any());
    }


}
