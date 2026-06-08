package ru.yandex.practicum.mainservice.requests.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.mainservice.category.model.Category;
import ru.yandex.practicum.mainservice.event.enums.EventState;
import ru.yandex.practicum.mainservice.event.model.Event;
import ru.yandex.practicum.mainservice.event.model.Location;
import ru.yandex.practicum.mainservice.requests.enums.RequestStatus;
import ru.yandex.practicum.mainservice.requests.model.Request;
import ru.yandex.practicum.mainservice.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ActiveProfiles("test")
@DataJpaTest
class RequestRepositoryTest {

    private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2026, 1, 10, 12, 0);

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private RequestRepository requestRepository;


    @Test
    void findAllByRequesterId() {
        User initiator1 = persistUser("initiator");
        User requestor1 = persistUser("requestor");
        User requestor2 = persistUser("requestor2");

        Category targetCategory = persistCategory("concerts");
        Category otherCategory = persistCategory("lectures");

        Event event1 = persistEvent("Matched event", initiator1, targetCategory, false,
                EventState.PUBLISHED, FIXED_TIME.plusDays(10), persistLocation(55.75, 37.61));
        Event event2 = persistEvent("Pending event", initiator1, targetCategory, false,
                EventState.PENDING, FIXED_TIME.plusDays(10), persistLocation(55.75, 37.61));
        persistEvent("Other category event", initiator1, otherCategory, false,
                EventState.PUBLISHED, FIXED_TIME.plusDays(10), persistLocation(55.75, 37.61));

        Request request1 = persistRequest(event1, requestor1, FIXED_TIME.plusDays(1), RequestStatus.PENDING);
        Request request2 = persistRequest(event2, requestor1, FIXED_TIME.plusDays(2), RequestStatus.PENDING);
        persistRequest(event2, requestor2, FIXED_TIME.plusDays(3), RequestStatus.PENDING);

        flushAndClear();

        List<Request> allByRequesterId = requestRepository.findAllByRequesterId(requestor1.getId());

        assertThat(allByRequesterId, hasSize(2));

        List<Long> requestsIds = allByRequesterId.stream()
                .map(Request::getId)
                .toList();

        List<Long> requestsEventIds = allByRequesterId.stream()
                .map(request -> request.getEvent().getId())
                .toList();

        List<Long> requestsRequestorIds = allByRequesterId.stream()
                .map(request -> request.getRequester().getId())
                .toList();

        assertThat(requestsIds, containsInAnyOrder(request1.getId(), request2.getId()));
        assertThat(requestsEventIds, containsInAnyOrder(event1.getId(), event2.getId()));
        assertThat(requestsRequestorIds, containsInAnyOrder(requestor1.getId(), requestor1.getId()));

    }


    @Test
    void findAllByEventId() {
        User initiator1 = persistUser("initiator");
        User requestor1 = persistUser("requestor");
        User requestor2 = persistUser("requestor2");

        Category targetCategory = persistCategory("concerts");
        Category otherCategory = persistCategory("lectures");

        Event event1 = persistEvent("Matched event", initiator1, targetCategory, false,
                EventState.PUBLISHED, FIXED_TIME.plusDays(10), persistLocation(55.75, 37.61));
        Event event2 = persistEvent("Pending event", initiator1, targetCategory, false,
                EventState.PENDING, FIXED_TIME.plusDays(10), persistLocation(55.75, 37.61));
        persistEvent("Other category event", initiator1, otherCategory, false,
                EventState.PUBLISHED, FIXED_TIME.plusDays(10), persistLocation(55.75, 37.61));

        persistRequest(event1, requestor1, FIXED_TIME.plusDays(1), RequestStatus.PENDING);
        Request request2 = persistRequest(event2, requestor1, FIXED_TIME.plusDays(2), RequestStatus.PENDING);
        Request request3 = persistRequest(event2, requestor2, FIXED_TIME.plusDays(2), RequestStatus.PENDING);

        List<Request> allByEventId = requestRepository.findAllByEventId(event2.getId());

        assertThat(allByEventId, hasSize(2));

        List<Long> requestsIds = allByEventId.stream()
                .map(Request::getId)
                .toList();

        List<Long> requestsEventIds = allByEventId.stream()
                .map(request -> request.getEvent().getId())
                .toList();

        List<Long> requestsRequestorIds = allByEventId.stream()
                .map(request -> request.getRequester().getId())
                .toList();

        assertThat(requestsIds, containsInAnyOrder(request2.getId(), request3.getId()));
        assertThat(requestsEventIds, containsInAnyOrder(event2.getId(), event2.getId()));
        assertThat(requestsRequestorIds, containsInAnyOrder(requestor1.getId(), requestor2.getId()));
    }

    @Test
    void findAllByStatusAndEventIn() {
        User initiator = persistUser("initiator");
        User requester1 = persistUser("requester1");
        User requester2 = persistUser("requester2");
        User requester3 = persistUser("requester3");
        User requester4 = persistUser("requester4");

        Category category = persistCategory("concerts");

        Event event1 = persistEvent("First event", initiator, category, false,
                EventState.PUBLISHED, FIXED_TIME.plusDays(10), persistLocation(55.75, 37.61));
        Event event2 = persistEvent("Second event", initiator, category, false,
                EventState.PUBLISHED, FIXED_TIME.plusDays(11), persistLocation(55.76, 37.62));
        Event event3 = persistEvent("Third event", initiator, category, false,
                EventState.PUBLISHED, FIXED_TIME.plusDays(12), persistLocation(55.77, 37.63));

        Request request1 = persistRequest(event1, requester1, FIXED_TIME.plusDays(1), RequestStatus.CONFIRMED);
        Request request2 = persistRequest(event2, requester2, FIXED_TIME.plusDays(2), RequestStatus.CONFIRMED);
        persistRequest(event1, requester3, FIXED_TIME.plusDays(3), RequestStatus.PENDING);
        persistRequest(event3, requester4, FIXED_TIME.plusDays(4), RequestStatus.CONFIRMED);

        flushAndClear();

        List<Request> result = requestRepository.findAllByStatusAndEventIn(
                RequestStatus.CONFIRMED,
                List.of(event1, event2));

        assertThat(result, hasSize(2));

        List<Long> requestIds = result.stream()
                .map(Request::getId)
                .toList();
        List<Long> eventIds = result.stream()
                .map(request -> request.getEvent().getId())
                .toList();
        List<RequestStatus> statuses = result.stream()
                .map(Request::getStatus)
                .toList();

        assertThat(requestIds, containsInAnyOrder(request1.getId(), request2.getId()));
        assertThat(eventIds, containsInAnyOrder(event1.getId(), event2.getId()));
        assertThat(statuses, containsInAnyOrder(RequestStatus.CONFIRMED, RequestStatus.CONFIRMED));
    }

    @Test
    void countAllByStatusAndEventId() {
        User initiator = persistUser("initiator");
        User requester1 = persistUser("requester1");
        User requester2 = persistUser("requester2");
        User requester3 = persistUser("requester3");
        User requester4 = persistUser("requester4");

        Category category = persistCategory("concerts");

        Event event1 = persistEvent("First event", initiator, category, false,
                EventState.PUBLISHED, FIXED_TIME.plusDays(10), persistLocation(55.75, 37.61));
        Event event2 = persistEvent("Second event", initiator, category, false,
                EventState.PUBLISHED, FIXED_TIME.plusDays(11), persistLocation(55.76, 37.62));

        persistRequest(event1, requester1, FIXED_TIME.plusDays(1), RequestStatus.CONFIRMED);
        persistRequest(event1, requester2, FIXED_TIME.plusDays(2), RequestStatus.CONFIRMED);
        persistRequest(event1, requester3, FIXED_TIME.plusDays(3), RequestStatus.PENDING);
        persistRequest(event2, requester4, FIXED_TIME.plusDays(4), RequestStatus.CONFIRMED);

        flushAndClear();

        long result = requestRepository.countAllByStatusAndEventId(RequestStatus.CONFIRMED, event1.getId());

        assertThat(result, equalTo(2L));
    }


    private Request persistRequest(Event event, User requester, LocalDateTime requestTime, RequestStatus status) {
        Request request = Request.builder()
                .created(requestTime)
                .event(event)
                .status(status)
                .requester(requester)
                .build();
        entityManager.persist(request);

        return request;
    }

    private User persistUser(String suffix) {
        User user = User.builder()
                .name("User " + suffix)
                .email(suffix + "@mail.ru")
                .build();
        entityManager.persist(user);
        return user;
    }

    private Category persistCategory(String name) {
        Category category = Category.builder()
                .name(name)
                .build();
        entityManager.persist(category);
        return category;
    }

    private Event persistEvent(String title,
                               User initiator,
                               Category category,
                               boolean paid,
                               EventState state,
                               LocalDateTime eventDate,
                               Location location) {
        Event event = Event.builder()
                .initiator(initiator)
                .category(category)
                .location(location)
                .annotation("Default annotation for " + title)
                .description("Default description for " + title)
                .title(title)
                .createOn(FIXED_TIME)
                .publishedOn(FIXED_TIME.plusHours(1))
                .eventDate(eventDate)
                .paid(paid)
                .participantLimit(10)
                .requestModeration(true)
                .state(state)
                .build();
        entityManager.persist(event);
        return event;
    }

    private Location persistLocation(double lat, double lon) {
        Location location = Location.builder()
                .lat(lat)
                .lon(lon)
                .build();
        entityManager.persist(location);
        return location;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

}
