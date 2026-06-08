package ru.yandex.practicum.mainservice.event.service.publics.impl;

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
import ru.yandex.practicum.mainservice.event.dto.EventFullDto;
import ru.yandex.practicum.mainservice.event.dto.EventShortDto;
import ru.yandex.practicum.mainservice.event.enums.EventState;
import ru.yandex.practicum.mainservice.event.model.Event;
import ru.yandex.practicum.mainservice.event.model.GetEventsForPublicRequest;
import ru.yandex.practicum.mainservice.event.model.Location;
import ru.yandex.practicum.mainservice.event.repository.EventRepository;
import ru.yandex.practicum.mainservice.event.stateclient.ClientHandler;
import ru.yandex.practicum.mainservice.exceptions.BadRequestException;
import ru.yandex.practicum.mainservice.exceptions.EntityNotFoundException;
import ru.yandex.practicum.mainservice.place.model.Place;
import ru.yandex.practicum.mainservice.place.repository.PlaceRepository;
import ru.yandex.practicum.mainservice.requests.enums.RequestStatus;
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
import static ru.yandex.practicum.mainservice.event.model.GetEventsForPublicRequest.Sort.EVENT_DATE;
import static ru.yandex.practicum.mainservice.event.model.GetEventsForPublicRequest.Sort.VIEWS;

@ExtendWith(MockitoExtension.class)
class EventPublicServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ClientHandler clientHandler;

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private PlaceRepository placeRepository;

    @InjectMocks
    private EventPublicServiceImpl eventPublicService;


    @Test
    void shouldGetEvents() {

        User user = EventTestData.makeUser(1L);
        User user1 = EventTestData.makeUser(2L);
        User user2 = EventTestData.makeUser(3L);

        Category category = EventTestData.makeCategory(1L, "category");
        Category category1 = EventTestData.makeCategory(2L, "caory1");

        Location location = EventTestData.makeLocation(1L, 10.2, 30.2);
        Location location1 = EventTestData.makeLocation(2L, 20.2, 40.2);

        Event event = EventTestData.makeEvent(user, category, location)
                .title("title")
                .description("this is my event")
                .participantLimit(2)
                .build();

        Event event1 = EventTestData.makeEvent(user1, category1, location1)
                .id(2L)
                .title("MOROZKINO × GORBUFFET “SHASHLYCHNAYA”")
                .description("Throughout February, Morozkino steps beyond the gallery")
                .build();

        Request request = EventTestData.makeRequest(1L, event1, user2)
                .status(RequestStatus.CONFIRMED)
                .build();


        GetEventsForPublicRequest eventRequest = GetEventsForPublicRequest.builder()
                .from(0)
                .size(10)
                .onlyAvailable(false)
                .build();

        Mockito.when(eventRepository.findAll(Mockito.any(BooleanExpression.class), Mockito.any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(event, event1)));

        Mockito.when(requestRepository.findAllByStatusAndEventIn(Mockito.any(RequestStatus.class), Mockito.anyList()))
                .thenReturn(List.of(request));

        List<EventShortDto> result = eventPublicService.getEvents(eventRequest);

        Mockito.verify(eventRepository, Mockito.times(1))
                .findAll(Mockito.any(BooleanExpression.class), Mockito.any(PageRequest.class));
        Mockito.verify(eventRepository, Mockito.never())
                .findAll(Mockito.any(PageRequest.class));

        assertThat(result.size(), equalTo(2));

        assertThat(result.get(0).getId(), equalTo(1L));
        assertThat(result.get(1).getId(), equalTo(2L));
    }

    @Test
    void shouldGetAvailableEventWhenGetOnlyAvailableTrue() {

        User user = EventTestData.makeUser(1L);
        User user1 = EventTestData.makeUser(2L);
        User user2 = EventTestData.makeUser(3L);

        Category category = EventTestData.makeCategory(1L, "category");
        Category category1 = EventTestData.makeCategory(2L, "caory1");

        Location location = EventTestData.makeLocation(1L, 10.2, 30.2);
        Location location1 = EventTestData.makeLocation(2L, 20.2, 40.2);
        Location location2 = EventTestData.makeLocation(3L, 30.2, 50.2);

        Event event = EventTestData.makeEvent(user, category, location)
                .title("title")
                .description("this is my event")
                .participantLimit(2)
                .state(EventState.PUBLISHED)
                .build();

        Event event1 = EventTestData.makeEvent(user1, category1, location1)
                .id(2L)
                .title("MOROZKINO × GORBUFFET “SHASHLYCHNAYA”")
                .description("Throughout February, Morozkino steps beyond the gallery")
                .state(EventState.PUBLISHED)
                .build();

        Request request = EventTestData.makeRequest(1L, event, user2)
                .status(RequestStatus.CONFIRMED)
                .build();

        Request request2 = EventTestData.makeRequest(2L, event, user1)
                .status(RequestStatus.CONFIRMED)
                .build();

        GetEventsForPublicRequest eventRequest = GetEventsForPublicRequest.builder()
                .text("Gorbuffet")
                .from(0)
                .size(10)
                .paid(false)
                .sort(VIEWS)
                .rangeStart(EventTestData.FIXED_TIME)
                .rangeEnd(EventTestData.FIXED_TIME.plusHours(30))
                .categories(List.of(2L))
                .onlyAvailable(true)
                .build();

        Mockito.when(eventRepository.findAll(Mockito.any(BooleanExpression.class), Mockito.any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(event, event1)));

        Mockito.when(requestRepository.findAllByStatusAndEventIn(Mockito.any(RequestStatus.class), Mockito.anyList()))
                .thenReturn(List.of(request, request2));

        List<EventShortDto> events = eventPublicService.getEvents(eventRequest);

        Mockito.verify(eventRepository, Mockito.times(1))
                .findAll(Mockito.any(BooleanExpression.class), Mockito.any(PageRequest.class));
        Mockito.verify(eventRepository, Mockito.never())
                .findAll(Mockito.any(PageRequest.class));

        assertThat(events.size(), equalTo(1));

        assertThat(events.get(0), allOf(
                hasProperty("initiator", allOf(
                        hasProperty("id", equalTo(user1.getId())),
                        hasProperty("name", equalTo(user1.getName())),
                        hasProperty("email", equalTo(user1.getEmail())))),
                hasProperty("title", equalTo(event1.getTitle())),
                hasProperty("annotation", equalTo(event1.getAnnotation())),
                hasProperty("confirmedRequests", equalTo(0L)),
                hasProperty("category", allOf(
                        hasProperty("id", equalTo(category1.getId())),
                        hasProperty("name", equalTo(category1.getName()))
                )),
                hasProperty("description", equalTo(event1.getDescription())),
                hasProperty("eventDate", equalTo(event1.getEventDate().format(EventTestData.FORMATTER))),
                hasProperty("paid", equalTo(event1.isPaid()))
        ));


    }

    @Test
    void shouldThrowWhenRangeStartAfterRangeEnd() {

        GetEventsForPublicRequest eventRequest = GetEventsForPublicRequest.builder()
                .text("Gorbuffet")
                .from(0)
                .size(10)
                .paid(false)
                .sort(VIEWS)
                .rangeStart(EventTestData.FIXED_TIME)
                .rangeEnd(EventTestData.FIXED_TIME.minusHours(30))
                .categories(List.of(2L))
                .onlyAvailable(true)
                .build();


        assertThrows(BadRequestException.class,
                () -> eventPublicService.getEvents(eventRequest));

    }


    @Test
    void shouldSortByEventDate() {
        User user = EventTestData.makeUser(1L);
        User user1 = EventTestData.makeUser(2L);
        User user2 = EventTestData.makeUser(3L);

        Category category = EventTestData.makeCategory(1L, "category");
        Category category1 = EventTestData.makeCategory(2L, "caory1");

        Location location = EventTestData.makeLocation(1L, 10.2, 30.2);
        Location location1 = EventTestData.makeLocation(2L, 20.2, 40.2);

        Event event = EventTestData.makeEvent(user, category, location)
                .title("title")
                .description("this is my event")
                .participantLimit(2)
                .eventDate(EventTestData.FIXED_TIME.plusHours(30))
                .state(EventState.PUBLISHED)
                .build();

        Event event1 = EventTestData.makeEvent(user1, category1, location1)
                .id(2L)
                .title("MOROZKINO × GORBUFFET “SHASHLYCHNAYA”")
                .description("Throughout February, Morozkino steps beyond the gallery")
                .eventDate(EventTestData.FIXED_TIME.plusHours(20))
                .state(EventState.PUBLISHED)
                .build();

        Request request = EventTestData.makeRequest(1L, event, user2)
                .status(RequestStatus.CONFIRMED)
                .build();

        Request request2 = EventTestData.makeRequest(2L, event, user1)
                .status(RequestStatus.CONFIRMED)
                .build();

        GetEventsForPublicRequest eventRequest = GetEventsForPublicRequest.builder()
                .text("Gorbuffet")
                .from(0)
                .size(10)
                .paid(false)
                .sort(EVENT_DATE)
                .rangeStart(EventTestData.FIXED_TIME)
                .rangeEnd(EventTestData.FIXED_TIME.plusHours(30))
                .categories(List.of(2L))
                .onlyAvailable(true)
                .build();

        Mockito.when(eventRepository.findAll(Mockito.any(BooleanExpression.class), Mockito.any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(event, event1)));

        Mockito.when(requestRepository.findAllByStatusAndEventIn(Mockito.any(RequestStatus.class), Mockito.anyList()))
                .thenReturn(List.of(request, request2));

        eventPublicService.getEvents(eventRequest);

        Mockito.verify(eventRepository, Mockito.times(1))
                .findAll(Mockito.any(BooleanExpression.class), Mockito.any(PageRequest.class));
        Mockito.verify(eventRepository, Mockito.never())
                .findAll(Mockito.any(PageRequest.class));

        ArgumentCaptor<PageRequest> pageCaptor = ArgumentCaptor.forClass(PageRequest.class);

        Mockito.verify(eventRepository).findAll(Mockito.any(BooleanExpression.class), pageCaptor.capture());

        assertThat(pageCaptor.getValue().getSort().getOrderFor("eventDate")
                .isAscending(), equalTo(true));
    }

    @Test
    void shouldSortByViews() {
        User user = EventTestData.makeUser(1L);
        User user1 = EventTestData.makeUser(2L);

        Category category = EventTestData.makeCategory(1L, "category");
        Category category1 = EventTestData.makeCategory(2L, "caory1");

        Location location = EventTestData.makeLocation(1L, 10.2, 30.2);
        Location location1 = EventTestData.makeLocation(2L, 20.2, 40.2);

        Event event = EventTestData.makeEvent(user, category, location)
                .title("title")
                .description("this is my event")
                .eventDate(EventTestData.FIXED_TIME.plusHours(30))
                .state(EventState.PUBLISHED)
                .build();

        Event event1 = EventTestData.makeEvent(user1, category1, location1)
                .id(2L)
                .title("MOROZKINO × GORBUFFET “SHASHLYCHNAYA”")
                .description("Throughout February, Morozkino steps beyond the gallery")
                .eventDate(EventTestData.FIXED_TIME.plusHours(20))
                .state(EventState.PUBLISHED)
                .build();

        StatsDto statsDto = StatsDto.builder()
                .uri("/events/" + event1.getId())
                .app("ewm")
                .hits(4L)
                .build();

        StatsDto statsDto1 = StatsDto.builder()
                .uri("/events/" + event.getId())
                .app("ewm")
                .hits(5L)
                .build();

        GetEventsForPublicRequest eventRequest = GetEventsForPublicRequest.builder()
                .text("Gorbuffet")
                .from(0)
                .size(10)
                .paid(false)
                .sort(VIEWS)
                .rangeStart(EventTestData.FIXED_TIME)
                .rangeEnd(EventTestData.FIXED_TIME.plusHours(30))
                .categories(List.of(2L))
                .onlyAvailable(true)
                .build();

        Mockito.when(clientHandler.getStatsForEvents(Mockito.anyList(), Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class)))
                .thenReturn(List.of(statsDto1, statsDto));

        Mockito.when(eventRepository.findAll(Mockito.any(BooleanExpression.class), Mockito.any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(event, event1)));

        Mockito.when(requestRepository.findAllByStatusAndEventIn(Mockito.any(RequestStatus.class), Mockito.anyList()))
                .thenReturn(List.of());

        List<EventShortDto> result = eventPublicService.getEvents(eventRequest);

        assertThat(result.size(), equalTo(2));

        assertThat(result.get(0).getId(), equalTo(2L));
        assertThat(result.get(1).getId(), equalTo(1L));
    }

    @Test
    void shouldGetEventsByLocation() {
        Place place = Place.builder()
                .id(1L)
                .lat(55.75)
                .lon(37.61)
                .radius(1.0)
                .name("Moscow center")
                .build();
        User initiator = EventTestData.makeUser(1L);
        User requester = EventTestData.makeUser(2L);
        Category category = EventTestData.makeCategory(1L);
        Location locationInsideRadius = EventTestData.makeLocation(1L, 55.751, 37.612);
        Location locationOutsideRadius = EventTestData.makeLocation(2L, 59.93, 30.33);
        Event eventInsideRadius = EventTestData.makeEvent(initiator, category, locationInsideRadius)
                .id(1L)
                .state(EventState.PUBLISHED)
                .build();
        Event eventOutsideRadius = EventTestData.makeEvent(initiator, category, locationOutsideRadius)
                .id(2L)
                .state(EventState.PUBLISHED)
                .build();
        Request confirmedRequest = EventTestData.makeRequest(1L, eventInsideRadius, requester)
                .status(RequestStatus.CONFIRMED)
                .build();
        StatsDto statsDto = StatsDto.builder()
                .uri("/events/" + eventInsideRadius.getId())
                .app("ewm")
                .hits(7L)
                .build();
        List<Event> eventsByLocation = List.of(eventInsideRadius);

        Mockito.when(placeRepository.findById(place.getId()))
                .thenReturn(Optional.of(place));
        Mockito.when(eventRepository.findPublishedEventsByLocation(place.getLat(), place.getLon(), place.getRadius()))
                .thenReturn(eventsByLocation);
        Mockito.when(requestRepository.findAllByStatusAndEventIn(RequestStatus.CONFIRMED, eventsByLocation))
                .thenReturn(List.of(confirmedRequest));
        Mockito.when(clientHandler.getStatsForEvents(Mockito.eq(eventsByLocation),
                        Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class)))
                .thenReturn(List.of(statsDto));

        List<EventShortDto> result = eventPublicService.getEventsByLocation(place.getId(), 0, 10);

        Mockito.verify(eventRepository).findPublishedEventsByLocation(place.getLat(), place.getLon(), place.getRadius());
        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), equalTo(eventInsideRadius.getId()));
        assertThat(result.get(0).getViews(), equalTo(7L));
        assertThat(result.get(0).getConfirmedRequests(), equalTo(1L));
        assertThat(result.get(0).getId(), not(equalTo(eventOutsideRadius.getId())));
    }

    @Test
    void shouldThrowWhenPlaceNotFound() {
        Mockito.when(placeRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> eventPublicService.getEventsByLocation(1L, 0, 10));
    }


    @Test
    void shouldGetPublishedEvent() {
        User initiator = EventTestData.makeUser(1L);
        Category category = EventTestData.makeCategory(1L);
        Location location = EventTestData.makeLocation(1L);
        Event event = EventTestData.makeEvent(initiator, category, location)
                .createOn(EventTestData.FIXED_TIME)
                .state(EventState.PUBLISHED)
                .build();
        StatsDto statsDto = StatsDto.builder()
                .uri("/events/" + event.getId())
                .app("ewm")
                .hits(5L)
                .build();

        Mockito.when(eventRepository.findByIdAndState(event.getId(), EventState.PUBLISHED))
                .thenReturn(Optional.of(event));
        Mockito.when(clientHandler.getStatsForEvent(Mockito.eq(event),
                        Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class)))
                .thenReturn(List.of(statsDto));
        Mockito.when(requestRepository.countAllByStatusAndEventId(RequestStatus.CONFIRMED, event.getId()))
                .thenReturn(2L);

        EventFullDto result = eventPublicService.getEvent(event.getId());

        Mockito.verify(eventRepository).findByIdAndState(event.getId(), EventState.PUBLISHED);
        Mockito.verify(clientHandler).getStatsForEvent(Mockito.eq(event),
                Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class));
        Mockito.verify(requestRepository).countAllByStatusAndEventId(RequestStatus.CONFIRMED, event.getId());

        assertThat(result, allOf(
                hasProperty("id", equalTo(event.getId())),
                hasProperty("title", equalTo(event.getTitle())),
                hasProperty("annotation", equalTo(event.getAnnotation())),
                hasProperty("description", equalTo(event.getDescription())),
                hasProperty("eventDate", equalTo(event.getEventDate().format(EventTestData.FORMATTER))),
                hasProperty("createdOn", equalTo(event.getCreateOn().format(EventTestData.FORMATTER))),
                hasProperty("publishedOn", equalTo(event.getPublishedOn().format(EventTestData.FORMATTER))),
                hasProperty("state", equalTo(EventState.PUBLISHED)),
                hasProperty("confirmedRequests", equalTo(2L)),
                hasProperty("views", equalTo(5L))
        ));
    }

    @Test
    void shouldThrowWhenPublishedEventNotFound() {
        long eventId = 1L;

        Mockito.when(eventRepository.findByIdAndState(eventId, EventState.PUBLISHED))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> eventPublicService.getEvent(eventId));

        assertThat(exception.getMessage(), containsString("Event with id=1 was not found or not published"));
        Mockito.verify(eventRepository).findByIdAndState(eventId, EventState.PUBLISHED);
        Mockito.verify(clientHandler, Mockito.never()).getStatsForEvent(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(requestRepository, Mockito.never()).countAllByStatusAndEventId(Mockito.any(), Mockito.anyLong());
    }
}
